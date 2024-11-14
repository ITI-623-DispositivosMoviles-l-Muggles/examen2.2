package com.example.a12_firebaseaccess.ui.users

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.a12_firebaseaccess.R
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import com.google.firebase.auth.FirebaseAuth

const val valorIntentSignup = 1

class LoginActivity : AppCompatActivity() {
    var auth = FirebaseAuth.getInstance()
    private lateinit var btnAutenticar: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var txtRegister: TextView
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        btnAutenticar = findViewById(R.id.btnAutenticar)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtContra)
        txtRegister = findViewById(R.id.txtRegister)

        // Verificar si ya hay datos en SharedPreferences antes de realizar login
        val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)
        val storedEmail = prefe.getString("email", "")
        val storedContra = prefe.getString("contra", "")
        val storedCustomerID = prefe.getString("customerID", "")

        // Si ya hay datos guardados, completar los campos automáticamente
        if (!storedEmail.isNullOrEmpty() && !storedContra.isNullOrEmpty()) {
            txtEmail.setText(storedEmail)
            txtContra.setText(storedContra)
        }

        txtRegister.setOnClickListener {
            goToSignup()
        }

        btnAutenticar.setOnClickListener {
            if(txtEmail.text.isNotEmpty() && txtContra.text.isNotEmpty()){
                auth.signInWithEmailAndPassword(txtEmail.text.toString(), txtContra.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful){
                        val dt: Date = Date()

                        // Datos para actualizar el último acceso del usuario
                        val user = hashMapOf(
                            "ultAcceso" to dt.toString()
                        )

                        // Buscar en la base de datos el usuario con el idemp
                        db.collection("datosUsuarios").whereEqualTo("idemp", it.result?.user?.uid.toString()).get()
                            .addOnSuccessListener { documentReference ->
                                documentReference.forEach { document ->
                                    db.collection("datosUsuarios").document(document.id).update(user as Map<String, Any>)
                                }

                                // Obtener los datos del cliente desde Firestore
                                if (!storedCustomerID.isNullOrEmpty()) {
                                    db.collection("Customers").document(storedCustomerID).get()
                                        .addOnSuccessListener { customerDoc ->
                                            if (customerDoc.exists()) {
                                                // Obtener los detalles del cliente
                                                val shipVia = customerDoc.getLong("ShipVia")?.toInt() ?: 0
                                                val shipName = customerDoc.getString("ShipName") ?: ""
                                                val shipAddress = customerDoc.getString("ShipAddress") ?: ""
                                                val shipCity = customerDoc.getString("ShipCity") ?: ""
                                                val shipRegion = customerDoc.getString("ShipRegion") ?: ""
                                                val shipPostalCode = customerDoc.getString("ShipPostalCode") ?: ""
                                                val shipCountry = customerDoc.getString("ShipCountry") ?: ""

                                                // Guardar los datos del cliente en SharedPreferences si han cambiado
                                                val editor = prefe.edit()
                                                editor.putInt("shipVia", shipVia)
                                                editor.putString("shipName", shipName)
                                                editor.putString("shipAddress", shipAddress)
                                                editor.putString("shipCity", shipCity)
                                                editor.putString("shipRegion", shipRegion)
                                                editor.putString("shipPostalCode", shipPostalCode)
                                                editor.putString("shipCountry", shipCountry)
                                                editor.apply()

                                                // Redirigir al usuario a la pantalla principal
                                                Intent().let {
                                                    setResult(Activity.RESULT_OK)
                                                    finish()
                                                }
                                            } else {
                                                Toast.makeText(this, "No se encontraron datos del cliente en Firestore", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al obtener los datos del cliente", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al actualizar los datos del usuario", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        showAlert("Error", "Al autenticar el usuario")
                    }
                }
            } else {
                showAlert("Error", "El correo electrónico y contraseña no pueden estar vacíos")
            }
        }
    }

    private fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, valorIntentSignup)
    }

    private fun showAlert(titu: String, mssg: String) {
        val diagMessage = AlertDialog.Builder(this)
        diagMessage.setTitle(titu)
        diagMessage.setMessage(mssg)
        diagMessage.setPositiveButton("Aceptar", null)

        val diagVentana: AlertDialog = diagMessage.create()
        diagVentana.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // validate control variables
        if (resultCode == Activity.RESULT_OK) {
            // call back to main activity
            Intent().let {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}
