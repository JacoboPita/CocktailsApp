package com.example.cocktails;

public class FavoriteCocktail {
    private String name;
    private String imageUrl;

    public FavoriteCocktail(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
