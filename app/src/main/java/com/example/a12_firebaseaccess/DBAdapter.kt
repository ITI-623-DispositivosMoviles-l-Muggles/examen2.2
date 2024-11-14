package com.example.a12_firebaseaccess

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBAdapter(context: Context) : SQLiteOpenHelper(context, DB_NOMBRE, null, DB_VERSION) {

    companion object {
        const val DB_NOMBRE = "productos_db"
        const val DB_VERSION = 1
        const val DB_TABLA_ORDEN = "ordenes"
        const val PRODUCTO_ID = "productoID"
        const val PRODUCTO_NOMBRE = "productoNombre"
        const val PRECIO_UNITARIO = "precioUnitario"
        const val CANTIDAD = "cantidad"
        const val DESCUENTO = "descuento"
        const val SUBTOTAL = "subtotal"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $DB_TABLA_ORDEN (" +
                "$PRODUCTO_ID INTEGER PRIMARY KEY, " +
                "$PRODUCTO_NOMBRE TEXT NOT NULL, " +
                "$PRECIO_UNITARIO REAL NOT NULL, " +
                "$CANTIDAD INTEGER NOT NULL, " +
                "$DESCUENTO REAL NOT NULL, " +
                "$SUBTOTAL REAL NOT NULL)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $DB_TABLA_ORDEN")
        onCreate(db)
    }

    // Método para insertar productos en la orden
    fun insertarProductoEnOrden(productoID: String, nombre: String, precioUnitario: Double, cantidad: Int, descuento: Double): Long {
        val valores = ContentValues().apply {
            put(PRODUCTO_ID, productoID)
            put(PRODUCTO_NOMBRE, nombre)
            put(PRECIO_UNITARIO, precioUnitario)
            put(CANTIDAD, cantidad)
            put(DESCUENTO, descuento)
            put(SUBTOTAL, calcularSubtotal(precioUnitario, cantidad, descuento))
        }
        val db = writableDatabase
        return db.insert(DB_TABLA_ORDEN, null, valores)
    }

    // Calcular subtotal
    fun calcularSubtotal(precioUnitario: Double, cantidad: Int, descuento: Double): Double {
        return (precioUnitario * cantidad) * (1 - descuento)
    }

    // Obtener todos los productos de la orden
    fun obtenerProductosOrden(): Cursor {
        val db = readableDatabase
        return db.query(DB_TABLA_ORDEN, null, null, null, null, null, null)
    }

    // Borrar productos de la orden
    fun borrarProductosOrden() {
        val db = writableDatabase
        db.delete(DB_TABLA_ORDEN, null, null)
    }

    // Eliminar todos los productos de la base de datos (para cancelar la compra)
    fun eliminarTodosLosProductos() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM productos")
        db.close()
    }
}

