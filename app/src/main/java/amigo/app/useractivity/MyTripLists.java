package amigo.app.useractivity;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import amigo.app.R;
import amigo.app.navi.MapsActivity;

public class MyTripLists extends AppCompatActivity {


    private ListView mapLists;
    private ArrayList<String> endAddresses = new ArrayList<>();
    private ArrayList<String> route = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip_lists);

        fetchTrips();

    }

    private void fetchTrips(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String userID = FirebaseAuth.getInstance().getUid();
        if(userID != null) {
            DatabaseReference trips = ref.child("trips").child(userID);
            trips.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot trip : dataSnapshot.getChildren()) {
                        DataSnapshot end_p = trip.child("routes").child("0").child("legs")
                                .child("0").child("end_address");
                        String routeID = trip.getKey();
                        Log.d("routeID", "it is " + routeID);
                        String end_point = end_p.getValue(String.class);
                        if (end_point != null && routeID != null) {
                            endAddresses.add(end_point);
                            route.add(routeID);
                        }
                    }
                    listTrips();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Log.d("fetchTrip", "current userID is null");
        }
    }


    private void listTrips(){
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, endAddresses);
        mapLists = findViewById(R.id.trip_lists);
        mapLists.setAdapter(adapter);
        mapLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent clickMap = new Intent(MyTripLists.this, MapsActivity.class);
                clickMap.putExtra("routeId", route.get(position));
                startActivity(clickMap);
            }
        });
    }

    public static Intent newMyTripLists(Context context){
        return new Intent(context,MyTripLists.class);
    }
}
