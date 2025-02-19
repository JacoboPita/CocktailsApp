package com.example.cocktails;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsuarioL, etContrasenaL;
    private static final String URL = "http://10.0.2.2:8000/sessions"; // Url para iniciar sesión en endpoint loguin
    private RequestQueue queue;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        queue = Volley.newRequestQueue(this);

        try {
            etUsuarioL = findViewById(R.id.etUsuarioL);
            etContrasenaL = findViewById(R.id.etContrasenaL);
            Button btnIngresarL = findViewById(R.id.btnIngresarL);
            TextView lblRegistrar = findViewById(R.id.lblRegistrar);

            if (btnIngresarL != null) {
                btnIngresarL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validarDatos();
                    }
                });
            }
            if (lblRegistrar != null) {
                lblRegistrar.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            Log.e("LoginActivity", "Error en onCreate: " + e.getMessage());
            Toast.makeText(this, "Error al inicializar la aplicación", Toast.LENGTH_LONG).show();
        }
    }

    private void validarDatos() {
        if (etUsuarioL == null || etContrasenaL == null) return;

        String usuario = Objects.requireNonNull(etUsuarioL.getText()).toString().trim();
        String contrasena = Objects.requireNonNull(etContrasenaL.getText()).toString().trim();

        if (TextUtils.isEmpty(usuario)) {
            etUsuarioL.setError("Ingrese un usuario");
            return;
        }

        if (TextUtils.isEmpty(contrasena)) {
            etContrasenaL.setError("Ingrese contraseña");
            return;
        }

        if (contrasena.length() < 6) {
            etContrasenaL.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        realizarLogin(usuario, contrasena);
    }

    private void realizarLogin(String usuario, String contrasena) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", usuario);
            requestBody.put("password", contrasena);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    //BASE_URL + "sessions", // a mi asinome funciona
                    URL,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                guardarToken(response);
                                Toast.makeText(context, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            } catch (JSONException e) {
                                Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String mensaje = "Error de conexión";
                            if (error.networkResponse != null) {
                                if (error.networkResponse.statusCode == 401) {
                                    mensaje = "Credenciales incorrectas";
                                }
                            }
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
                        }
                    }
            ) {/**
             @Override
             public Map<String, String> getHeaders() {
             Map<String, String> headers = new HashMap<>();
             headers.put("Content-Type", "application/json");
             headers.put("SessionToken", obtenerToken()); // Esto solo si el login requiere un token previo
             return headers;
             }*/

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                String token = obtenerToken();
                if (token != null) {
                    headers.put("SessionToken", token);
                }

                return headers;
            }


            };

            queue.add(request);
        } catch (JSONException e) {
            Toast.makeText(context, "Error al crear la petición", Toast.LENGTH_LONG).show();
        }
    }

    private String obtenerToken() {
        SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
        return preferences.getString("SessionToken", null);
    }

    private void guardarToken(JSONObject requestBody) throws JSONException {
        SharedPreferences preferences = getSharedPreferences("COCKTAILS_APP_PREFS", MODE_PRIVATE);
        Log.i("GUARDAR TOKEN", "guardarToken: " + requestBody.toString());
        preferences.edit().putString("SessionToken", requestBody.getString("token")).apply();
        preferences.edit().putString("USERNAME", requestBody.getString("username")).apply();
        preferences.edit().putString("NAME", requestBody.getString("name")).apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
