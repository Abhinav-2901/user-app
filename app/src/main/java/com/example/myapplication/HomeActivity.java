package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.helloar.HelloArActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

public class HomeActivity extends AppCompatActivity {


    TextView textViewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.firstFragment, R.id.secondFragment, R.id.thirdFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


//        Button btnAction = (Button) findViewById(R.id.startcon);
//        btnAction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(HomeActivity.this, RemoteActivity.class);
//                startActivity(i);
//            }
//        });



//        Intent intent = getIntent();
//
//        textViewPassword = findViewById(R.id.userName);
//
//        String id = intent.getStringExtra("key");
//        String userName = "Welcome " + id;
//
//        textViewPassword.setText(userName);





    }


    public void startAR(View view) {

        startActivity(new Intent(HomeActivity.this, HelloArActivity.class));
    }
}