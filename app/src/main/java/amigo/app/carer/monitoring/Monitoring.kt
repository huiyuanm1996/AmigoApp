package amigo.app.carer.monitoring

import amigo.app.CarerContactsActivity
import android.os.Bundle
import android.app.Activity
import amigo.app.R
import amigo.app.VideoChat
import amigo.app.useractivity.chatting
import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.monitoring_activity.*

fun Context.newMonitoringIntent(): Intent {
    return Intent(this, Monitoring::class.java)
}

class Monitoring : AppCompatActivity() {

    val ref = FirebaseDatabase.getInstance().reference
    val users = ref.child("users")

    lateinit var mMapView: MapView
    lateinit var googleMap: GoogleMap
    lateinit var userName: String
    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userName = intent.getStringExtra("userfirstname")
        userID = intent.getStringExtra("uid")

        this.setTitle("Monitoring " + userName)

        setContentView(R.layout.monitoring_activity)

        imageButton_chat.setOnClickListener {
            var intent = Intent(this, chatting::class.java)
            startActivity(intent)
        }

        imageButton_call.setOnClickListener {
            var intent = Intent(this, VideoChat::class.java)
            startActivity(intent)
        }

        mMapView = findViewById(R.id.monitoringMap) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        MapsInitializer.initialize(applicationContext);
        mMapView.getMapAsync { mMap ->
            googleMap = mMap
        }

        var trackingUserLocation = users.child(userID).child("currentlocation")
        val locationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null){
                    val locationLat = dataSnapshot.child("latitude").getValue(Double::class.java)?: 0.0
                    val locationLng = dataSnapshot.child("longitude").getValue(Double::class.java)?: 0.0
                    val locationLatLng = LatLng(locationLat, locationLng)
                    placeMarkerOnMap(locationLatLng)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }

        trackingUserLocation.addValueEventListener(locationListener)

    }

    private fun placeMarkerOnMap(location: LatLng) {
        googleMap.clear()
        val markerOptions = MarkerOptions().position(location)
        googleMap.addMarker(markerOptions)
        val cameraPosition = CameraPosition.Builder().target(location).zoom(16f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

}
