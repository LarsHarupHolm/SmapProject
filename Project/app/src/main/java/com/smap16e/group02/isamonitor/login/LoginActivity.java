package com.smap16e.group02.isamonitor.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.smap16e.group02.isamonitor.ParameterListActivity;
import com.smap16e.group02.isamonitor.R;

//Sources for authentication:
//https://firebase.google.com/docs/auth/android/password-auth

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";
    public static final String EXTRA_EMAIL = "extra_email";
    public static final int REQ_REGISTERED_EMAIL = 1;

    private Button btnLogIn;
    private Button btnSignUp;
    private Button btnLostPassword;

    private String mEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get UI elements
        btnLogIn = (Button)findViewById(R.id.btn_logIn);
        btnSignUp = (Button)findViewById(R.id.btn_logIn_signUp);
        btnLostPassword = (Button)findViewById(R.id.btn_lostPassword);
        createOnClickListeners();

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //Check if returning by log out
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
           String email = bundle.getString(LoginActivity.EXTRA_EMAIL);
            if(email != null)
            {
                ((EditText)findViewById(R.id.login_email)).setText(email);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Skip login if user is already logged in.
        skipIfLoggedIn();
    }

    private void skipIfLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        //Only if user is verified
        if(user != null) {
            if(user.isEmailVerified()) {
                //Redirect to list
                Log.d(TAG, "LogIn: User already logged in, redirecting past login");
                Intent loginFinishedIntent = new Intent(LoginActivity.this, ParameterListActivity.class);
                startActivity(loginFinishedIntent);
                finish(); //finish so User cannot re-enter login screen
            }
        }
    }

    private void createOnClickListeners()
    {
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
                String password = ((EditText)findViewById(R.id.login_password)).getText().toString();

                if(email.isEmpty() || !email.contains("@")) {
                    Toast.makeText(LoginActivity.this, R.string.enter_email, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.enter_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "LogIn: " + task.isSuccessful());
                        if(task.isSuccessful())
                        {
                            //Check if user is verified
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user.isEmailVerified()) {
                                Intent loginFinishedIntent = new Intent(LoginActivity.this, ParameterListActivity.class);
                                startActivity(loginFinishedIntent);
                                finish(); //finish so User cannot re-enter login screen
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.verify_account, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.username_pw_incorrect, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //Take the input from mail and send it to new window
                String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
                Intent signUpIntent = new Intent(LoginActivity.this, SignupActivity.class);
                if(!email.isEmpty() && email.contains("@")) {
                    signUpIntent.putExtra(EXTRA_EMAIL, email);
                }
                startActivityForResult(signUpIntent, REQ_REGISTERED_EMAIL);
            }
        });

        btnLostPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //Take the input from mail and send it to new window
                String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
                Intent signUpIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                if(!email.isEmpty() && email.contains("@")) {
                    signUpIntent.putExtra(EXTRA_EMAIL, email);
                }
                startActivityForResult(signUpIntent, REQ_REGISTERED_EMAIL);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case REQ_REGISTERED_EMAIL:
                    String email = data.getStringExtra(EXTRA_EMAIL);
                    ((EditText)findViewById(R.id.login_email)).setText(email);
                    break;
            }
        }
    }
}
