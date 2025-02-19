package com.example.cocktails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BuscadorFragment extends Fragment {
    private EditText nombreText;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_buscador, container, false);

        context = view.getContext();
        nombreText = view.findViewById(R.id.editText);
        ImageButton buscador = view.findViewById(R.id.buscadorBoton);
        Button conAlc = view.findViewById(R.id.conAlcButton);
        Button sinAlc = view.findViewById(R.id.sinAlcButton);
        Button ingre = view.findViewById(R.id.ingreButton);
        Button categ = view.findViewById(R.id.cateButtom);

        Context context = getContext();

        buscador.setOnClickListener(v -> performSearch());
        nombreText.setOnKeyListener((view1, i, keyEvent) -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                performSearch();
                return true;
            }

            return false;
        });

        categ.setOnClickListener(v -> {
            Intent buscarPorCategoriaActivity = new Intent(context, BuscarPorCategoriaActivity.class);
            context.startActivity(buscarPorCategoriaActivity);
        });

        ingre.setOnClickListener(v -> {
            Intent buscarPorIngredienteActivity = new Intent(context, BuscarPorIngredienteActivity.class);
            context.startActivity(buscarPorIngredienteActivity);
        });

        conAlc.setOnClickListener(v -> {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?";
            String type = "a=";
            Intent intent = new Intent(view.getContext(), ResultsActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("type", type);
            intent.putExtra("cocktail", "Alcoholic");
            startActivity(intent);
        });

        sinAlc.setOnClickListener(v -> {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?";
            String type = "a=";
            Intent intent = new Intent(view.getContext(), ResultsActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("type", type);
            intent.putExtra("cocktail", "Non_Alcoholic");
            startActivity(intent);
        });

        return view;

    }

    private void performSearch() {
        String nombre = nombreText.getText().toString();
        if (!nombre.isEmpty()) {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?";
            String type = "s=";
            Intent intent = new Intent(context, ResultsActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("type", type);
            intent.putExtra("cocktail", nombre);
            startActivity(intent);
        } else {
            Toast.makeText(context, "Por favor , ingrese un nombre.", Toast.LENGTH_SHORT).show();
        }
    }

}