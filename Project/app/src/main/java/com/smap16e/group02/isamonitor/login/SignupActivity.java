package com.smap16e.group02.isamonitor.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.smap16e.group02.isamonitor.R;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {


    private String TAG = "SignupActivity";

    private Button btnSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnSignUp = (Button)findViewById(R.id.btn_signUp_signUp);
        mAuth = FirebaseAuth.getInstance();

        //Get email from login screen
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String email = bundle.getString(LoginActivity.EXTRA_EMAIL);
            if(email != null)
            {
                ((EditText)findViewById(R.id.signUp_email)).setText(email);
            }
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = ((EditText)findViewById(R.id.signUp_email)).getText().toString();
                String password = ((EditText)findViewById(R.id.signUp_password)).getText().toString();

                //Check if email syntax
                //check if passwords match
                if(email.isEmpty() || !email.contains("@")) {
                    Toast.makeText(SignupActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!Objects.equals(password, ((EditText) findViewById(R.id.signUp_rePassword)).getText().toString()))
                {
                    Toast.makeText(SignupActivity.this, "Passwords must match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check if email is already registered
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "LogIn: " + task.isSuccessful());
                        if(task.isSuccessful())
                        {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Log.d(TAG, task.getResult().toString());
                        }
                    }
                });
            }
        });
    }
}
