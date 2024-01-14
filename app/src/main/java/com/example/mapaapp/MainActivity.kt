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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

internal class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var botonVerListaUbicaciones: Button
    private lateinit var botonBorrarUbicaciones: Button
    private var marcadorSeleccionado: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        botonVerListaUbicaciones = findViewById(R.id.botonVerListaUbicaciones)
        botonBorrarUbicaciones = findViewById(R.id.botonBorrarUbicaciones)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Muestra la ubicación recibida en el intent

        //Carga las ubicaciones desde la base de datos
        val ubicaciones = cargarUbicacionesDesdeBaseDeDatos()
        //Recorre las ubicaciones y las muestra en el mapa
        for (ubicacion in ubicaciones) {
            val ubicacionObtenidaDeBD = LatLng(ubicacion.latitud, ubicacion.longitud)
            mMap.addMarker(
                MarkerOptions()
                    .position(ubicacionObtenidaDeBD)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(ubicacion.descripcion)
            )
        }
        //la mostramos aqui porque al abrir el mapa se muestran todas las ubicaciones
        //primero cargamos la base de datos y luego mostramos la ubicacion recibida
        //y como se guarda en la base de datos al recibirla, la mostrariamos en el mapa igual
        //pero si borramos la base de datos tambien se borraria y se actualizaria el mapa
        //sin esta ubicacion tambien.
        mostrarUbicacionRecibida()

        //onclick
        googleMap.setOnMapClickListener { latLng ->
            // Mostrar un Toast con la latitud y la longitud
            Toast.makeText(
                this,
                "Latitud: ${latLng.latitude}, Longitud: ${latLng.longitude}",
                Toast.LENGTH_LONG
            ).show()
            //Establezco un marcador con el evento
            googleMap.addMarker(
                (MarkerOptions()
                    .position(latLng)
                    .title("Marcador en destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                    .draggable(true)
            )
        }
        //onlongclick
        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Marcador largo")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet("Teléfono")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            guardarUbicacionEnBaseDeDatos(latLng.latitude, latLng.longitude, "Marcador largo")

        }

        mMap.setOnMarkerClickListener { marker ->
            // Desmarca el marcador anteriormente seleccionado
            marcadorSeleccionado?.setIcon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED
                )
            )

            // Marca el nuevo marcador como seleccionado
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))

            // Almacena el nuevo marcador seleccionado
            marcadorSeleccionado = marker

            // Indica que el evento ha sido consumido (para evitar que se ejecute el comportamiento predeterminado)
            true
        }

        botonVerListaUbicaciones.setOnClickListener {
            //si hay un marcador seleccionado, se envia esa ubicacion a la otra app
            if (marcadorSeleccionado != null) {
                mandarUbicacion()
            }
        }
        //boton para borrar, se hace aqui porque para poder borrar tambien la ubicacion recibida de
        //la otra app.
        botonBorrarUbicaciones.setOnClickListener {
            borrarUbicaciones()
            //refresca el mapa
            val intent = intent
            finish()
            startActivity(intent)
        }

    }

    //funcion para mostrar en el mapa la ubicacion recibida en el intent
    private fun mostrarUbicacionRecibida() {
        val intent = intent
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)
        val descripcion = intent.getStringExtra("descripcion")
        if (latitud != 0.0 && longitud != 0.0 && descripcion != null) {
            val ubicacionRecibida = LatLng(latitud, longitud)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionRecibida))

            guardarUbicacionEnBaseDeDatos(latitud, longitud, descripcion)
        }
    }

    //metodo para guardar ubicacion en sqlite
    private fun guardarUbicacionEnBaseDeDatos(
        latitud: Double,
        longitud: Double,
        descripcion: String
    ) {
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
                    val latitud =
                        getDouble(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_LATITUD))
                    val longitud =
                        getDouble(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_LONGITUD))
                    val descripcion =
                        getString(getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_DESCRIPCION))
                    ubicaciones.add(Ubicacion(latitud, longitud, descripcion))
                }
            }
        }
        return ubicaciones
    }

    //metodo para mandar con un intent las ubicaciones guardadas en sqlite
    private fun mandarUbicacion() {

        val intent = this.packageManager.getLaunchIntentForPackage("com.example.aplicacion1")
        intent?.action = "com.example.aplicacion1.MOSTRAR_UBICACION"
        intent?.putExtra("latitud", marcadorSeleccionado?.position?.latitude)
        intent?.putExtra("longitud", marcadorSeleccionado?.position?.longitude)
        intent?.putExtra("descripcion", marcadorSeleccionado?.title)
        startActivity(intent)

    }

    //metodo para borrar ubicaciones de sqlite
    private fun borrarUbicaciones() {
        val dbHelper = LocationDbHelper(this)
        val db = dbHelper.writableDatabase
        db.delete(LocationContract.LocationEntry.TABLE_NAME, null, null)
    }

}

