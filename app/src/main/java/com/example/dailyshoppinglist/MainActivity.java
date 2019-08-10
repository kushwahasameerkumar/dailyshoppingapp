package com.example.dailyshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText email;
    private EditText pass;
    private TextView signup;
    private Button login;

    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email_login);
        pass = findViewById(R.id.password_login);
        signup = findViewById(R.id.signup_txt);
        login = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();

        //Check if user is already Logged in
        if(mAuth.getCurrentUser() != null)
            startActivity(new Intent(MainActivity.this, HomeActivity.class));

        //Show progress dialog while loading
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage("Processing...");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mEmail = email.getText().toString().trim();
                final String mPass = pass.getText().toString().trim();

                //check for null input
                if(TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPass)){
                    email.setError("Required field...");
                    pass.setError("Required field...");
                    return;
                }
                mDialog.show();
                mAuth.signInWithEmailAndPassword(mEmail, mPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }else{
                            Toast.makeText(MainActivity.this, "Login Failed... Kindly recheck your login credentials...", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                        }
                    }
                });

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });
    }
}
