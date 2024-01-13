package com.example.mapaapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //recogemos las coordenadas que nos llegan del intent
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)
        // Se fijan unas coordenadas
        val ubicacionRecibida = LatLng(latitud, longitud)
        //Fija un marcador en la posición y con el texto que se indica.
        mMap.addMarker(MarkerOptions()
            .position(ubicacionRecibida)
            .title("Marcador en la ubicacion recibida"))
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

        }

    }

}

