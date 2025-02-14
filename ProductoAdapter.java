package com.example.proyec;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {
    private List<Producto> listaProductos;
    private Context context;
    private OnTotalChangedListener onTotalChangedListener;

    // Interfaz para notificar cambios en el total
    public interface OnTotalChangedListener {
        void onTotalChanged();
    }

    // Método para establecer el listener
    public void setOnTotalChangedListener(OnTotalChangedListener listener) {
        this.onTotalChangedListener = listener;
    }

    public ProductoAdapter(List<Producto> listaProductos, Context context) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        if (producto != null) {
            holder.txtId.setText("Id: " + producto.getId());
            holder.txtNombre.setText("Nombre: " + producto.getNombre());
            holder.txtPrecio.setText("Precio unidad: $" + producto.getPrecio().toString()); // Mostrar el precio como String
            holder.txtStock.setText("Stock: " + producto.getStock());

            // Cargar la imagen con Glide
            String imagenUrlCompleta = "http://10.2.4.71/fin2/almacen/img_productos/" + producto.getImagenUrl();
            Glide.with(context)
                    .load(imagenUrlCompleta)
                    .error(R.drawable.imagen_predeterminada)
                    .into(holder.imgProducto);

            // Eliminar el TextWatcher anterior para evitar duplicados
            if (holder.textWatcher != null) {
                holder.edtCantidad.removeTextChangedListener(holder.textWatcher);
            }

            // Establecer la cantidad actual en el EditText
            holder.edtCantidad.setText(String.valueOf(producto.getCantidad()));

            // Crear un nuevo TextWatcher para manejar cambios en la cantidad
            holder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        try {
                            int cantidad = Integer.parseInt(s.toString());
                            if (cantidad > producto.getStock()) {
                                cantidad = producto.getStock();
                                holder.edtCantidad.setText(String.valueOf(cantidad));
                            }
                            // Calcular el total usando BigDecimal
                            BigDecimal total = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
                            holder.txtTotal.setText("$" + total.setScale(2, BigDecimal.ROUND_HALF_UP)); // Formatear a 2 decimales
                            producto.setCantidad(cantidad); // Actualiza la cantidad del producto

                            // Notificar que el total ha cambiado
                            if (onTotalChangedListener != null) {
                                onTotalChangedListener.onTotalChanged();
                            }
                        } catch (NumberFormatException e) {
                            holder.edtCantidad.setText("0");
                            producto.setCantidad(0); // Reiniciar la cantidad si hay un error
                            if (onTotalChangedListener != null) {
                                onTotalChangedListener.onTotalChanged();
                            }
                        }
                    } else {
                        producto.setCantidad(0); // Si el campo está vacío, la cantidad es 0
                        if (onTotalChangedListener != null) {
                            onTotalChangedListener.onTotalChanged();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            // Agregar el nuevo TextWatcher al EditText
            holder.edtCantidad.addTextChangedListener(holder.textWatcher);
        }
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    // Método para calcular el total de la compra
    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO; // Inicializar con BigDecimal
        for (Producto p : listaProductos) {
            BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(p.getCantidad()));
            total = total.add(subtotal);
        }
        return total;
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtNombre, txtPrecio, txtStock, txtTotal;
        EditText edtCantidad;
        ImageView imgProducto;
        TextWatcher textWatcher; // Campo para almacenar el TextWatcher

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtId);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            edtCantidad = itemView.findViewById(R.id.edtCantidad);
            imgProducto = itemView.findViewById(R.id.imgProducto);
        }
    }
}