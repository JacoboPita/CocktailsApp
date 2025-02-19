package com.example.cocktails;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserCocktailDetailActivity extends AppCompatActivity {

    private ImageView imageViewCocktail;
    private TextView textViewName, textViewGlass, textViewInstructions, textViewIngredients;
    private ImageButton buttonDelete;
    private String cocktailNameStr;
    private int cocktailId;  // Guardamos el ID
    private RequestQueue queue;
    private ImageButton btnShowIngredients;
    private ImageButton btnShowIntructions;
//    private String fullIngredientsList;
//    private String fullIntructionsList;
    private String fullIngredientsList = "";
    private String fullIntructionsList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_cocktail_detail);

        // Inicializar vistas
        imageViewCocktail = findViewById(R.id.imageViewCocktail);
        textViewName = findViewById(R.id.textViewName);
        textViewGlass = findViewById(R.id.textViewGlass);
        textViewInstructions = findViewById(R.id.textViewInstructions);
        textViewIngredients = findViewById(R.id.textViewIngredients);
        buttonDelete = findViewById(R.id.buttonDelete);  // Cambiado a botón de eliminar
        btnShowIngredients = findViewById(R.id.btnShowIngredients);
        btnShowIntructions = findViewById(R.id.btnShowInstructions);



        if (fullIngredientsList.length() > 100) {
            textViewIngredients.setText(fullIngredientsList.substring(0, 100) + "...");
            textViewInstructions.setText(fullIngredientsList.substring(0, 100) + "...");
        }



        // Obtener el nombre del cóctel desde el intent
        Intent intent = getIntent();
        cocktailId = intent.getIntExtra("id", -1);  // Obtener el ID
        cocktailNameStr = intent.getStringExtra("name");

        // Mostrar el nombre en la Toolbar
        textViewName.setText(cocktailNameStr);

        // Inicializar Volley
        queue = Volley.newRequestQueue(this);

        // Cargar detalles del cóctel
        loadCocktailDetails();


        // Manejar clic en el botón de favoritos (puedes personalizar la acción)
        buttonDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Cocktail")
                    .setMessage("Are you sure you want to delete this cocktail?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // cAMBIO EL ICONO CUANDO ESTÁ ELIMINADO"
                        buttonDelete.setImageResource(R.drawable.papelera_negra);

                        deleteCocktail();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    private void showDialog(String lista, String name) {
        if (lista == null || lista.isEmpty()) {
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleDialog = dialogView.findViewById(R.id.titleDialog);
        titleDialog.setText(name);
        TextView dialogIngredientsText = dialogView.findViewById(R.id.dialogIngredientsText);
        dialogIngredientsText.setText(lista);

        builder.setView(dialogView)
                .setPositiveButton("Cerrar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void loadCocktailDetails() {
        String url = "http://10.0.2.2:8000/cocktail/user/" + cocktailId + "/";
        Log.d("DEBUG", "Cargando detalles desde: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String imageBase64 = response.getString("imagen");
                        String instructions = response.getString("instrucciones");
                        String glass = response.getString("vaso");
                        JSONArray ingredientsArray = response.getJSONArray("ingredientes");

                        // Decodificar imagen Base64
                        if (imageBase64 != null && imageBase64.startsWith("data:image")) {
                            imageBase64 = imageBase64.split(",")[1];  // Quitar el prefijo "data:image/jpeg;base64,"
                        }
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageViewCocktail.setImageBitmap(decodedBitmap);  // 🔹 Mostrar imagen en el ImageView

                        // Convertir ingredientes a String
                        StringBuilder ingredientsStr = new StringBuilder();
                        for (int i = 0; i < ingredientsArray.length(); i++) {
                            JSONObject ingredient = ingredientsArray.getJSONObject(i);
                            String name = ingredient.getString("nombreIngrediente");
                            String quantity = ingredient.getString("cantidadIngrediente");
                            ingredientsStr.append(name).append(": ").append(quantity).append("\n");
                        }

                        //actualizamos la vista con los datos que obtenemos:
                        textViewGlass.setText("Glass: " + glass);
                        textViewInstructions.setText(instructions);
                        textViewIngredients.setText(ingredientsStr.toString());
                        fullIngredientsList = ingredientsStr.toString();
                        fullIntructionsList = instructions;

                        btnShowIngredients.setOnClickListener(v -> showDialog(fullIngredientsList, "Ingredients"));
                        btnShowIntructions.setOnClickListener(v -> showDialog(fullIntructionsList, "Instructions"));
                    } catch (JSONException e) {
                        Log.e("ERROR", "Error procesando JSON: " + e.getMessage());
                        Toast.makeText(this, "Error loading details.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error en la petición: " + error.getMessage()); //Error en la petición
                    Toast.makeText(this, "Error loading details.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                Log.d("DEBUG", "Enviando SessionToken en la cabecera: " + obtenerToken());
                return headers;
            }
        };

        queue.add(request);
    }




    // Método para eliminar el cóctel usando DELETE
    private void deleteCocktail() {
        String url = "http://10.0.2.2:8000/cocktail/delete/" + cocktailId + "/";
        Log.d("DEBUG", "Eliminadno cocktail en: " + url);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d("DEBUG", "Cocktail eliminado.");

                    // Deshabilitar el bton de papelera para poder eliminar:
                    //buttonDelete.setImageResource(R.drawable.papelera_deshabilitada);
                    buttonDelete.setEnabled(false);

                    // Volver a la pantalla anterior y actualizar lista
                    Intent intent = new Intent(this, UserCocktailListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e("API_ERROR", "Error al eliminar: " + error.getMessage());
                    Toast.makeText(this, "Error deleting cocktail.", Toast.LENGTH_SHORT).show();

                    // Si falla recupera el boton inicial, el de pepelera habilitada (la de linea)
                    buttonDelete.setImageResource(R.drawable.papelera);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                return headers;
            }
        };

        queue.add(request);
    }


    // Recupera el token de las shared preferecnes:
    private String obtenerToken() {
        SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
        return preferences.getString("SessionToken", null);
    }

}

