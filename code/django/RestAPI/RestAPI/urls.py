"""
URL configuration for RestAPI project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.1/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from cocktailsPersonalapp import endpoints, endpoint_cocktail_create, endpoint_cocktail_see, endpoint_cocktail_delete, endpoint_favourite_see
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('user', endpoints.register),
    path('sessions', endpoints.login),
    path('cocktail/', endpoint_cocktail_create.create_cocktail),
    path('favoritos/cocktaildb/check/', endpoints.check_favorite),
    path('favoritos/cocktaildb/', endpoints.favorite),
    path('cocktail/user/', endpoint_cocktail_see.see_user_cocktail),
    path('cocktail/user/<int:cocktail_id>/', endpoint_cocktail_see.see_user_cocktail),
    path('cocktail/delete/<int:cocktail_id>/', endpoint_cocktail_delete.delete_cocktail),
    path('favoritos/user/', endpoint_favourite_see.see_user_favourite),

]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
