package com.example.cocktails;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BuscarPorCategoriaActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private ListView listView;
    private ArrayList<String> resultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_por_categoria);

        listView = findViewById(R.id.listView);
        resultados = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Configura el listener para los clics en los elementos del ListView
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?";
            String type = "c=";
            String name = adapterView.getAdapter().getItem(i).toString().replace(" ", "_");

            Intent intent = new Intent(BuscarPorCategoriaActivity.this, ResultsActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("type", type);
            intent.putExtra("cocktail", name);
            startActivity(intent);
        });

        buscarPorCategoria();
    }


    private void buscarPorCategoria() {
        String categoriasUrl = "https://www.thecocktaildb.com/api/json/v1/1/list.php?c=list";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                categoriasUrl,
                null,
                response -> {
                    try {
                        JSONArray categories = response.getJSONArray("drinks");
                        for (int i = 0; i < categories.length(); i++) {
                            JSONObject category = categories.getJSONObject(i);
                            String nombre = category.getString("strCategory");
                            resultados.add(nombre);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BuscarPorCategoriaActivity.this, android.R.layout.simple_list_item_1, resultados);
                        listView.setAdapter(adapter);

                    } catch (JSONException e) {
                        Toast.makeText(BuscarPorCategoriaActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(BuscarPorCategoriaActivity.this, "Error en la solicitud", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }
}
