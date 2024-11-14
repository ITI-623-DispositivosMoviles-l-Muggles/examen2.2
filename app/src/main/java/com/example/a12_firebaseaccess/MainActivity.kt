package com.example.a12_firebaseaccess

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12_firebaseaccess.entities.cls_product
import com.example.a12_firebaseaccess.ui.products.ProductCaptureActivity
import com.example.a12_firebaseaccess.ui.products.ProductAAdapter
import com.example.a12_firebaseaccess.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val valorIntentLogin = 1

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var productosRecyclerView: RecyclerView
    private lateinit var productosAdapter: ProductAAdapter
    private var auth = FirebaseAuth.getInstance()
    private var email: String? = null
    private var contra: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos la base de datos de Firestore
        db = FirebaseFirestore.getInstance()

        // RecyclerView para mostrar los productos
        productosRecyclerView = findViewById(R.id.recyclerViewProductos)
        productosRecyclerView.layoutManager = LinearLayoutManager(this)

        // Intentamos obtener los datos del usuario (email y contraseña) del almacenamiento local
        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
        email = prefe.getString("email", "")
        contra = prefe.getString("contra", "")

        // Si el email está vacío, redirigimos a la pantalla de login
        if (email.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, valorIntentLogin)
        } else {
            // Si hay datos de login almacenados, intentamos hacer el login automático.
            val uid: String = auth.uid.toString()
            if (uid == "null") {
                auth.signInWithEmailAndPassword(email.toString(), contra.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Autenticación correcta", Toast.LENGTH_SHORT).show()
                            obtenerDatos()  // Si el login es exitoso, obtenemos los datos.
                        } else {
                            Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivityForResult(intent, valorIntentLogin)
                        }
                    }
            } else {
                obtenerDatos()
            }
        }

        // Configuración del botón para agregar un nuevo producto
        findViewById<Button>(R.id.btnAgregarProducto).setOnClickListener {
            val intent = Intent(this, ProductCaptureActivity::class.java)
            startActivity(intent)
        }

        // Configuración del botón para aplicar la compra
        findViewById<Button>(R.id.btnAplicarCompra).setOnClickListener {
            aplicarCompra()
        }

        // Configuración del botón para cancelar la compra
        findViewById<Button>(R.id.btnCancelarCompra).setOnClickListener {
            cancelarCompra()
        }
    }

    private fun obtenerDatos() {
        val coleccion: ArrayList<cls_product> = ArrayList()
        val listaView: RecyclerView = findViewById(R.id.recyclerViewProductos)
        listaView.layoutManager = LinearLayoutManager(this)

        db.collection("Products").orderBy("ProductID")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d("MainActivity", document.id + " => " + document.data)

                        val productoID = document.data["ProductID"]?.toString() ?: "Producto ID desconocido"
                        val nombreProducto = document.data["ProductName"]?.toString() ?: "Producto desconocido"
                        val unitPrice = document.data["UnitPrice"]?.toString()?.toDoubleOrNull() ?: 0.0
                        val unitsInStock = document.data["UnitsInStock"]?.toString() ?: "Producto desconocido"
                        val unitsOnOrder = document.data["UnitsOnOrder"]?.toString() ?: "Producto desconocido"
                        val discontinued = document.data["Discontinued"]?.toString() ?: "0"

                        val producto = cls_product(
                            productoID,
                            nombreProducto,
                            unitPrice,
                            unitsInStock,
                            unitsOnOrder,
                            discontinued
                        )

                        coleccion.add(producto)
                    }

                    productosAdapter = ProductAAdapter(this, coleccion)
                    listaView.adapter = productosAdapter

                } else {
                    Log.w("MainActivity", "Error getting documents.", task.exception)
                }
            }
    }

    private fun aplicarCompra() {
        val productosSQLite = DBAdapter.obtenerProductosOrden()
        val firestore = FirebaseFirestore.getInstance()

        // Recorremos los productos de SQLite y los guardamos en Firestore
        for (producto in productosSQLite) {
            val order = hashMapOf(
                "codigo" to producto.codigo,
                "producto" to producto.nombre,
                "precioUnitario" to producto.precioUnitario,
                "cantidad" to producto.cantidad,
                "descuento" to producto.descuento,
                "subtotal" to producto.subtotal
            )

            firestore.collection("Orders").add(order)
                .addOnSuccessListener {
                    DBAdapter.borrarProductosOrden(producto.id)
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al agregar documento", e)
                }
        }

        Toast.makeText(this, "Compra aplicada correctamente.", Toast.LENGTH_SHORT).show()
    }

    private fun cancelarCompra() {
        val dbHelper = DBAdapter(this)

        dbHelper.eliminarTodosLosProductos()

        actualizarListaDeProductos()

        Toast.makeText(this, "Compra cancelada.", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarListaDeProductos() {
        val dbHelper = DBAdapter(this)
        val productos = dbHelper.obtenerProductos()

        // Actualizar el RecyclerView con la lista de productos vacía
        val adapter = ProductAAdapter(this, ArrayList(productos))
        recyclerView.adapter = adapter
    }

    // Si necesitas manejar el resultado del login (por ejemplo, si usas startActivityForResult):
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == valorIntentLogin && resultCode == RESULT_OK) {
            // Si el login fue exitoso, obtén los datos de nuevo
            email = data?.getStringExtra("email")
            contra = data?.getStringExtra("contra")
            if (!email.isNullOrEmpty() && !contra.isNullOrEmpty()) {
                // Intentamos hacer login automáticamente
                auth.signInWithEmailAndPassword(email!!, contra!!)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Autenticación correcta", Toast.LENGTH_SHORT).show()
                            obtenerDatos()
                        } else {
                            Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
