package com.example.cocktails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.android.volley.RequestQueue;


public class UserCocktailListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CocktailAdapter adapter;
    private ArrayList<Cocktail> cocktailNames = new ArrayList<>();
    private RequestQueue queue;
    private ProgressBar progressBar;
    private TextView resultQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_cocktail_list);

        // Inicializar RecyclerView que está en activity_user_cocktail_list
        recyclerView = findViewById(R.id.cocktailsView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CocktailAdapter(cocktailNames);
        recyclerView.setAdapter(adapter);

        // Inicializar ProgressBar y TextView
        progressBar = findViewById(R.id.progressBar);
        resultQuantity = findViewById(R.id.resultQuantity);

        // Inicializo Volley RequestQueue una sola vez. IMPORTANTE!!! a veces me olvido
        queue = Volley.newRequestQueue(this);

        //Este es el código para los clics en las celdas del RV:
        adapter.setOnItemClickListener(cocktail -> {
            Intent intent = new Intent(UserCocktailListActivity.this, UserCocktailDetailActivity.class);
            intent.putExtra("id", cocktail.getId());  // Pasar el ID del cóctel
            intent.putExtra("name", cocktail.getName());

            // Aqui se convierte el json array de los ingredientes a un string:
            StringBuilder ingredientsStr = new StringBuilder();
            JSONArray ingredientsArray = cocktail.getIngredients();
            if (ingredientsArray != null) {
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    try {
                        JSONObject ingredient = ingredientsArray.getJSONObject(i);
                        String name = ingredient.getString("nombreIngrediente");
                        String quantity = ingredient.getString("cantidadIngrediente");
                        ingredientsStr.append(name).append(": ").append(quantity).append("\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            intent.putExtra("ingredients", ingredientsStr.toString());

            startActivity(intent);
        });


        // Este es el metodo que carga los cocteles (hace la petición al endpoint de GET)
        loadUserCocktails();
    }

    //lo necesito para que se vuelvan a cargar los cocteles cuando volvemos a esta actividad
    @Override
    protected void onResume() {
        super.onResume();
        loadUserCocktails();  // Vuelve a cargar los cocteles cuando volvemos a esta actividad
    }


    // SOLO IMAGEN Y NOMBRE - LUEGO RECUPERAREMOS EL RESTO DE DATOS CON OTRO GET DESDE EL DETAIL
    private void loadUserCocktails() {
        Log.d("DEBUG", "La función loadUserCocktails() se ha llamado.");
        String url = "http://10.0.2.2:8000/cocktail/user/"; //URL DEL ENDPOINT CORRSPONDIENTE

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray drinks = response.getJSONArray("cocktails");
                        cocktailNames.clear(); //es por precaución pero funciona sin ello

                        for (int j = 0; j < drinks.length(); j++) {
                            JSONObject drink = drinks.getJSONObject(j);
                            int id = drink.getInt("id");  // Obtener el ID del JSON
                            String name = drink.getString("nombre");
                            String imageBase64 = drink.getString("imagen");

                            cocktailNames.add(new Cocktail(id, name, imageBase64));
                        }

                        Log.d("DEBUG", "Total cocktails cargados en la lista: " + cocktailNames.size());

                        //asi nos aseguramos que el RV se actualiza con los datos recibidos en el adapter:
                        adapter.notifyDataSetChanged();
                        Log.d("DEBUG", "Adapter actualizado correctamente.");

                        //actualiza el textView con el número de cockteles que se han recuperado:
                        resultQuantity.setText("Total cocktails: " + cocktailNames.size());

                        //cuando los cockteles se cargan, debemos ocultar el progress bar:
                        progressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        Log.e("ERROR", "Error en los datos: " + e.getMessage());
                        Toast.makeText(UserCocktailListActivity.this, "Data error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String errorData = new String(error.networkResponse.data);
                        Log.e("API_ERROR", "Status Code: " + statusCode + ", Response: " + errorData);
                        Toast.makeText(UserCocktailListActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("API_ERROR", "Network error: " + error.getMessage());
                        Toast.makeText(UserCocktailListActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            // Necesario para recuperar datos de las cabeceras (ya que no tenemos clase auxiliar)
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                Log.d("DEBUG", "El SessionToken se envía en la cabecera");
                return headers;
            }
        };
        queue.add(request); // Es la instancia de cola que inicialicé en onCreate()
    }

    /**
     // TODOS LOS DATOS DEL COCKTAIL, LUEGO EN DETAIL SE RECUPERAN CON INTENT
     private void loadUserCocktails() {
     Log.d("DEBUG", "La función loadUserCocktails() se ha llamado.");
     String url = "http://10.0.2.2:8000/cocktail/user/";

     JsonObjectRequest request = new JsonObjectRequest(
     Request.Method.GET,
     url,
     null,
     response -> {
     try {
     JSONArray drinks = response.getJSONArray("cocktails");
     cocktailNames.clear();

     for (int j = 0; j < drinks.length(); j++) {
     JSONObject drink = drinks.getJSONObject(j);
     String name = drink.getString("nombre");
     String imageBase64 = drink.getString("imagen");
     String instructions = drink.getString("instrucciones");
     String glass = drink.getString("vaso");
     JSONArray ingredients = drink.getJSONArray("ingredientes");

     cocktailNames.add(new Cocktail(name, imageBase64, instructions, ingredients, glass));
     }

     Log.d("DEBUG", "Total cocktails cargados en la lista: " + cocktailNames.size());
     adapter.notifyDataSetChanged();

     } catch (JSONException e) {
     Log.e("ERROR", "Error en los datos: " + e.getMessage());
     Toast.makeText(UserCocktailListActivity.this, "Data error", Toast.LENGTH_SHORT).show();
     }
     },
     error -> {
     if (error.networkResponse != null) {
     int statusCode = error.networkResponse.statusCode;
     String errorData = new String(error.networkResponse.data);
     Log.e("API_ERROR", "Status Code: " + statusCode + ", Response: " + errorData);
     Toast.makeText(UserCocktailListActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
     } else {
     Log.e("API_ERROR", "Network error: " + error.getMessage());
     Toast.makeText(UserCocktailListActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
     }
     }) {
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
    Map<String, String> headers = new HashMap<>();
    headers.put("SessionToken", obtenerToken());
    Log.d("DEBUG", "El SessionToken se envía en la cabecera");
    return headers;
    }
    };

     queue.add(request);
     }

     */

    // Recupera el token de las shared preferecnes:
    private String obtenerToken() {
        SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
        return preferences.getString("SessionToken", null);
    }
}
