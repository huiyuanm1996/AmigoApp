package amigo.app.navi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import amigo.app.R;
import amigo.app.useractivity.MainActivity;
import amigo.app.useractivity.UserContactActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15f;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSION_REQUEST_LOCATION = 123;
    private String routeID;

    FirebaseDatabase fb = FirebaseDatabase.getInstance();
    DatabaseReference def = fb.getReference();
    DatabaseReference users = def.child("users");

    private Location currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        routeID = getIntent().getStringExtra("routeId");
        // Try to check the permission of getting device location before getting it
        getLocationPermission();

        Button help =  findViewById(R.id.help_button);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, UserContactActivity.class));
            }
        });

        Button arrive = findViewById(R.id.arrived_button);
        arrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        String uid = FirebaseAuth.getInstance().getUid();
        users.child(uid).child("tripstatus").setValue(true);
    }

    @Override
    protected void onStop(){
        super.onStop();
        String uid = FirebaseAuth.getInstance().getUid();
        users.child(uid).child("tripstatus").setValue(false);
    }

    public void initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: found location");
                        currentLocation = (Location) task.getResult();
                        String uid = FirebaseAuth.getInstance().getUid();
                        if (uid != null) {
                            users.child(uid).child("currentlocation").child("latitude").setValue(currentLocation.getLatitude());
                            users.child(uid).child("currentlocation").child("longitude").setValue(currentLocation.getLongitude());
                            users.child(uid).child("currentlocation").child("time").setValue(System.currentTimeMillis());
                            Log.d(TAG, "uploaded to Firebase");
                        }
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        Log.d(TAG, "mved camera");
                    } else {
                        Log.d(TAG, "current location is null");
                        Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /**
     * Try to check and get the permission of ACCESS_FINE_LOCATION
     */
    private void getLocationPermission(){
        // Check if ACCESS_FINE_LOCATION is granted
        if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            // if user deny this permission, try to explain to user why we need this permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                // Pop up a window to tell user the reason and request the permission
                Snackbar.make(findViewById(android.R.id.content),
                        "Please grant permissions, We need this permission to locate " +
                                "where you are and help you to navigate",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS},
                                        PERMISSION_REQUEST_LOCATION);
                            }
                        }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }
        } else {
            // Make mLocationPermissionGranted true and initial map
            mLocationPermissionsGranted = true;
            initMap();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch (requestCode){
            case PERMISSION_REQUEST_LOCATION:{
                if(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    mLocationPermissionsGranted = true;
                    initMap();
                } else {
                    
                }
                return;
            }
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to: lat: " + latLng.latitude + ", lng:" + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Our map is ready!");
        mMap = googleMap;
        if(mLocationPermissionsGranted){
            getDeviceLocation();
        }
        mMap.setMyLocationEnabled(true);
        renderRoute(mMap);

    }

    public void renderRoute(final GoogleMap mMap){
        Log.d(TAG, "render the route now");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String userID = FirebaseAuth.getInstance().getUid();
        if(userID != null) {
            final DatabaseReference map = ref.child("trips").child(userID).child(routeID);
            map.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Polyline polyline;
                    try {

                        DataSnapshot points = dataSnapshot.child("routes").child("0")
                                .child("overview_polyline").child("points");
                        String encodedString = points.getValue(String.class);

                        List<LatLng> list = decodePoly(encodedString);
                        // draw a line between each 2 adjacent points of list
                        for (int z = 0; z < list.size() - 1; z++) {
                            LatLng src = list.get(z);
                            LatLng dest = list.get(z + 1);
                            polyline = mMap.addPolyline(new PolylineOptions()
                                    .add(new LatLng(src.latitude, src.longitude),
                                            new LatLng(dest.latitude, dest.longitude))
                                    .width(25)
                                    .color(Color.BLUE)
                                    .geodesic(true));
                            if (z == list.size() - 2) {

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(dest.latitude, dest.longitude)));
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Decode the string that holds an encoded polyline representation of the route and save it to list as LatLng type
     * @param encoded a string contains all points
     * @return a LatLng list of all points
     */
    private List<LatLng> decodePoly(String encoded){

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {

            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        mMap.setMyLocationEnabled(true);

        return poly;
    }
}
