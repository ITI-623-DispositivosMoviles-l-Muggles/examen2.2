package com.example.a12_firebaseaccess.ui.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.a12_firebaseaccess.R
import com.example.a12_firebaseaccess.entities.cls_product

class ProductAAdapter(
    private val context: Context,
    private val dataModalArrayList: ArrayList<cls_product> // Lista de productos
) : RecyclerView.Adapter<ProductAAdapter.ProductViewHolder>() {

    // ViewHolder que representa un elemento de la lista
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productID: TextView = itemView.findViewById(R.id.codigo_producto)
        val nombreProducto: TextView = itemView.findViewById(R.id.nombre_producto)
        val unitPrice: TextView = itemView.findViewById(R.id.precio_unitario)
        val cantidadProducto: TextView = itemView.findViewById(R.id.cantidad_producto)
        val subtotalProducto: TextView = itemView.findViewById(R.id.subtotal_producto)
        val discontinued: TextView = itemView.findViewById(R.id.descuento_producto)
    }

    // Este método infla la vista para cada elemento en la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false)
        return ProductViewHolder(itemView)
    }

    // Este método enlaza los datos del producto con las vistas
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val producto = dataModalArrayList[position]

        // Aseguramos que los datos no sean nulos y asignamos los valores correspondientes a las vistas
        holder.productID.text = producto.productID.takeIf { it.isNotEmpty() } ?: "Sin ID"
        holder.nombreProducto.text = producto.productName.takeIf { it.isNotEmpty() } ?: "Sin nombre"
        holder.unitPrice.text = producto.unitPrice.toString()
        holder.cantidadProducto.text = producto.unitsInStock.toString()
        holder.discontinued.text = if (producto.discontinued == "1") "Descontinuado" else "Disponible"

        // Evento de clic
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Producto clickeado: ${producto.productName}", Toast.LENGTH_SHORT).show()
        }
    }

    // Devuelve el número total de elementos en la lista
    override fun getItemCount(): Int {
        return dataModalArrayList.size
    }
}
