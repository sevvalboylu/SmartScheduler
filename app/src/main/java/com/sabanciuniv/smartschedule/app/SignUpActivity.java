package com.sabanciuniv.smartschedule.app;

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

public class SignUpActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private String email_text;
    private String password_text;
    private final String TAG ="";
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email = findViewById(R.id.editText_email);
        password = findViewById(R.id.editText_password);
        Button button = findViewById(R.id.button_signUp);

        button.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View v) {
                                          mAuth = FirebaseAuth.getInstance();
                                          email_text = email.getText().toString();
                                          password_text = password.getText().toString();
                                          mAuth.createUserWithEmailAndPassword(email_text, password_text).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                              @Override
                                              public void onComplete(@NonNull Task<AuthResult> task) {
                                                  if (task.isSuccessful()) {
                                                      // Sign in success, update UI with the signed-in user's information
                                                      Log.d(TAG, "createUserWithEmail:success");
                                                      FirebaseUser user = mAuth.getCurrentUser();
                                                  } else {
                                                      // If sign in fails, display a message to the user.
                                                      Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                      Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                                          });
                                      }
                                  });

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();

                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });

            }
    }

