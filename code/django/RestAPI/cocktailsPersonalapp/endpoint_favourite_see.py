from django.http import JsonResponse
from .models import FavoritoCocktailDB, Session

SESSION_TOKEN_HEADER = 'SessionToken'

def see_user_favourite(request):
    if request.method != "GET":
        return JsonResponse({'error': 'Método HTTP no soportado.'}, status=405)

    # Validamos el token de sesión
    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'Sesión no válida.'}, status=401)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'Sesión no válida.'}, status=401)

    # Obtenemos los cócteles favoritos del usuario
    favoritos = FavoritoCocktailDB.objects.filter(usuario=session.user)

    # Construimos la respuesta en JSON
    data = [{"name": fav.name, "image": fav.image} for fav in favoritos]

    return JsonResponse(data, safe=False, status=200)
