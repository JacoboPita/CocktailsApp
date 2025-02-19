from django.db import models


class User(models.Model):
    name = models.CharField(max_length=100)
    username = models.CharField(unique=True, max_length=50)
    encrypted_password = models.CharField(max_length=100)

    def __str__(self):
        return self.username


class Session(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(unique=True, max_length=50)

    def __str__(self):
        return str(self.user) + ' - ' + self.token


class Cocktail(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='cocktails') 
    nombre = models.CharField(max_length=100)
    instrucciones = models.TextField()
    imagen = models.ImageField(upload_to='cocktails/', blank=True, null=True) # Necesary for imageBase64
    #imagen = models.URLField(blank=True, null=True)  # Opcional: URL de la imagen
    # imagen = models.TextField()  # Imagen en formato Base64
    vaso = models.CharField(max_length=50)  # Tipo de vaso

    def __str__(self):
        return self.nombre


class CocktailIngredientes(models.Model):
    # Referencia a la tabla principal de cocktails
    cocktail = models.ForeignKey(Cocktail, on_delete=models.CASCADE)
    nombreIngrediente = models.CharField(max_length=100)
    # cantidadIngrediente = models.DecimalField(max_digits=5, decimal_places=2)
    cantidadIngrediente = models.CharField(max_length=50)  # Ahora acepta texto y números

    def __str__(self):
        return f"{self.nombreIngrediente} ({self.cantidadIngrediente}) - {self.cocktail.nombre}"


class FavoritoCocktailDB(models.Model):
    usuario = models.ForeignKey("User", on_delete=models.CASCADE,
                                related_name="favoritos_cocktaildb") # Usuario que guarda el favorito
    name = models.CharField(max_length=100)  # Nombre del cóctel de la API pública
    image = models.URLField(blank=True, null=True)
    def __str__(self):
        return f"Favorito CocktailDB: {self.name} - Usuario: {self.usuario.username}"
