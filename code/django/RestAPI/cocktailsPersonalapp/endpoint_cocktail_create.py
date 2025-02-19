import json, base64
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.core.files.base import ContentFile
from .models import Cocktail, CocktailIngredientes, Session

SESSION_TOKEN_HEADER = 'SessionToken'

# convert base64 image to file (Stackoverflow):
def base64_file(data, name=None):
    _format, _img_str = data.split(';base64,')
    _name, ext = _format.split('/')
    if not name:
        name = _name.split(":")[-1]
    return ContentFile(base64.b64decode(_img_str), name='{}.{}'.format(name, ext))

@csrf_exempt
def create_cocktail(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Método HTTP no soportado.'}, status=405)
    
    # json.loads can raise a JSONDecodeError if we receive a malformed JSON. 
    # Include a try-except and return a 400 to the client in that case:
    try: 
        body = json.loads(request.body) 
    except json.JSONDecodeError: 
        return JsonResponse({'error': 'JSON mal formado.'}, status=400)

    # Cocktail details (all required):
    nombre = body.get('nombre', None)
    instrucciones = body.get('instrucciones', None)
    ingredientes = body.get('ingredientes', []) # no todos son necesarios
    imagen_base64 = body.get('imagen', None)
    vaso = body.get('vaso', None)

    # We validate the required fields:
    if not nombre or not instrucciones or not vaso or not imagen_base64:
        return JsonResponse({'error': 'Falta algún campo por rellenar.'}, status=400)


    # We validate the required fields:
    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)

    try:
        # We verify that the token is valid:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)
    
    try:
        imagen = base64_file(imagen_base64)  # The base64_file method decodes the image
    except (ValueError, TypeError, IndexError, base64.binascii.Error):
            return JsonResponse({'error': 'Formato de imagen no válido.'}, status=400)
    

    # Create the cocktail in BD:
    cocktail = Cocktail.objects.create(
        user=session.user,
        nombre=nombre,
        instrucciones=instrucciones,
        imagen=imagen, # decoded image
        vaso=vaso
    )

    # We validate and create the ingredients:
    for ingrediente in ingredientes:
        nombre_ingrediente = ingrediente.get('nombreIngrediente')
        cantidad_ingrediente = ingrediente.get('cantidadIngrediente')

        if not nombre_ingrediente or not cantidad_ingrediente:
            # return JsonResponse({'error': 'Cada ingrediente debe tener nombre y cantidad.'}, status=400)
            continue

        # The ingredients are associated with a cocktail:
        CocktailIngredientes.objects.create(
            cocktail=cocktail,
            nombreIngrediente=nombre_ingrediente,
            cantidadIngrediente=cantidad_ingrediente
        )

    # Message to the user if the cocktail has been created correctly:
    return JsonResponse({
        'created': 'Cóctel creado con éxito.',
        'id': cocktail.id  # id del cocktail que necesitamos para poder borrar, porque por nombre es mala práctica
    }, status=201)

