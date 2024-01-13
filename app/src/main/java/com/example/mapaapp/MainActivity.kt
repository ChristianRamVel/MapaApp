package com.example.mapaapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

internal class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var botonVerListaUbicaciones: Button
    private lateinit var botonBorrarUbicaciones: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        botonVerListaUbicaciones = findViewById(R.id.botonVerListaUbicaciones)
        botonBorrarUbicaciones = findViewById(R.id.botonBorrarUbicaciones)
        botonVerListaUbicaciones.setOnClickListener {
            mandarUbicaciones()
        }

        botonBorrarUbicaciones.setOnClickListener {
            borrarUbicaciones()
            //refresca el mapa
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Carga las ubicaciones desde la base de datos
        val ubicaciones = cargarUbicacionesDesdeBaseDeDatos()
        //Recorre las ubicaciones y las muestra en el mapa
        for (ubicacion in ubicaciones) {
            val ubicacionRecibida = LatLng(ubicacion.latitud, ubicacion.longitud)
            mMap.addMarker(MarkerOptions()
                .position(ubicacionRecibida)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(ubicacion.descripcion))
        }

        //recogemos las coordenadas que nos llegan del intent
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)
        val descripcion = intent.getStringExtra("descripcion")
        // Se fijan unas coordenadas
        val ubicacionRecibida = LatLng(latitud, longitud)
        //Fija un marcador en la posición y con el texto que se indica.
        mMap.addMarker(MarkerOptions()
            .position(ubicacionRecibida)
            .title(descripcion))
        //Mueve la cámara a la latitud que se indica
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionRecibida))
        //Configuro como satélite
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        //Método que muestra la latitud y la longitud
        googleMap.setOnMapClickListener { latLng ->
            // Mostrar un Toast con la latitud y la longitud
            Toast.makeText(
                this,
                "Latitud: ${latLng.latitude}, Longitud: ${latLng.longitude}",
                Toast.LENGTH_LONG
            ).show()
            //Establezco un marcador con el evento
            googleMap.addMarker((MarkerOptions()
                .position(latLng)
                .title("Marcador en destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                .draggable(true))
        }

        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Marcador largo")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet("Teléfono: 983989784")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            guardarUbicacionEnBaseDeDatos(latLng.latitude, latLng.longitude, "Marcador largo")

        }

    }

    //metodo para guardar ubicacion en sqlite
    private fun guardarUbicacionEnBaseDeDatos(latitud: Double, longitud: Double, descripcion: String) {
        val dbHelper = LocationDbHelper(this)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LocationContract.LocationEntry.COLUMN_LATITUD, latitud)
            put(LocationContract.LocationEntry.COLUMN_LONGITUD, longitud)
            put(LocationContract.LocationEntry.COLUMN_DESCRIPCION, descripcion)
        }

        val newRowId = db?.insert(LocationContract.LocationEntry.TABLE_NAME, null, values)
    }

    //metodo para cargar ubicaciones desde sqlite
    private fun cargarUbicacionesDesdeBaseDeDatos(): List<Ubicacion> {
        val ubicaciones = mutableListOf<Ubicacion>()
        val dbHelper = LocationDbHelper(this)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            LocationContract.LocationEntry.COLUMN_LATITUD,
            LocationContract.LocationEntry.COLUMN_LONGITUD,
            LocationContract.LocationEntry.COLUMN_DESCRIPCION
        )
        val cursor = db.query(
            LocationContract.LocationEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        if (cursor != null) {
            with(cursor) {
                while (moveToNext()) {
                    val latitud = getDouble(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_LATITUD))
                    val longitud = getDouble(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_LONGITUD))
                    val descripcion = getString(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_DESCRIPCION))
                    ubicaciones.add(Ubicacion(latitud, longitud, descripcion))
                }
            }
        }
        return ubicaciones
    }

    //metodo para mandar con un intent las ubicaciones guardadas en sqlite
    private fun mandarUbicaciones() {
        val ubicaciones = cargarUbicacionesDesdeBaseDeDatos()
        val intent = this.packageManager.getLaunchIntentForPackage("com.example.aplicacion1")
        for (ubicacion in ubicaciones) {
            intent?.action = "com.example.aplicacion1.MOSTRAR_UBICACION"
            intent?.putExtra("latitud", ubicacion.latitud)
            intent?.putExtra("longitud", ubicacion.longitud)
            intent?.putExtra("descripcion", ubicacion.descripcion)
            startActivity(intent)
        }
    }

    //metodo para borrar ubicaciones de sqlite
    private fun borrarUbicaciones() {
        val dbHelper = LocationDbHelper(this)
        val db = dbHelper.writableDatabase
        db.delete(LocationContract.LocationEntry.TABLE_NAME, null, null)
    }
}

