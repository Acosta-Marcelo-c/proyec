package com.example.proyec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class productos extends AppCompatActivity implements ProductoAdapter.OnTotalChangedListener {
    private TextView txtUserId, txtUsername, txtEmail, txtTotalCompra;
    private RecyclerView recyclerViewProductos;
    private ProductoAdapter productoAdapter;
    private List<Producto> listaProductos;
    private Button btnCancelar, btnEnviar;

    private static final String URL = "http://10.2.4.71/fin2/web_servise/listar_producto.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        // Referencias UI
        txtUserId = findViewById(R.id.txtUserId);
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        txtTotalCompra = findViewById(R.id.txtTotalCompra);
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnEnviar = findViewById(R.id.btnEnviar);

        // Configuración del RecyclerView
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        productoAdapter = new ProductoAdapter(listaProductos, this);
        recyclerViewProductos.setAdapter(productoAdapter);

        // Implementar el listener
        productoAdapter.setOnTotalChangedListener(this);

        // Recibir datos del usuario desde MainActivity
        Intent intent = getIntent();
        int userId = intent.getIntExtra("UserId", -1);
        String username = intent.getStringExtra("Username");
        String email = intent.getStringExtra("Email");

        txtUserId.setText("ID: " + (userId != -1 ? userId : "No recibido"));
        txtUsername.setText("Usuario: " + (username != null ? username : "Desconocido"));
        txtEmail.setText("Email: " + (email != null ? email : "No disponible"));

        cargarProductos();

        // Botón cancelar
        btnCancelar.setOnClickListener(v -> finish());

        // Botón enviar
        btnEnviar.setOnClickListener(v -> {
            BigDecimal totalCompra = productoAdapter.calcularTotal();
            Toast.makeText(productos.this, "Total: $" + totalCompra.setScale(2, BigDecimal.ROUND_HALF_UP), Toast.LENGTH_SHORT).show();

            // Crear un Intent para iniciar la nueva actividad
            Intent resumenIntent = new Intent(productos.this, ResumenCompraActivity.class);

            // Pasar los datos del usuario
            resumenIntent.putExtra("UserId", userId);
            resumenIntent.putExtra("Username", username);
            resumenIntent.putExtra("Email", email);

            // Pasar la lista de productos con cantidades ingresadas
            resumenIntent.putExtra("Productos", new ArrayList<>(listaProductos));

            // Pasar el total de la compra
            resumenIntent.putExtra("TotalCompra", totalCompra.toString());

            // Iniciar la nueva actividad
            startActivity(resumenIntent);
        });
    }

    @Override
    public void onTotalChanged() {
        BigDecimal totalCompra = productoAdapter.calcularTotal();
        txtTotalCompra.setText("Total a pagar: $" + totalCompra.setScale(2, BigDecimal.ROUND_HALF_UP)); // Formatear a 2 decimales
    }

    private void cargarProductos() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("PRODUCTOS", "Respuesta del servidor: " + response.toString());

                            if (response.optString("status", "error").equals("success")) {
                                JSONArray productosArray = response.getJSONArray("productos");
                                listaProductos.clear();

                                for (int i = 0; i < productosArray.length(); i++) {
                                    JSONObject productoJSON = productosArray.getJSONObject(i);
                                    int id = productoJSON.optInt("id_Almacen", 0);
                                    String nombre = productoJSON.optString("alm_Nombre", "Sin nombre");
                                    double precio = productoJSON.optDouble("alm_PrecioVenta", 0.0);
                                    int stock = productoJSON.optInt("alm_Stock", 0);
                                    String imagenUrl = productoJSON.optString("alm_Imagen", "");

                                    listaProductos.add(new Producto(id, nombre, precio, stock, imagenUrl));
                                }

                                productoAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(productos.this, "No se encontraron productos", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(productos.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                            Log.e("PRODUCTOS_ERROR", "Error parsing JSON: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error de conexión";
                        if (error.networkResponse != null) {
                            errorMessage += " (Código: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(productos.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("PRODUCTOS_ERROR", "Error de conexión: " + error.toString());
                    }
                });

        queue.add(request);
    }
}