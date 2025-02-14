package com.example.proyec;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText edtUsuario, edtPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Configurar el listener del botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuario = edtUsuario.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (usuario.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    validarUsuario("http://10.2.4.71/fin2/web_servise/webService.php", usuario, password);
                }
            }
        });
    }

    private void validarUsuario(String URL, final String username, final String password) {
        Log.d("LOGIN", "Conectando a: " + URL);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LOGIN_RESPONSE", "Respuesta del servidor: " + response);

                        if (response == null || response.trim().isEmpty()) {
                            Toast.makeText(MainActivity.this, "Error: Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.optString("status", "error");

                            if ("success".equals(status)) {
                                if (!jsonResponse.has("user")) {
                                    throw new JSONException("No se encontró la clave 'user' en la respuesta JSON");
                                }

                                JSONObject userObject = jsonResponse.getJSONObject("user");

                                // Convertir UserId a int correctamente
                                int userId = userObject.optInt("UserId", -1);
                                String username = userObject.optString("Username", "Desconocido");
                                String email = userObject.optString("email", "No disponible");

                                if (userId == -1) {
                                    Log.e("LOGIN_ERROR", "UserId inválido en JSON");
                                    Toast.makeText(MainActivity.this, "Error al obtener UserId", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Guardar en SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("UserId", userId); // Guardar como int
                                editor.putString("Username", username);
                                editor.putString("Email", email);
                                editor.apply();

                                Log.d("LOGIN_SUCCESS", "Usuario: " + username + ", ID: " + userId);
                                Toast.makeText(MainActivity.this, "Bienvenido " + username, Toast.LENGTH_SHORT).show();

                                // Enviar datos a productos.java con Intent
                                Intent intent = new Intent(MainActivity.this, productos.class);
                                intent.putExtra("UserId", userId);
                                intent.putExtra("Username", username);
                                intent.putExtra("Email", email);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish(); // Cierra MainActivity después de iniciar productos
                            } else {
                                String message = jsonResponse.optString("message", "Error desconocido");
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                            Log.e("LOGIN_ERROR", "Error al parsear JSON: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LOGIN_ERROR", "Error de conexión: ", error);
                        Toast.makeText(MainActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("username", username);
                parametros.put("password", password);
                return parametros;
            }
        };

        // Configurar y agregar la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}