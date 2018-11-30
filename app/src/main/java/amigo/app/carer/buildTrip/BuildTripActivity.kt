package amigo.app

import amigo.app.auth.User
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

fun Context.newBuildTripIntent(): Intent {
    return Intent(this, BuildTripActivity::class.java)
}

class BuildTripActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var sp: SharedPreferences
    lateinit var userName: String
    lateinit var userID: String
    lateinit var thisUserID: String

    internal var fb = FirebaseDatabase.getInstance()
    internal var def = fb.reference
    internal var users = def.child("users")

    override fun onStart() {
        val data = sp.getInt("page", 0)
        viewPager.currentItem = data
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        users.child(thisUserID).child("tripstatus").setValue(true)
    }

    override fun onPause() {
        val editor = sp.edit()
        editor.putInt("page", viewPager.currentItem)
        editor.commit()
        super.onPause()
    }

    override fun onStop() {
        val editor = sp.edit()
        editor.putInt("page", 0)
        editor.commit()
        users.child(thisUserID).child("tripstatus").setValue(false)
        super.onStop()
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("action: ", "onCreate")
        super.onCreate(savedInstanceState)
        this.setTitle("Building a trip")
        setContentView(R.layout.builtrip_activity)

        userName = intent.getStringExtra("userfirstname")
        userID = intent.getStringExtra("uid")
        thisUserID = FirebaseAuth.getInstance().uid?:""

        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = BuildTripPagerAdapter(supportFragmentManager, userName, userID)
        val next = findViewById(R.id.next_button) as Button
        sp = getSharedPreferences("Pages", 0)
        val editor = sp.edit()
        editor.putInt("page", 0)
        editor.commit()
        viewPager.currentItem = 0

        next.setOnClickListener {
            viewPager.nextPage()
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            // This method will be invoked when a new page becomes selected.
            override fun onPageSelected(position: Int) {
                if (position == PagesModel.values().size - 1) {
                    next.text = "Review"
                } else {
                    next.text = "Next"
                }
            }
        });
    }

    private fun ViewPager.nextPage() {
        if (this.currentItem < (PagesModel.values().size - 1)) {
            this.currentItem = currentItem + 1
        } else {
            var viewPager = this.adapter as BuildTripPagerAdapter
            var locationFragment = viewPager.getItem(0) as LocationFragment
            var destinationFragment = viewPager.getItem(1) as DestinationFragment
            var transportFragment = viewPager.getItem(2) as TransportFragment

            var confirmationIntent = newConfirmationIntent()

            confirmationIntent.putExtra("uid", userID)
            confirmationIntent.putExtra("location", locationFragment.read())
            confirmationIntent.putExtra("destination", destinationFragment.read())
            confirmationIntent.putExtra("transport", transportFragment.read())
            confirmationIntent.putExtra("locationLatLng", locationFragment.readLatLng())
            confirmationIntent.putExtra("destinationLatLng", destinationFragment.readLatLng())

            startActivity(confirmationIntent)
        }
    }
}



