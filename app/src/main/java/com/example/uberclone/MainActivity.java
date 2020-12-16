package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button mDriver,mCustomer;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDriver=findViewById(R.id.driver);
        mCustomer=findViewById(R.id.customer);

        mDriver.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,DriverLoginActivity.class);
            startActivity(intent);
            finish();
        });
        mCustomer.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,CustomerLoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
}