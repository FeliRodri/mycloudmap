package com.example.mycloudmap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
    GoogleMap.OnMapLongClickListener {

    private lateinit var txtLatitud: EditText
    private lateinit var txtLongitud: EditText
    private lateinit var mMap: GoogleMap
    private lateinit var btnGuardarUbicacion: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mTextViewName: TextView
    private lateinit var mTextViewEmail: TextView

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.btnGuardarUbicacion = findViewById<View>(R.id.btnGuardarUbicacion) as Button
        txtLatitud = findViewById(R.id.txtLatitud)
        txtLongitud = findViewById(R.id.txtLongitud)
        this.btnCerrarSesion = findViewById<View>(R.id.btnCerrarSesion) as Button
        mTextViewName = findViewById(R.id.textViewName)
        mTextViewEmail = findViewById(R.id.textViewEmail)

        val mAuth = FirebaseAuth.getInstance()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            val opcionesMapa: UiSettings = googleMap.uiSettings
            opcionesMapa.isZoomControlsEnabled = true
            opcionesMapa.isCompassEnabled = true

            val zoomLevel = 15f

            val chile = LatLng(-33.4323276, -70.6335647)
            mMap.addMarker(MarkerOptions().position(chile).title(getString(R.string.marker_chile)))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chile,zoomLevel))

            mMap.setOnMapClickListener(this)
            mMap.setOnMapLongClickListener(this)

            enableLocation()

            // Inicializa Firebase Realtime Database
            databaseReference = FirebaseDatabase.getInstance().reference.child("ubicaciones")

            // Inicializa FusedLocationProviderClient para obtener la ubicación
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            btnGuardarUbicacion.setOnClickListener {
                guardarUbicacion()
            }


            btnCerrarSesion.setOnClickListener {
                mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            getUserInfo()
        }
    }


    /*private fun verificarDatosUsuario(): Boolean {
        // Aquí verificas los datos del usuario, por ejemplo, a través de autenticación Firebase
        // Si los datos coinciden, devuelve true; de lo contrario, devuelve false
        return true
    }*/

    override fun onMapClick(latLng: LatLng) {
        updateMarkerAndCamera(latLng)
    }

    override fun onMapLongClick(latLng: LatLng) {
        updateMarkerAndCamera(latLng)
    }

    private fun updateMarkerAndCamera(latLng: LatLng) {
        txtLatitud.setText(latLng.latitude.toString())
        txtLongitud.setText(latLng.longitude.toString())

        mMap.clear()

        val markerOptions = MarkerOptions().position(latLng).title(getString(R.string.marker_new))
        mMap.addMarker(markerOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")

    }

    private fun isLocatedPermissionGranted() =
        checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::mMap.isInitialized) return
        if (isLocatedPermissionGranted()) {
            if (checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = true


        } else {
            requestLocationPermission()
        }
    }

   /* private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos de ubicación si no están concedidos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // Obtener la ubicación actual del dispositivo
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Verificar si se obtuvo la ubicación
            if (location != null) {
                // Guardar la ubicación en Firebase Realtime Database
                guardarUbicacionEnFirebase(location.latitude, location.longitude)
            }
        }
    }*/

    private fun guardarUbicacion() {
        // Aquí obtén la latitud y longitud actual del mapa
        val latitud = mMap.cameraPosition.target.latitude
        val longitud = mMap.cameraPosition.target.longitude

        // Luego, guarda esta ubicación en Firebase Realtime Database u otro lugar de almacenamiento.
        guardarUbicacionEnFirebase(latitud, longitud)

        // Puedes mostrar un mensaje de confirmación después de guardar la ubicación
        Toast.makeText(this, "Ubicación guardada con éxito", Toast.LENGTH_SHORT).show()
    }



    private fun guardarUbicacionEnFirebase(latitud: Double, longitud: Double) {
        // Crear un objeto de ubicación para guardar en la base de datos
        val ubicacion = Ubicacion(latitud, longitud)

        // Generar una clave única para la ubicación
        val nuevaUbicacionKey = databaseReference.push().key

        if (nuevaUbicacionKey != null) {
            // Guardar la ubicación en la base de datos con la clave generada
            databaseReference.child(nuevaUbicacionKey).setValue(ubicacion)

        }
    }

    data class Ubicacion(val latitud: Double, val longitud: Double)


    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Para activar la localizacion ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {

            }
        }
    }
    private fun getUserInfo() {
        val mAuth = FirebaseAuth.getInstance()
        val id = mAuth.currentUser!!.uid
        val mDatabase = FirebaseDatabase.getInstance().reference.child("Users")
        mDatabase.child("Users").child(id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").getValue<String>()
                    val email = dataSnapshot.child("email").getValue<String>()

                    mTextViewName.text = name
                    mTextViewEmail.text = email
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar la cancelación de la lectura de datos aquí
            }
        })
    }

}



