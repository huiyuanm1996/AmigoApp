package amigo.app

import amigo.app.auth.User
import amigo.app.carer.buildTrip.toast
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_video_chat.*
import org.w3c.dom.Text
import java.util.*

val unimelb = LatLng(-37.798079, 144.959583)
var PLACE_PICKER_REQUEST = 3

class LocationFragment : Fragment() {

    lateinit var locationText: TextView
    lateinit var mMapView: MapView
    lateinit var googleMap: GoogleMap
    lateinit var addressText: String
    lateinit var locationPlace: Place
    lateinit var placeButton: FrameLayout
    lateinit var autocomplete_text: TextView
    lateinit var user: User
    lateinit var userPosition: LatLng


    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.location_layout, container, false)

        val ref = FirebaseDatabase.getInstance().reference
        val users = ref.child("users")

        var pa = activity?.findViewById<ViewPager>(R.id.viewpager)?.adapter as BuildTripPagerAdapter
        val userName = pa.userName
        val userID = pa.userID

        placeButton = rootView.findViewById(R.id.placeButton)
        autocomplete_text = rootView.findViewById(R.id.place_autocomplete)
        locationText = rootView.findViewById(R.id.location_text)
        locationText.text = "Where will " + userName + " be starting their journey?"
        placeButton.setOnClickListener {
            loadPlacePicker()
        }

        mMapView = rootView.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        MapsInitializer.initialize(activity!!.getApplicationContext());
        mMapView.getMapAsync { mMap ->
            googleMap = mMap

            users.child(userID).child("currentlocation").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {}

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val userLatitude = snapshot?.child("latitude")?.getValue(Double::class.java)?:unimelb.latitude
                    val userLongitude = snapshot?.child("longitude")?.getValue(Double::class.java)?:unimelb.longitude
                    userPosition = LatLng(userLatitude, userLongitude)
                    val cameraPosition = CameraPosition.Builder().target(userPosition).zoom(16f).build()
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            })

            }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    fun read(): CharSequence {
        return addressText
    }

    companion object {
        // newInstance constructor for creating fragment with arguments
        fun newInstance(): LocationFragment {
            val fragment = LocationFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            val place = PlacePicker.getPlace(this.context, data)
            locationPlace = place
            addressText = place.address.toString()
            autocomplete_text.text = addressText
            placeMarkerOnMap(place.latLng)
        }

    }


    private fun placeMarkerOnMap(location: LatLng) {
        googleMap.clear()
        val markerOptions = MarkerOptions().position(location)
        googleMap.addMarker(markerOptions)
        val cameraPosition = CameraPosition.Builder().target(location).zoom(16f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        val boundsBuilder = LatLngBounds.Builder()

        boundsBuilder.include(userPosition)

        val bounds = boundsBuilder.build()
        builder.setLatLngBounds(bounds)
        try {
            startActivityForResult(builder.build(this.activity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    fun readLatLng(): LatLng {
        return locationPlace.latLng
    }
}

class DestinationFragment : Fragment() {

    lateinit var mMapView: MapView
    lateinit var googleMap: GoogleMap
    lateinit var addressText: String
    lateinit var destinationText: TextView
    lateinit var destinationPlace: Place
    lateinit var placeButton: FrameLayout
    lateinit var autocomplete_text: TextView
    lateinit var userPosition: LatLng

    val ref = FirebaseDatabase.getInstance().reference
    val users = ref.child("users")

    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.destination_layout, container, false)
        placeButton = rootView.findViewById(R.id.placeButton)
        autocomplete_text = rootView.findViewById(R.id.place_autocomplete)
        destinationText = rootView.findViewById(R.id.destination_text)

        var pa = activity?.findViewById<ViewPager>(R.id.viewpager)?.adapter as BuildTripPagerAdapter
        val userName = pa.userName
        val userID = pa.userID

        destinationText.text = "Where will " + userName + " be going to on their journey?"

        placeButton.setOnClickListener {
            loadPlacePicker()
        }

        mMapView = rootView.findViewById(R.id.mapView) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        MapsInitializer.initialize(activity!!.getApplicationContext());
        mMapView.getMapAsync { mMap ->
            googleMap = mMap
            users.child(userID).child("currentlocation").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {}

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val userLatitude = snapshot?.child("latitude")?.getValue(Double::class.java)?:unimelb.latitude
                    val userLongitude = snapshot?.child("longitude")?.getValue(Double::class.java)?:unimelb.longitude
                    userPosition = LatLng(userLatitude, userLongitude)
                    val cameraPosition = CameraPosition.Builder().target(userPosition).zoom(16f).build()
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            })
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    fun read(): CharSequence {
        return addressText
    }

    companion object {
        // newInstance constructor for creating fragment with arguments
        fun newInstance(): DestinationFragment {
            val fragment = DestinationFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this.context, data)
                destinationPlace = place
                addressText = place.address.toString()
                autocomplete_text.text = addressText
                placeMarkerOnMap(place.latLng)

            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        googleMap.clear()
        val markerOptions = MarkerOptions().position(location)
        googleMap.addMarker(markerOptions)
        val cameraPosition = CameraPosition.Builder().target(location).zoom(16f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        val boundsBuilder = LatLngBounds.Builder()

        boundsBuilder.include(userPosition)

        val bounds = boundsBuilder.build()
        builder.setLatLngBounds(bounds)
        try {
            startActivityForResult(builder.build(this.activity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    fun readLatLng(): LatLng {
        return destinationPlace.latLng
    }
}

class TransportFragment : Fragment() {

    lateinit var carButton: ImageButton
    lateinit var busButton: ImageButton
    lateinit var walkButton: ImageButton
    lateinit var transportText: TextView
    lateinit var savedTransport: String

    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.transport_layout, container, false)

        transportText = rootView.findViewById(R.id.transport_text)

        var pa = activity?.findViewById<ViewPager>(R.id.viewpager)?.adapter as BuildTripPagerAdapter
        val userName = pa.userName

        transportText.text = "What is the best way for " + userName + " to travel?"
        carButton = rootView.findViewById(R.id.carButton)
        busButton = rootView.findViewById(R.id.busButton)
        walkButton = rootView.findViewById(R.id.walkButton)
        var buttons = hashMapOf(carButton to "Driving", busButton to "Transit", walkButton to "Walking")

        carButton.setOnClickListener {
            clearButtonSelections()
            carButton.setImageResource(R.drawable.cargreen)
            savedTransport = buttons[carButton]!!
        }
        busButton.setOnClickListener {
            clearButtonSelections()
            busButton.setImageResource(R.drawable.busgreen)
            savedTransport = buttons[busButton]!!
        }
        walkButton.setOnClickListener {
            clearButtonSelections()
            walkButton.setImageResource(R.drawable.walkgreen)
            savedTransport = buttons[walkButton]!!
        }

        return rootView

    }

    fun read(): CharSequence {
        return savedTransport
    }

    fun clearButtonSelections() {
        carButton.setImageResource(R.drawable.car)
        busButton.setImageResource(R.drawable.bus)
        walkButton.setImageResource(R.drawable.walk)
    }

    companion object {

        // newInstance constructor for creating fragment with arguments
        fun newInstance(): TransportFragment {
            val fragment = TransportFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}