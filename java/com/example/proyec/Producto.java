package com.example.proyec;

import java.io.Serializable;
import java.math.BigDecimal;

public class Producto implements Serializable {
    private String nombre;
    private BigDecimal precio;
    private int stock, id;
    private String imagenUrl;
    private int cantidad;

    public Producto(int id, String nombre, double precio, int stock, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.precio = BigDecimal.valueOf(precio);
        this.stock = stock;
        this.imagenUrl = imagenUrl;
        this.cantidad = 0;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getImagenUrl() { return imagenUrl; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}