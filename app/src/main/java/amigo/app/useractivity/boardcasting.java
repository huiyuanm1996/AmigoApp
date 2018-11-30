package amigo.app.useractivity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import amigo.app.R;

/**
 * There is no real implementation of this class, has been deprecated*/
public class boardcasting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardcasting);
    }

    public static Intent createIntent(Context context){
        return new Intent(context,boardcasting.class);
    }
}
