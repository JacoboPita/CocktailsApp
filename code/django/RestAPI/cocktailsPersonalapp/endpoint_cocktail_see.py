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
def see_user_cocktail(request, cocktail_id=None):  # Ahora puede recibir un ID opcional
    if request.method != 'GET':
        return JsonResponse({'error': 'Método HTTP no soportado.'}, status=405)

    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)

    # Si se proporciona un ID, intentamos obtener solo ese cóctel
    if cocktail_id:
        try:
            cocktail = Cocktail.objects.get(id=cocktail_id, user=session.user)
            # Convertir imagen a Base64 si existe
            base64_image = None
            if cocktail.imagen:
                with open(cocktail.imagen.path, "rb") as img_file:
                    base64_image = base64.b64encode(img_file.read()).decode('utf-8')

            ingredientes = list(CocktailIngredientes.objects.filter(cocktail=cocktail).values("nombreIngrediente", "cantidadIngrediente"))

            return JsonResponse({
                'id': cocktail.id,
                'nombre': cocktail.nombre,
                'imagen': f"data:image/jpeg;base64,{base64_image}" if base64_image else None,
                'instrucciones': cocktail.instrucciones,
                'vaso': cocktail.vaso,
                'ingredientes': ingredientes
            }, status=200)

        except Cocktail.DoesNotExist:
            return JsonResponse({'error': 'Cóctel no encontrado.'}, status=404)

    # Si no hay ID, devolver todos los cócteles del usuario
    cocktails = Cocktail.objects.filter(user=session.user)
    cocktail_list = []

    for cocktail in cocktails:
        base64_image = None
        if cocktail.imagen:
            with open(cocktail.imagen.path, "rb") as img_file:
                base64_image = base64.b64encode(img_file.read()).decode('utf-8')

        ingredientes = list(CocktailIngredientes.objects.filter(cocktail=cocktail).values("nombreIngrediente", "cantidadIngrediente"))

        cocktail_list.append({
            'id': cocktail.id,
            'nombre': cocktail.nombre,
            'imagen': f"data:image/jpeg;base64,{base64_image}" if base64_image else None,
            'instrucciones': cocktail.instrucciones,
            'vaso': cocktail.vaso,
            'ingredientes': ingredientes
        })

    return JsonResponse({'cocktails': cocktail_list}, status=200)
