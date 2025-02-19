package com.example.cocktails;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.Manifest;


public class RecetasActivity extends AppCompatActivity {
    // Variables necesarias:
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivImagePreview;
    private Uri imageUri;
    private Bitmap bitmap;
    private String base64Image; // Cadena para almacenar la imagen codificada
    private EditText cocktailName, instructions, ingredient1, cantidad1;
    private EditText ingredient2, cantidad2, ingredient3, cantidad3;
    private EditText ingredient4, cantidad4, ingredient5, cantidad5;
    private EditText ingredient6, cantidad6, ingredient7, cantidad7;
    private EditText ingredient8, cantidad8, glass;
    private Button buttonSubmit;
    private ImageButton buttonChooseImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        // Referencio las variables con los items del xml:
        cocktailName = findViewById(R.id.cocktailNameEditText);
        instructions = findViewById(R.id.instructionsEditText);
        ingredient1 = findViewById(R.id.ingredient1EditText);
        cantidad1 = findViewById(R.id.cantidadIngrediente1EditText);
        ingredient2 = findViewById(R.id.ingredient2EditText);
        cantidad2 = findViewById(R.id.cantidadIngredient2EditText);
        ingredient3 = findViewById(R.id.ingredient3EditText);
        cantidad3 = findViewById(R.id.cantidadIngrediente3EditText);
        ingredient4 = findViewById(R.id.ingredient4EditText);
        cantidad4 = findViewById(R.id.cantidadIngrediente4EditText);
        ingredient5 = findViewById(R.id.ingredient5EditText);
        cantidad5 = findViewById(R.id.cantidadIngrediente5EditText);
        ingredient6 = findViewById(R.id.ingredient6EditText);
        cantidad6 = findViewById(R.id.cantidadIngrediente6EditText);
        ingredient7 = findViewById(R.id.ingredient7EditText);
        cantidad7 = findViewById(R.id.cantidadIngrediente7EditText);
        ingredient8 = findViewById(R.id.ingredient8EditText);
        cantidad8 = findViewById(R.id.cantidadIngrediente8EditText);
        glass = findViewById(R.id.glassEditText);
        buttonSubmit = findViewById(R.id.submitButton);
        ivImagePreview = findViewById(R.id.imagePreviewIP);
        buttonChooseImage = findViewById(R.id.chooseImageBtn);

        //elegimos una imagen desde el almacenamiento del emulador en downloads:
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        //enviamos la receta que hemos creado a nuestro api rest:
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCocktailData();
            }
        });

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
        imageUri = data.getData();
        try {
            // Convierte la URI en un Bitmap y actualiza la vista previa
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ivImagePreview.setImageBitmap(bitmap);
            convertImageToBase64(bitmap); // Convierte la imagen seleccionada a Base64
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
        }
    }
}

    // metodo que onvierte el mapa de bits a string (Base64):
    private void convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Comprime la imagen
        byte[] imageBytes = baos.toByteArray();
        base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT); // Codifica la imagen a Base64
    }


    private void sendCocktailData() {

        if (bitmap == null || base64Image == null) {
            Toast.makeText(this, "Please select an image before sending the cocktail.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:8000/cocktail/"; // url para crear cocktail de la api del proyecto
        // Obtengo el token de sesión de las SharedPreferences:
        SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
        String sessionToken = preferences.getString("SessionToken", null);
        if (sessionToken == null) {
            Toast.makeText(this, "No session token found. You must log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validamos campos antes de enviar
        if (cocktailName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "You must enter the name of the cocktail.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (instructions.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "You must enter the instructions.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (glass.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "You must enter the type of glass.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (base64Image == null) {
            Toast.makeText(this, "You must load the image for your cocktail.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto JSON
        JSONObject cocktailData = new JSONObject();
        try {
            cocktailData.put("nombre", cocktailName.getText().toString().trim());
            cocktailData.put("instrucciones", instructions.getText().toString().trim());
            cocktailData.put("vaso", glass.getText().toString().trim());
            cocktailData.put("imagen", "data:image/jpeg;base64," + base64Image); // Imagen en base64

            // Crear lista de ingredientes
            JSONArray ingredients = new JSONArray();
            addIngredient(ingredients, ingredient1, cantidad1);
            addIngredient(ingredients, ingredient2, cantidad2);
            addIngredient(ingredients, ingredient3, cantidad3);
            addIngredient(ingredients, ingredient4, cantidad4);
            addIngredient(ingredients, ingredient5, cantidad5);
            addIngredient(ingredients, ingredient6, cantidad6);
            addIngredient(ingredients, ingredient7, cantidad7);
            addIngredient(ingredients, ingredient8, cantidad8);
            cocktailData.put("ingredientes", ingredients);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating cocktail data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviar los datos usando Volley
        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                cocktailData,
                response -> {
                    Toast.makeText(this, "Cocktail sent successfully.", Toast.LENGTH_SHORT).show();

                    //Volver a un fragment usando BANDERAS
                    //Lo hago así porque el fragemt está en otra activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("goToProfile", true); // Enviar flag para abrir PerfilFragment
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish(); // Cierra RecetasActivity


                },
                error -> {
                    if (error.networkResponse != null) {
                        String errorData = new String(error.networkResponse.data);
                        Toast.makeText(this, "Error: " + errorData, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error sending: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("SessionToken", sessionToken);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    // Método auxiliar para agregar ingredientes al JSON
    private void addIngredient(JSONArray ingredients, EditText ingredientField, EditText quantityField) throws JSONException {
        String ingredient = ingredientField.getText().toString().trim();
        String quantity = quantityField.getText().toString().trim();
        if (!ingredient.isEmpty() && !quantity.isEmpty()) {
            JSONObject ingredientObject = new JSONObject();
            ingredientObject.put("nombreIngrediente", ingredient);
            ingredientObject.put("cantidadIngrediente", quantity);
            ingredients.put(ingredientObject);
        }
    }
}

