package com.example.proyec;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class ResumenCompraActivity extends AppCompatActivity {

    private TextView txtUserId, txtUsername, txtEmail, txtTotalCompra;
    private RecyclerView recyclerViewProductos;
    private ProductoAdapter productoAdapter;
    private Button btnCancelar, btnComprar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_compra);

        // Referencias UI
        txtUserId = findViewById(R.id.txtUserId);
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        txtTotalCompra = findViewById(R.id.txtTotalCompra);
        recyclerViewProductos = findViewById(R.id.recyclerViewProductos);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnComprar = findViewById(R.id.btnComprar);

        // Configuración del RecyclerView
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(this));
        productoAdapter = new ProductoAdapter(new ArrayList<>(), this);
        recyclerViewProductos.setAdapter(productoAdapter);

        // Recibir datos del usuario y productos desde la actividad anterior
        Intent intent = getIntent();
        int userId = intent.getIntExtra("UserId", -1);
        String username = intent.getStringExtra("Username");
        String email = intent.getStringExtra("Email");
        List<Producto> productos = (List<Producto>) intent.getSerializableExtra("Productos");
        String totalCompra = intent.getStringExtra("TotalCompra");

        // Mostrar datos del usuario
        txtUserId.setText("ID: " + (userId != -1 ? userId : "No recibido"));
        txtUsername.setText("Usuario: " + (username != null ? username : "Desconocido"));
        txtEmail.setText("Email: " + (email != null ? email : "No disponible"));

        // Filtrar productos con cantidad ingresada
        List<Producto> productosConCantidad = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.getCantidad() > 0) {
                productosConCantidad.add(producto);
            }
        }

        // Mostrar productos con cantidad ingresada
        productoAdapter = new ProductoAdapter(productosConCantidad, this);
        recyclerViewProductos.setAdapter(productoAdapter);

        // Mostrar el total de la compra
        txtTotalCompra.setText("Total a pagar: $" + totalCompra);

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> finish());

        // Botón Comprar
        btnComprar.setOnClickListener(v -> {
            // Crear un JSONObject con los datos de la compra
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("id_Persona", userId);
                jsonBody.put("cli_PagoTotal", totalCompra);

                // Crear un JSONArray para los productos
                JSONArray productosArray = new JSONArray();
                for (Producto producto : productosConCantidad) {
                    JSONObject productoJson = new JSONObject();
                    productoJson.put("id_Almacen", producto.getId());
                    productoJson.put("per_Cantidad", producto.getCantidad());
                    productosArray.put(productoJson);
                }
                jsonBody.put("productos", productosArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Enviar los datos al web service
            enviarDatosAlServidor(jsonBody);
        });
    }

    private void enviarDatosAlServidor(JSONObject jsonBody) {
        String url = "http://192.168.100.57/fin2/web_servise/procesar_compra.php"; // url envio

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            Toast.makeText(ResumenCompraActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (status.equals("success")) {
                                // Cerrar la actividad si la compra fue exitosa
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ResumenCompraActivity.this, "Error al enviar los datos", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }
}