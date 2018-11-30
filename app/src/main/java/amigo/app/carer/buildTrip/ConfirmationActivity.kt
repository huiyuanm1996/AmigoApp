package amigo.app

import amigo.app.carer.buildTrip.toast
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import org.json.JSONObject
import android.widget.Toast



fun Context.newConfirmationIntent(): Intent {
    return Intent(this, ConfirmationActivity::class.java)
}



class ConfirmationActivity : AppCompatActivity() {

    val ref = FirebaseDatabase.getInstance().reference
    val trips = ref.child("trips")

    val toastThread: Thread = object : Thread() {
        override fun run() {
            try {
                Thread.sleep((Toast.LENGTH_LONG * 2).toLong())
                val carerIntent = newCarerContactsIntent()
                carerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(carerIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTitle("Confirm trip")
        val extras = intent.extras
        val location = extras.getString("location") ?: "location"
        val destination = extras.getString("destination") ?: "destination"
        val transport = extras.getString("transport") ?: "transport"

        setContentView(R.layout.confirmation_layout)
        val locationView = findViewById<TextView>(R.id.location)
        locationView.text = location
        val destinationView = findViewById<TextView>(R.id.destination)
        destinationView.text = destination
        val transportView = findViewById<TextView>(R.id.transport)
        transportView.text = transport

        val back = findViewById(R.id.back_button) as Button
        val locationEdit = findViewById(R.id.locationEdit) as TextView
        val destinationEdit = findViewById(R.id.destinationEdit) as TextView
        val transportEdit = findViewById(R.id.transportEdit) as TextView
        val confirm = findViewById(R.id.confirm_button) as Button

        confirm.setOnClickListener {
            val locationLatLng = extras.getParcelable("locationLatLng") as LatLng
            val destinationLatLng = extras.getParcelable("destinationLatLng") as LatLng
            val uid = extras.getString("uid") as String
            val usersTrips = trips.child(uid)

            val urlDirections = getUrl(locationLatLng, destinationLatLng, transport)
            val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
                var jsonResponse = JSONObject(response).toString()
                val map = Gson().fromJson(jsonResponse, HashMap::class.java)
                var key = usersTrips.push().key
                usersTrips.child(key).setValue(map) { error, ref ->
                     if (error != null) {
                         this.toast("Couldn't send the trip, try again later")
                     } else {
                         this.toast("Trip sent!")
                         toastThread.start()
                     }
                }
//                 disabled for testing
//                Log.v("jsonMap", map.toString())
//                this.toast("Map sent to John!")
//                toastThread.start()
            }, Response.ErrorListener {
                _ ->
            }){}

            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(directionsRequest)
        }

        locationEdit.setOnClickListener {
            val sp = getSharedPreferences("Pages", 0)
            val editor = sp.edit()
            editor.putInt("page", 0)
            editor.commit()
            finish()
        }
        destinationEdit.setOnClickListener {
            val sp = getSharedPreferences("Pages", 0)
            val editor = sp.edit()
            editor.putInt("page", 1)
            editor.commit()
            finish()
        }
        transportEdit.setOnClickListener {
            val sp = getSharedPreferences("Pages", 0)
            val editor = sp.edit()
            editor.putInt("page", 2)
            editor.commit()
            finish()
        }

        back.setOnClickListener {
            finish()
        }
    }

    private fun getUrl(origin: LatLng, dest: LatLng, mode: String): String {

        // Origin of route
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val strDest = "destination=" + dest.latitude + "," + dest.longitude

        // Mode of transport
        val mode = "mode" + mode.toLowerCase()

        // API Key
        val key = "key=" + resources.getString(R.string.google_maps_key)

        // Building the parameters to the web service
        val parameters = "$strOrigin&$strDest&$mode&$key"

        // Output format
        val output = "json"

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }
}