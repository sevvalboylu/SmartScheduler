package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity  extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private EditText email;
    private EditText password;
    private String email_text;
    private String password_text;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "";


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("545871567838-9bnlmgh0nofbpevbuvl583d7g4l9fv4a.apps.googleusercontent.com")
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        Boolean signedOut= getIntent().getBooleanExtra("signedOut",false);
        if(signedOut) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    loadIntent();
                }
            });
        }

        SharedPreferences sharedPref = SignInActivity.this.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String lastEmail = sharedPref.getString("lastEmail", "");
        String lastpwd = sharedPref.getString("lastPassword", "");

        if(acc!=null)
            firebaseAuthWithGoogle(acc);

        else if (lastEmail != "" )
        {
            mAuth.signInWithEmailAndPassword(lastEmail, lastpwd);
            Intent intent = new Intent(SignInActivity.this, BasicActivity.class);
            startActivity(intent);
        }

        else
            loadIntent();
    }
    private void loadIntent(){

        //google sign in

        setContentView(R.layout.activity_signin);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.editText_password);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }

        });

        Button button = findViewById(R.id.button_signIn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                password_text = password.getText().toString();
                email_text = email.getText().toString();
                if(!email_text.equals("") && !password_text.equals("")) {
                    mAuth.signInWithEmailAndPassword(email_text, password_text).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent intent = new Intent(SignInActivity.this, BasicActivity.class);
                                SharedPreferences sharedPref = SignInActivity.this.getSharedPreferences("loginData", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("lastEmail", String.valueOf(user.getEmail()));
                                editor.putString("lastPassword", String.valueOf(password_text));
                                editor.commit();
                                startActivity(intent);

                            } else {
                                Toast.makeText(SignInActivity.this, "Login failed!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(SignInActivity.this,"Please provide email and password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToSignup(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        Intent intent = new Intent(SignInActivity.this, BasicActivity.class);
        startActivity(intent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    Intent intent = new Intent(SignInActivity.this, BasicActivity.class);
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    loadIntent();
                    Toast.makeText(getApplicationContext(), "Couldn't sign in with last signed in account", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
}

