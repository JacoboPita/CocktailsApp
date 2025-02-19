package com.example.cocktails;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    private EditText etNombre, etUsuario, etContrasena, etConfirmContrasena;
    private RequestQueue queue;
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private static final String TAG = "RegistroActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initViews();
        queue = Volley.newRequestQueue(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario...");
        progressDialog.setCancelable(false);
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmContrasena = findViewById(R.id.etConfirmContrasena);

        findViewById(R.id.btnRgistrar).setOnClickListener(v -> registrarUsuario());
        findViewById(R.id.lblLoginR).setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registrarUsuario() {
        if (!validarCampos()) return;

        progressDialog.show();
        String url = BASE_URL + "/user";

        JSONObject requestBody = crearRequestBody();
        if (requestBody == null) {
            progressDialog.dismiss();
            return;
        }

        Log.d(TAG, "Enviando petición a: " + url);
        Log.d(TAG, "Body: " + requestBody.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    progressDialog.dismiss();
                    try {
                        Log.d(TAG, "Respuesta del servidor: " + response.toString());
                        SharedPreferences prefs = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
                        prefs.edit().putString("USERNAME", requestBody.getString("username")).apply();
                        prefs.edit().putString("NAME", requestBody.getString("name")).apply();
                        // Guardar el usuario
                        String token = response.getString("token");
                        // Guardar el token
                        prefs.edit().putString("SessionToken", token).apply();

                        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta", e);
                        Toast.makeText(this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 segundos de timeout
                1,     // Sin reintentos
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private JSONObject crearRequestBody() {
        try {
            JSONObject body = new JSONObject();
            body.put("name", etNombre.getText().toString().trim());
            body.put("username", etUsuario.getText().toString().trim());
            body.put("password", etContrasena.getText().toString().trim());
            Log.d(TAG, "Request body: " + body);
            return body;
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON", e);
            Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String confirmarContrasena = etConfirmContrasena.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es requerido");
            return false;
        }

        if (usuario.isEmpty()) {
            etUsuario.setError("El usuario es requerido");
            return false;
        }

        if (contrasena.isEmpty()) {
            etContrasena.setError("La contraseña es requerida");
            return false;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            etConfirmContrasena.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}