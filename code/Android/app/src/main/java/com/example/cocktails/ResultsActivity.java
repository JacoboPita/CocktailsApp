package com.example.cocktails;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private String url;
    private String type;
    private String cocktailName;
    private boolean ownAPI = false;
    private VolleyCallback callback;
    ArrayList<Cocktail> resultados = new ArrayList<>();
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        this.requestQueue = Volley.newRequestQueue(this);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        url = getIntent().getStringExtra("url");
        type = getIntent().getStringExtra("type");
        cocktailName = getIntent().getStringExtra("cocktail");
        ownAPI = getIntent().getBooleanExtra("ownAPI", false);
        Log.i("URL Info", url + type + "=" + cocktailName);

        if (ownAPI) {
            TextView resultsTitle = findViewById(R.id.resultsTitle);
            resultsTitle.setText(type);
            type = "";
        }
        callback = new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                progressBar.setVisibility(View.INVISIBLE);
                if (ownAPI) mostrarResultadosOwnAPI(result);
                else mostrarResultados(result);


                TextView resultQuantity = findViewById(R.id.resultQuantity);
                String str = resultados.size() + " Cocktails found";
                resultQuantity.setText(str);

                // Configurar el RecyclerView
                RecyclerView recyclerView = findViewById(R.id.cocktailsView);
                recyclerView.setLayoutManager(new GridLayoutManager(ResultsActivity.this, 2));
                CocktailAdapter adapter = new CocktailAdapter(resultados);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("Error", error.getMessage());
            }
        };

        requestCocktails(callback);
    }

    private void requestCocktails(final VolleyCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url + type + cocktailName,
                null,
                callback::onSuccess,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
                headers.put("SessionToken", preferences.getString("SessionToken", null));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void mostrarResultados(JSONObject response) {
        try {
            JSONArray drinks = response.getJSONArray("drinks");
            for (int i = 0; i < drinks.length(); i++) {
                JSONObject cocktailObject = drinks.getJSONObject(i);

                String nombre = cocktailObject.getString("strDrink");
                String imagen = cocktailObject.getString("strDrinkThumb");

                Cocktail cocktail = new Cocktail(nombre, imagen);
                resultados.add(cocktail);
            }
        } catch (JSONException e) {
            Toast.makeText(ResultsActivity.this, "No se encontraron resultados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarResultadosOwnAPI(JSONObject response) {
        try {
            JSONArray drinks = response.getJSONArray("cocktails");
            for (int i = 0; i < drinks.length(); i++) {
                JSONObject cocktailObject = drinks.getJSONObject(i);

                String nombre = cocktailObject.getString("nombre");
                String imagen = cocktailObject.getString("imagen");

                Cocktail cocktail = new Cocktail(nombre, imagen);
                resultados.add(cocktail);
            }
        } catch (JSONException e) {
            Toast.makeText(ResultsActivity.this, "No se encontraron resultados.", Toast.LENGTH_SHORT).show();
        }
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);

        void onError(VolleyError error);
    }
}
