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
import com.google.firebase.auth.FirebaseUser;
import com.smap16e.group02.isamonitor.R;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {


    private String TAG = "SignupActivity";
    private final int MIN_PASSWORD_LENGTH = 6;

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

                if(password.length() < MIN_PASSWORD_LENGTH) {
                    Toast.makeText(SignupActivity.this, "Password must be "+MIN_PASSWORD_LENGTH + " characters long!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check if email is already registered
                try{
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "SignUp: " + task.isSuccessful());
                            if(task.isSuccessful())
                            {
                                //Send verification Email
                                /***** todo:not sending email - fix later
                                 FirebaseUser user = mAuth.getCurrentUser();
                                 user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                Log.d(TAG, "Verification Email sent");
                                Toast.makeText(SignupActivity.this, "Verification email has been sent. Please verify your account", Toast.LENGTH_LONG).show();
                                } else {
                                Toast.makeText(SignupActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                                }
                                }
                                }); */

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            } else {
                                Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, task.getException().toString());
                                finish();
                            }
                        }
                    });
                } catch (Exception e){
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }
}
