package com.example.a12_firebaseaccess.ui.products

import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a12_firebaseaccess.DBAdapter
import com.example.a12_firebaseaccess.R

class ProductCaptureActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_capture)

        dbHelper = DBAdapter(this)

        val cantidadEditText = findViewById<EditText>(R.id.editTextCantidad)
        val descuentoEditText = findViewById<EditText>(R.id.editTextDescuento)
        val agregarButton = findViewById<Button>(R.id.btnAgregar)

        agregarButton.setOnClickListener {
            val cantidad = cantidadEditText.text.toString().toIntOrNull()
            val descuento = descuentoEditText.text.toString().toDoubleOrNull()

            if (cantidad != null && descuento != null) {
                // Suponemos que ya tenemos los datos del producto (ID, nombre, precio) en variables
                val productoID = "13" // Ejemplo de ProductoID
                val nombre = "Konbu"
                val precioUnitario = 6.0

                dbHelper.insertarProductoEnOrden(productoID, nombre, precioUnitario, cantidad, descuento)
                Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                finish() // Volver a la pantalla anterior
            } else {
                Toast.makeText(this, "Por favor, ingresa cantidad y descuento válidos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
