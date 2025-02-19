package com.example.cocktails;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PerfilFragment extends Fragment {
    private RequestQueue requestQueue;
    private RecyclerView recyclerViewFavorites;
    private FavoriteCocktailAdapter adapter;
    private List<FavoriteCocktail> favoriteCocktailList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        this.requestQueue = Volley.newRequestQueue(view.getContext());
        TextView name = view.findViewById(R.id.userName);
        name.setText(getContext().getSharedPreferences("COCKTAILS_APP_PREFS", Context.MODE_PRIVATE).getString("NAME", null));
        TextView username = view.findViewById(R.id.userUsername);
        username.setText("@" + getContext().getSharedPreferences("COCKTAILS_APP_PREFS", Context.MODE_PRIVATE).getString("USERNAME", null));
        TextView userAcronym = view.findViewById(R.id.userAcronym);
        userAcronym.setText(makeUserAcronym(name.getText().toString()));

//        view.findViewById(R.id.logoutButton).setOnClickListener(view1 -> {
//            JsonObjectRequest request = new JsonObjectRequest(
//                    Request.Method.DELETE,
//                    "http://10.0.2.2:8000/sessions",
//                    null,
//                    success -> {
//                        Toast.makeText(view1.getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
//                        SharedPreferences prefs = view.getContext().getSharedPreferences("COCKTAILS_APP_PREFS", Context.MODE_PRIVATE);
//                        prefs.edit().clear().apply();
//                        startActivity(new Intent(view.getContext(), LoginActivity.class));
//                    },
//                    error -> Toast.makeText(view1.getContext(), "No se pudo cerrar sesión", Toast.LENGTH_SHORT).show()
//            );
//            requestQueue.add(request);
//        });
        view.findViewById(R.id.logoutButton).setOnClickListener(view1 -> {
            // Realizar la solicitud DELETE para cerrar sesión
            String url = "http://10.0.2.2:8000/sessions";
            SharedPreferences prefs = view.getContext().getSharedPreferences("COCKTAILS_APP_PREFS", Context.MODE_PRIVATE);
            String sessionToken = prefs.getString("SessionToken", null);

            if (sessionToken == null) {
                Toast.makeText(view1.getContext(), "No session token found", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.DELETE,
                    url,
                    null,
                    success -> {
                        Toast.makeText(view1.getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
                        prefs.edit().clear().apply();
                        startActivity(new Intent(view1.getContext(), LoginActivity.class));
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e("API_ERROR", "Código de error: " + statusCode);
                            if (error.networkResponse.data != null) {
                                String responseBody = new String(error.networkResponse.data);
                                Log.e("API_ERROR", "Detalles del error: " + responseBody);
                            }
                        }
                        Toast.makeText(view1.getContext(), "No se pudo cerrar sesión", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("SessionToken", sessionToken);
                    return headers;
                }
            };
            requestQueue.add(request);
        });

        view.findViewById(R.id.createCocktailButton).setOnClickListener(v -> {
            startActivity(new Intent(view.getContext(), RecetasActivity.class));
        });

        view.findViewById(R.id.seeCocktailsButton).setOnClickListener(v -> {
            startActivity(new Intent(view.getContext(), UserCocktailListActivity.class));
        });

        recyclerViewFavorites = view.findViewById(R.id.recyclerView);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(view.getContext()));
        favoriteCocktailList = new ArrayList<>();
        adapter = new FavoriteCocktailAdapter(view.getContext(), favoriteCocktailList);
        recyclerViewFavorites.setAdapter(adapter);

        adapter.setOnItemClickListener(cocktail -> {
            Toast.makeText(getContext(), "Clicked: " + cocktail.getName(), Toast.LENGTH_SHORT).show();
        });

        seeFavorites();
        return view;
    }

    private void seeFavorites() {
        String url = "http://10.0.2.2:8000/favoritos/user/";
        SharedPreferences prefs = requireActivity().getSharedPreferences("COCKTAILS_APP_PREFS", Context.MODE_PRIVATE);
        String sessionToken = prefs.getString("SessionToken", null);

        if (sessionToken == null) {
            Toast.makeText(getContext(), "No session token found", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    favoriteCocktailList.clear();
                    parseFavorites(response);
                },
                error -> {
                    Toast.makeText(getContext(), "Error fetching favorites", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", sessionToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private void parseFavorites(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                String name = jsonObject.getString("name");
                String imageUrl = jsonObject.getString("image");
                favoriteCocktailList.add(new FavoriteCocktail(name, imageUrl));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

//    private String makeUserAcronym(String username) {
//        String[] names = username.split(" ");
//        if (names.length == 1) return username.substring(0, 2);
//        return names[0].charAt(0) + names[1].substring(0, 1);
//    }
private String makeUserAcronym(String username) {
    String[] names = username.split(" ");
    if (names.length == 1) {
        // Si solo hay una palabra (nombre o apellido), devuelve las dos primeras letras
        return username.length() > 1 ? username.substring(0, 2) : username; // En caso de que sea solo una letra, la devuelve
    } else {
        // Si hay más de una palabra, devuelve la inicial del primer y segundo nombre
        return names[0].charAt(0) + "" + names[1].charAt(0);
    }
}


    @Override
    public void onResume() {
        super.onResume();
        seeFavorites();
    }
}


