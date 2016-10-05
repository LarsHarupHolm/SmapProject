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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.smap16e.group02.isamonitor.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private final String TAG = "ResetPasswordActivity";

    private Button btn_reset_pw_submit;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        btn_reset_pw_submit = (Button)findViewById(R.id.btn_res_pw_submit);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //Get email from login screen
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String email = bundle.getString(LoginActivity.EXTRA_EMAIL);
            if(email != null)
            {
                ((EditText)findViewById(R.id.res_pw_email)).setText(email);
            }
        }

        btn_reset_pw_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send email to reset password
                final String email = ((EditText)findViewById(R.id.res_pw_email)).getText().toString();

                if(email.isEmpty() || !email.contains("@")) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check if email is registered.
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Email has been sent.", Toast.LENGTH_LONG).show();
                            //Go back to Login
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException invUserEx) {
                                //No user with email exists
                                Toast.makeText(ResetPasswordActivity.this, "No user exists with that email", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    }
                });
            }
        });
    }
}
