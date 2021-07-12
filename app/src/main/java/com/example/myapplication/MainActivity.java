package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.helloar.HelloArActivity;

public class MainActivity extends AppCompatActivity {
    EditText editText,editTextPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       editText = (EditText) findViewById(R.id.userName);
       editTextPassword = (EditText) findViewById(R.id.password);

        Button btnAction = (Button) findViewById(R.id.loginButton);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               clickListner();

            }
        });
    }

    public void  clickListner(){



        String value = editText.getText().toString();
        String password = editTextPassword.getText().toString();

        if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(password)) {

            Intent i = new Intent(MainActivity.this, MainActivity3.class);
//            i.putExtra("key", value);
            startActivity(i);


        }
        else{
            Toast.makeText(MainActivity.this, "pLease enter name", Toast.LENGTH_SHORT).show();
        }








    }


//    public void startAR(View view) {
//        startActivity(new Intent(MainActivity.this, HelloArActivity.class));
//    }
}