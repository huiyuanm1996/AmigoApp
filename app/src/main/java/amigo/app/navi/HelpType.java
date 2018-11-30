package amigo.app.navi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import amigo.app.R;
import amigo.app.VideoChat;
import amigo.app.useractivity.UserContactActivity;
import amigo.app.useractivity.chatting;

public class HelpType extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help_type_window);
        popUpWindow();
    }

    private void popUpWindow(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.6));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);


        Button textChat = findViewById(R.id.text);
        textChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textChatIntent = new Intent(HelpType.this, UserContactActivity.class);
                startActivity(textChatIntent);
            }
        });

        Button videoChat = findViewById(R.id.video);
        videoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoChatIntent = new Intent(HelpType.this, VideoChat.class);
                startActivity(videoChatIntent);
            }
        });

    }
}
