package amigo.app;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference map = ref.child("testmaps").child("-LOC7y4pCF2NfHjvypU-").child("testmap1");
        map.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String map = dataSnapshot.toString();

                TextView textView = findViewById(R.id.textView);
                textView.setText(map);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
