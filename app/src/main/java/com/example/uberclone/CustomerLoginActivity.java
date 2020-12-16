package com.example.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mAuth=FirebaseAuth.getInstance();
        firebaseAuthListener= firebaseAuth -> {
            FirebaseUser user=firebaseAuth.getCurrentUser();
            if(user!=null)
            {
                Intent intent=new Intent(CustomerLoginActivity.this,CustomerMapActivity.class);
                startActivity(intent);
                finish();
            }
        };

        mEmail=findViewById(R.id.emailCustomer);
        mPassword=findViewById(R.id.passwordCustomer);
        mLogin=findViewById(R.id.loginCustomer);
        mRegister=findViewById(R.id.registrationCustomer);

        mRegister.setOnClickListener(v -> {
            final String email=mEmail.getText().toString();
            final String password=mPassword.getText().toString();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener
                    (CustomerLoginActivity.this, task -> {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(CustomerLoginActivity.this, "Signup error", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String userID=mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserInDB= FirebaseDatabase.getInstance()
                                    .getReference().child("Users").child("Customers")
                                    .child(userID);
                            currentUserInDB.setValue(true);
                        }
                    });
        });

        mLogin.setOnClickListener(v -> {
            final String email=mEmail.getText().toString();
            final String password=mPassword.getText().toString();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener
                    (CustomerLoginActivity.this, task -> {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(CustomerLoginActivity.this, "Signin error", Toast.LENGTH_SHORT).show();
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