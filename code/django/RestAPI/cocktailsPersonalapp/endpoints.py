import json
import secrets
import bcrypt
from django.db import IntegrityError
from cocktailsPersonalapp.models import User, FavoritoCocktailDB, Session
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

SESSION_TOKEN_HEADER = 'SessionToken'


@csrf_exempt
def favorite(request):
    if request.method == 'PUT':
        body_json = json.loads(request.body)
        json_name = body_json.get('name', None)
        json_image = body_json.get('cocktaildb_image_url')

        # Validamos el SessionToken:
        session_token = request.headers.get('SessionToken', None)
        try:
            # Verificamos que el token sea válido:
            session = Session.objects.get(token=session_token)
        except Session.DoesNotExist:
            return JsonResponse({'error': 'Sesión no válida.'}, status=401)

        if json_name is None or json_image is None:
            return JsonResponse({'error': 'Faltan campos'}, status=400)

        if FavoritoCocktailDB.objects.filter(usuario=session.user, name=json_name).exists():
            return JsonResponse({'error': 'Ya pertenece a favoritos.'}, status=409)

        FavoritoCocktailDB.objects.create(
            usuario=session.user,
            name=json_name,
            image=json_image
        )
        return JsonResponse({'mensaje': 'Cóctel añadido a favoritos.'}, status=201)

    elif request.method == 'POST':
        body_json = json.loads(request.body)
        json_name = body_json.get('name', None)

        # Validamos el SessionToken:
        session_token = request.headers.get('SessionToken', None)
        try:
            # Verificamos que el token sea válido:
            session = Session.objects.get(token=session_token)
        except Session.DoesNotExist:
            return JsonResponse({'error': 'Sesión no válida.'}, status=401)

        if json_name is None:
            return JsonResponse({'error': 'Faltan campos'}, status=400)

        favorito = FavoritoCocktailDB.objects.filter(usuario=session.user, name=json_name).first()

        if favorito is None:
            return JsonResponse({'error': 'Favorito no encontrado.'})

        favorito.delete()

        return JsonResponse({'mensaje': 'Favorito eliminado correctamente.'})


@csrf_exempt
def register(request):
    if request.method == 'POST':
        try:
            body_json = json.loads(request.body)
            json_name = body_json.get('name', None)
            json_username = body_json.get('username', None)
            json_password = body_json.get('password', None)

            if json_name is None:
                return JsonResponse({"error": "name can't be None"}, status=400)
            if json_username is None:
                return JsonResponse({"error": "username can't be None"}, status=400)
            if json_password is None:
                return JsonResponse({"error": "password can't be None"}, status=400)

            salted_and_hashed_pass = bcrypt.hashpw(json_password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
            user_object = User(name=json_name, username=json_username, encrypted_password=salted_and_hashed_pass)
            user_object.save()

            # Login automático tras registrarse
            random_token = secrets.token_hex(10)
            session = Session(user=user_object, token=random_token)
            session.save()
            return JsonResponse({"token": random_token}, status=201)
        except IntegrityError:
            return JsonResponse({"error": "username already taken"}, status=409)
    elif request.method == 'GET':
        json_response = []
        all_rows = User.objects.all()
        for row in all_rows:
            json_response.append(row.name)
        return JsonResponse(json_response, status=200, safe=False)
    else:
        return JsonResponse({'error': 'Unsupported HTTP method'}, status=405)


@csrf_exempt
def check_favorite(request):
    if request.method == 'GET':
        # Validamos el SessionToken:
        session_token = request.headers.get('SessionToken', None)
        try:
            session = Session.objects.get(token=session_token)
        except Session.DoesNotExist:
            return JsonResponse({'error': 'Sesión no válida.'}, status=401)

        name = request.GET.get('name')  # Cambiado query_params por GET
        if name is None:
            return JsonResponse({'error': 'Falta el nombre del cóctel'}, status=400)

        is_favorite = FavoritoCocktailDB.objects.filter(
            usuario=session.user,
            name=name
        ).exists()
        return JsonResponse({'is_favorite': is_favorite})
    # return JsonResponse({'error': 'Método no permitido'}, status=405)
    return JsonResponse({'error': 'Método no permitido'})


@csrf_exempt
def login(request):
    if request.method == 'POST':
        try:
            body_json = json.loads(request.body)
        except json.JSONDecodeError as e:
            return JsonResponse({"error": f"JSON mal formado: {str(e)}"}, status=400)

        json_username = body_json.get('username', None)
        json_password = body_json.get('password', None)

        if json_username is None:
            return JsonResponse({"error": "username can't be None"}, status=400)
        if json_password is None:
            return JsonResponse({"error": "password can't be None"}, status=400)

        try:
            user_object = User.objects.get(username=json_username)
        except User.DoesNotExist:
            return JsonResponse({"error": "User does not exist"}, status=404)

        # Detectamos si el usuario ya ha iniciado sesión
        # sessions = Session.objects.filter(user=user_object)
        # if sessions.exists():
        # return JsonResponse({"error": "User already logged in"}, status=409)

        if bcrypt.checkpw(json_password.encode('utf8'), user_object.encrypted_password.encode('utf8')):
            # Las contraseñas coinciden, procedemos a iniciar sesión
            random_token = secrets.token_hex(10)
            session = Session(user=user_object, token=random_token)
            session.save()
            return JsonResponse({"token": random_token, "username": user_object.username, "name": user_object.name},
                                status=201)
        else:
            return JsonResponse({"error": "Not valid password"}, status=401)
    elif request.method == 'DELETE':
        json_token = request.headers.get('SessionToken', None)

        if json_token is None:
            return JsonResponse({"error": "token can't be None"}, status=400)

        try:
            session_object = Session.objects.get(token=json_token)
        except Session.DoesNotExist:
            return JsonResponse({"error": "Session does not exist"}, status=404)

        session_object.delete()
        return JsonResponse({"status": "logged out"}, status=200)
    else:
        return JsonResponse({'error': 'Unsupported HTTP method'}, status=405)
