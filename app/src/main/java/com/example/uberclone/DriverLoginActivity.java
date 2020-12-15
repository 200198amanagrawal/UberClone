package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class  DriverLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth=FirebaseAuth.getInstance();
        firebaseAuthListener= firebaseAuth -> {
            FirebaseUser user=firebaseAuth.getCurrentUser();
            if(user!=null)
            {
                Intent intent=new Intent(DriverLoginActivity.this,DriverMapActivity.class);
                startActivity(intent);
                finish();
            }
        };

        mEmail=findViewById(R.id.emailDriver);
        mPassword=findViewById(R.id.passwordDriver);
        Button mLogin = findViewById(R.id.loginDriver);
        Button mRegister = findViewById(R.id.registrationDriver);

        mRegister.setOnClickListener(v -> {
            final String email=mEmail.getText().toString();
            final String password=mPassword.getText().toString();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener
                    (DriverLoginActivity.this, task -> {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(DriverLoginActivity.this, "Signup error"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String userID= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            DatabaseReference currentUserInDB= FirebaseDatabase.getInstance()
                                    .getReference().child("Users").child("Drivers")
                                    .child(userID);
                            currentUserInDB.setValue(true);
                        }
                    });
        });

        mLogin.setOnClickListener(v -> {
            final String email=mEmail.getText().toString();
            final String password=mPassword.getText().toString();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener
                    (DriverLoginActivity.this, task -> {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(DriverLoginActivity.this, "Signin error", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.addAuthStateListener(firebaseAuthListener );
    }
}