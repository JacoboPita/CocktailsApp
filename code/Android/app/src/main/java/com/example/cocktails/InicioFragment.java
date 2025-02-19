package com.example.cocktails;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/** This was the initial code for this fragment. I am commenting it out in case any changes
 * are needed and it can be compared with the original.
 *
 public class InicioFragment extends Fragment {

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
Bundle savedInstanceState) {
// Inflate the layout for this fragment
return inflater.inflate(R.layout.fragment_inicio, container, false);
}
}*/

public class InicioFragment extends Fragment {
    private RecyclerView recyclerView;
    private CocktailAdapter adapter; //before CocktailStarterAdapter
    private ArrayList<Cocktail> cocktailNames = new ArrayList<>();
    private RequestQueue queue;
    private static final int NUMBER_OF_COCKTAILS = 6; // We limit the number of cocktails shown

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflates the fragment layout:
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Configure the RecyclerView with GridLayoutManager:
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columnas
        adapter = new CocktailAdapter(cocktailNames);

        //Assign the listener for clicks on the RecyclerView cells
        adapter.setOnItemClickListener(cocktail -> {
            // Aquí defines qué quieres hacer cuando se haga clic en un cóctel
            // Vamos a abrir una nueva actividad (CocktailDetailActivity) para mostrar detalles
            Intent intent = new Intent(getContext(), SeeCocktailActivity.class);
            intent.putExtra("nombre", cocktail.getName()); // Pasa el nombre del cóctel
            startActivity(intent); // Inicia la nueva actividad
        });

        recyclerView.setAdapter(adapter);

        // Initializes RequestQueue:
        queue = Volley.newRequestQueue(getContext());

        // Load the cocktails:
        loadRandomCocktails();

        return view;
    }

    //PUBLIC API GET TO ORDER SEVERAL RANDOM:
    private void loadRandomCocktails() {
        cocktailNames.clear();

        for (int i = 0; i < NUMBER_OF_COCKTAILS; i++) {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/random.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject drink = response.getJSONArray("drinks").getJSONObject(0);
                                String name = drink.getString("strDrink");
                                String imageUrl = drink.getString("strDrinkThumb");

                                // Añadimos el cóctel a la lista
                                cocktailNames.add(new Cocktail(name, imageUrl));

                                // Notificamos solo cuando todos los cócteles han sido cargados
                                if (cocktailNames.size() == NUMBER_OF_COCKTAILS) {
                                    adapter.notifyDataSetChanged();
                                }

                            } catch (JSONException e) { // I change it to the generic exception, which is bad practice
                                Toast.makeText(getContext(), "Error in data format", Toast.LENGTH_SHORT).show(); //Error en el formato de los datos
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "Error loading cocktails: " + error.getMessage(), Toast.LENGTH_SHORT).show();//Error al cargar los cócteles
                            Log.e("RandomCocktails", "Error: " + error.getMessage());
                        }
                    }
            );

            queue.add(request);
        }
    }
}
