package com.example.a12_firebaseaccess.ui.users

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a12_firebaseaccess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SignupActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    private lateinit var txtRNombre: EditText
    private lateinit var txtREmail: EditText
    private lateinit var txtRContra: EditText
    private lateinit var txtRreContra: EditText
    private lateinit var txtRCustomerID: EditText
    private lateinit var txtRContactTitle: EditText
    private lateinit var btnRegistrarU: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        txtRNombre = findViewById(R.id.txtRNombre)
        txtREmail = findViewById(R.id.txtREmail)
        txtRContra = findViewById(R.id.txtRContra)
        txtRreContra = findViewById(R.id.txtRreContra)
        txtRCustomerID = findViewById(R.id.txtRCustomerID)
        txtRContactTitle = findViewById(R.id.txtRContactTitle)
        btnRegistrarU = findViewById(R.id.btnRegistrarU)

        btnRegistrarU.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = txtRNombre.text.toString()
        val email = txtREmail.text.toString()
        val contra = txtRContra.text.toString()
        val reContra = txtRreContra.text.toString()
        val customerID = txtRCustomerID.text.toString()
        val contactTitle = txtRContactTitle.text.toString()

        if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty() || reContra.isEmpty() || customerID.isEmpty() || contactTitle.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
        } else {
            if (contra == reContra) {
                auth.createUserWithEmailAndPassword(email, contra)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val customerUpdates = hashMapOf(
                                "ContactName" to nombre,
                                "ContactTitle" to contactTitle
                            )

                            db.collection("Customers").document(customerID).update(customerUpdates as Map<String, Any>)
                                .addOnSuccessListener {
                                    // Obtener los datos adicionales del cliente
                                    db.collection("Customers").document(customerID).get()
                                        .addOnSuccessListener { document ->
                                            if (document != null && document.exists()) {
                                                val shipVia = document.getLong("ShipVia")?.toInt() ?: 0
                                                val shipName = document.getString("ShipName") ?: ""
                                                val shipAddress = document.getString("ShipAddress") ?: ""
                                                val shipCity = document.getString("ShipCity") ?: ""
                                                val shipRegion = document.getString("ShipRegion") ?: ""
                                                val shipPostalCode = document.getString("ShipPostalCode") ?: ""
                                                val shipCountry = document.getString("ShipCountry") ?: ""

                                                // Guardar en la colección datosUsuarios
                                                val userData = hashMapOf(
                                                    "idemp" to task.result?.user?.uid,
                                                    "usuario" to nombre,
                                                    "email" to email,
                                                    "ultAcceso" to Date().toString(),
                                                    "customerID" to customerID
                                                )

                                                db.collection("datosUsuarios").add(userData)
                                                    .addOnSuccessListener {
                                                        // Guardar en SharedPreferences
                                                        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
                                                        val editor = prefe.edit()
                                                        editor.putString("email", email)
                                                        editor.putString("contra", contra)
                                                        editor.putString("customerID", customerID)
                                                        editor.putString("contactTitle", contactTitle)  // Guardar el ContactTitle
                                                        editor.putInt("shipVia", shipVia)
                                                        editor.putString("shipName", shipName)
                                                        editor.putString("shipAddress", shipAddress)
                                                        editor.putString("shipCity", shipCity)
                                                        editor.putString("shipRegion", shipRegion)
                                                        editor.putString("shipPostalCode", shipPostalCode)
                                                        editor.putString("shipCountry", shipCountry)
                                                        editor.apply()

                                                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                                                        val intent = Intent(this, LoginActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(this, "Error al registrar usuario en Firestore", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                Toast.makeText(this, "No se encontraron datos del cliente", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al obtener datos del cliente", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        } else {
                            Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
