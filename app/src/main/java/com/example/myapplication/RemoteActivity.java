package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

public class RemoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
    }

    public void onButtonClick(View v){
        EditText editText = findViewById(R.id.confrenceid);
        String text = editText.getText().toString();

        if(text.length()>0){
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder().setRoom(text).build();
            JitsiMeetActivity.launch(this,options);
        }
    }
}