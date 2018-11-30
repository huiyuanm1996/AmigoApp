package amigo.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import amigo.app.useractivity.Listener;
import amigo.app.useractivity.MessageBody;
import amigo.app.useractivity.MyNotificiationManager;

public class VideoChat extends AppCompatActivity implements Connector.IConnect {

    private Connector vc;
    private FrameLayout videoFrame;

    FirebaseDatabase fb = FirebaseDatabase.getInstance();
    DatabaseReference def = fb.getReference();
    private String usertoken = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Video call");
        setContentView(R.layout.activity_video_chat);
        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();
        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);

        ImageButton connect = findViewById(R.id.imageButton_connect);
        ImageButton disconnect = findViewById(R.id.imageButton_disconnect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect(v);
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Disconnect(v);
                }
                catch(Exception e){

                }
                finish();
            }
        });

    }

    /*public boolean connect(String host, String token, String displayName, String resourceId, Connector.IConnect Connect) {
        this.Connect = Connect;
        return this.connectNative(this.objPtr, host, token, displayName, resourceId);
    }*/

    public static Intent createIntent(Context context){

        return new Intent(context,VideoChat.class);
    }

    public void Connect(View v) {
        def.child("videochat").child(usertoken).child(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())).setValue("ddd");
        vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoClient info@VidyoConnector", "", 0);
        vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
        String token = "cHJvdmlzaW9uAHVzZXIxQDRlOGYwNS52aWR5by5pbwA2MzcwNzIzOTQyMAAAZjJiNjE0MWUyMjI3OWViNDkxYTcyY2FkNDc3NzU0M2ZkYTkzNWVjN2FjMzkxYjk3ODk4MzQ5NmExNDRiN2FjY2NmOWZhMjg0ZGIyZmMzZGVjYzhlNjVmMmFkMjFiZTNm";
        vc.connect("prod.vidyo.io", token, "DemoUser", "DemoRoom",  this);
    }

    public void Disconnect(View v) {
        vc.disconnect();
    }

    public void onSuccess() {}

    public void onFailure(Connector.ConnectorFailReason reason) {}

    public void onDisconnected(Connector.ConnectorDisconnectReason reason) {}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}