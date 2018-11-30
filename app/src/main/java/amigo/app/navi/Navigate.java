package amigo.app.navi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import amigo.app.R;

public class Navigate extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        readDatabase(mDatabase);
    }

    private void readDatabase(DatabaseReference mDatabase) {

    }
}
