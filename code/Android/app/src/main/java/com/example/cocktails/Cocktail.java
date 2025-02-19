package com.example.cocktails;

import org.json.JSONArray;

import java.io.Serializable;


public class Cocktail implements Serializable {
    private int id; //no lo recibimos si es de la api publica
    private String name;
    //private String image;
    private String imageUrl; //la vamos a unificar así para que nos sirva para ambas apis
    private String instructions;
    private JSONArray ingredients;
    private String glass;

    //VAMOS A TENER VARIOS CONSTRUCTORES PARA QUE SE PUEDAN ADAPTAR A AMBAS APIS:
    // Constructor mínimo (usado en la API pública)
    public Cocktail(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Constructor con ID (para API propia)
    public Cocktail(int id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Constructor completo (cuando tenemos toda la información)
    public Cocktail(int id, String name, String imageUrl, String instructions, JSONArray ingredients, String glass) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.glass = glass;
    }

    // Constructor completo sin id
    public Cocktail(String name, String imageUrl, String instructions, JSONArray ingredients, String glass) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.glass = glass;
    }

    //getters & setters de todo

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public JSONArray getIngredients() {
        return ingredients;
    }

    public void setIngredients(JSONArray ingredients) {
        this.ingredients = ingredients;
    }

    public String getGlass() {
        return glass;
    }

    public void setGlass(String glass) {
        this.glass = glass;
    }
}
