import json
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Cocktail, Session

SESSION_TOKEN_HEADER = 'SessionToken'


@csrf_exempt
def delete_cocktail(request, cocktail_id):  # Ahora recibimos el ID en la URL
    if request.method != 'DELETE':
        return JsonResponse({'error': 'Método HTTP no soportado.'}, status=405)

    # Verificar el token de sesión
    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'SessionToken no válida.'}, status=403)

    try:
        # Buscar el cocktail por ID
        cocktail = Cocktail.objects.get(id=cocktail_id)

        # Verificar que el usuario es el propietario
        if cocktail.user != session.user:
            return JsonResponse({'error': 'No tienes permiso para eliminar este cóctel.'}, status=403)

        # Eliminar el cocktail
        cocktail.delete()

        return JsonResponse({'message': 'Cóctel eliminado con éxito.'}, status=200)

    except Cocktail.DoesNotExist:
        return JsonResponse({'error': 'Cóctel no encontrado.'}, status=404)
