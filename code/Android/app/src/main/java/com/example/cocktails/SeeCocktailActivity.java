package com.example.cocktails;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class SeeCocktailActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private TextView textViewName;
    private TextView textViewIngredients;
    private TextView textViewInstructions;
    private TextView textViewGlass;
    private ImageView imageViewCocktail;
    private ImageButton btnShowIngredients;
    private ImageButton btnShowIntructions;
    private String fullIngredientsList;
    private String fullIntructionsList;
    private ImageButton buttonFavorite;
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private ProgressDialog progressDialog;
    private String cocktailImageUrl;
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail_see);

        textViewName = findViewById(R.id.textViewName);
        textViewIngredients = findViewById(R.id.textViewIngredients);
        textViewInstructions = findViewById(R.id.textViewInstructions);
        textViewGlass = findViewById(R.id.textViewGlass);
        imageViewCocktail = findViewById(R.id.imageViewCocktail);
        btnShowIngredients = findViewById(R.id.btnShowIngredients);
        btnShowIntructions = findViewById(R.id.btnShowInstructions);
        requestQueue = Volley.newRequestQueue(this);

        fullIngredientsList = textViewIngredients.getText().toString();
        fullIntructionsList = textViewInstructions.getText().toString();

        if (fullIngredientsList.length() > 100) {
            textViewIngredients.setText(fullIngredientsList.substring(0, 100) + "...");
            textViewInstructions.setText(fullIngredientsList.substring(0, 100) + "...");
        }

        btnShowIngredients.setOnClickListener(v -> showDialog(fullIngredientsList, "Ingredients"));
        btnShowIntructions.setOnClickListener(v -> showDialog(fullIntructionsList, "Instructions"));

        obtenerDatosCocktail(getIntent().getStringExtra("nombre"));

        buttonFavorite = findViewById(R.id.buttonFavorite);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false);

        buttonFavorite.setOnClickListener(v -> {
            if (dataLoaded) {
                checkIfFavorite(new OnFavoriteCheckedListener() {
                    @Override
                    public void onFavoriteChecked(boolean isFavorite) {
                        if (isFavorite) {
                            deleteFavorite(); // Eliminar de favoritos
                        } else {
                            addFavorite(); // Agregar a favoritos
                        }
                    }
                });
            }
        });
    }

    private void obtenerDatosCocktail(String nombreCocktail) {
        String url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + nombreCocktail;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray drinks = response.getJSONArray("drinks");
                            if (drinks.length() > 0) {
                                JSONObject cocktail = drinks.getJSONObject(0);
                                mostrarDatosCocktail(cocktail);
                            } else {
                                Toast.makeText(SeeCocktailActivity.this, "No se encontro el cóctel", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(SeeCocktailActivity.this, "Error al parsear la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SeeCocktailActivity.this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);

    }

    private void mostrarDatosCocktail(JSONObject cocktail) throws JSONException {

        String name = cocktail.getString("strDrink");
        cocktailImageUrl = cocktail.getString("strDrinkThumb");
        String instructions = cocktail.getString("strInstructions");
        String glass = cocktail.getString("strGlass");

        //Obtener ingredientes
        StringBuilder ingredients = new StringBuilder();
        for (int i = 1; i < 15; i++) {
            String ingredient = cocktail.optString("strIngredient" + i, "");
            String measure = cocktail.optString("strMeasure" + i, "");
            if (!ingredient.equals("null") && !ingredient.isEmpty()) {
                ingredients.append(ingredient);
                if (!measure.equals("null") && !measure.isEmpty()) {
                    ingredients.append(" - ").append(measure);
                }
                ingredients.append("\n");
            }
        }

        // Mostrar los datos
        Picasso.get()
                .load(cocktailImageUrl)
                .transform(new com.squareup.picasso.Transformation() {
                    @Override
                    public Bitmap transform(Bitmap source) {
                        // Redondear la imagen
                        int diameter = Math.min(source.getWidth(), source.getHeight());
                        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(output);

                        final Paint paint = new Paint();
                        final Rect rect = new Rect(0, 0, diameter, diameter);
                        paint.setAntiAlias(true);  // Asegura que el borde sea suave

                        // Dibuja un círculo recortado
                        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);

                        // Recorta la imagen dentro del círculo
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                        canvas.drawBitmap(source, new Rect(0, 0, source.getWidth(), source.getHeight()), rect, paint);

                        // Añade un borde alrededor del círculo
                        paint.setXfermode(null);
                        paint.setColor(0xFF000000);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(4);  // Grosor del borde
                        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);
                        source.recycle();
                        return output;
                    }

                    @Override
                    public String key() {
                        return "circleTransformation()";  // Identificador para la transformación
                    }
                })
                .into(imageViewCocktail);
        textViewName.setText(name);
        textViewIngredients.setText(ingredients.toString());
        textViewInstructions.setText(instructions);
        textViewGlass.setText(glass);
        fullIngredientsList = (ingredients.toString());
        fullIntructionsList = (instructions.toString());

        dataLoaded = true;

        // Verificar favoritos solo después de cargar los datos
        checkIfFavorite(new OnFavoriteCheckedListener() {
            @Override
            public void onFavoriteChecked(boolean isFavorite) {
                updateFavoriteButton(isFavorite);
            }
        });
    }

    private void showDialog(String lista, String name) {
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

    public interface OnFavoriteCheckedListener {
        void onFavoriteChecked(boolean isFavorite);
    }

    private void checkIfFavorite(final OnFavoriteCheckedListener listener) {
        String url = BASE_URL + "/favoritos/cocktaildb/check/";
        String name = textViewName.getText().toString();

        // Añadir el nombre como parámetro de consulta
        url += "?name=" + name;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean isFavorite = response.getBoolean("is_favorite");
                        // Llamamos al listener con el resultado de la verificación
                        listener.onFavoriteChecked(isFavorite);
                        updateFavoriteButton(isFavorite);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SeeCocktailActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(SeeCocktailActivity.this, "Error al verificar favoritos", Toast.LENGTH_SHORT).show();
                    Log.e("CheckFavorite", "Error: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                return headers;
            }
        };

        requestQueue.add(request);

    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            buttonFavorite.setImageResource(R.drawable.icon_favorite); // Favorito
            buttonFavorite.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            buttonFavorite.setImageResource(R.drawable.icon_favorite_border);
            buttonFavorite.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);// No favorito
        }
    }

    private void addFavorite() {
        progressDialog.show();

        String url = BASE_URL + "/favoritos/cocktaildb/";
        String name = textViewName.getText().toString();
        String imageUrl = cocktailImageUrl;
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("name", name);
            requestBody.put("cocktaildb_image_url", imageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }

        Log.i("Request", "addFavorite: " + requestBody.toString());
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    progressDialog.dismiss();
                    try {
                        Log.d("Response", "Respuesta del servidor: " + response.toString());
                        Toast.makeText(SeeCocktailActivity.this, "Cóctel añadido a favoritos", Toast.LENGTH_SHORT).show();
                        updateFavoriteButton(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SeeCocktailActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(SeeCocktailActivity.this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void deleteFavorite() {
        progressDialog.show();

        String url = BASE_URL + "/favoritos/cocktaildb/";
        String name = textViewName.getText().toString();
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    progressDialog.dismiss();
                    try {
                        Log.d("Response", "Respuesta del servidor: " + response.toString());
                        Toast.makeText(SeeCocktailActivity.this, "Cóctel eliminado de favoritos", Toast.LENGTH_SHORT).show();
                        updateFavoriteButton(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SeeCocktailActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(SeeCocktailActivity.this, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", obtenerToken());
                return headers;
            }
        };
        requestQueue.add(request);
        }

        private String obtenerToken () {
            SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
            String token = preferences.getString("SessionToken", null);
            Log.i("TOKEN", token != null ? token : "null");
            return token;
        }
    }