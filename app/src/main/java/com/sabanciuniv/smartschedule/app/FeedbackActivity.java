package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class FeedbackActivity extends AppCompatActivity {
    private String feedbackText;
    private String username;
    private TextInputEditText feedback_box;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Intent intent = getIntent();
        feedback_box = findViewById(R.id.feedback_input);
        Button button = findViewById(R.id.button_feedback);

        username = mAuth.getUid();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedbackText = feedback_box.getText().toString();
                if(!feedbackText.equals("")){
                    sendFeedback(feedbackText, username);
                }
            }
        });
    }

    private void sendFeedback(String feedbackText, String username) {
        Intent feedIntent = new Intent(Intent.ACTION_SEND);
        feedIntent.setType("message/html");
        feedIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dilara.a96@gmail.com"});
        feedIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback from Smort - Smart Scheduler");
        feedIntent.putExtra(Intent.EXTRA_TEXT, "Name: "+ username + "\nMessage: " + feedbackText);
        try {
            startActivity(Intent.createChooser(feedIntent, "Send feedback..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
