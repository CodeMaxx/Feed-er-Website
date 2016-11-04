package com.homebrew.feed_er;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AssignmentDetail extends AppCompatActivity {

    String token;
    int pk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        Intent intent = getIntent();
        token = intent.getExtras().getString("token");
        pk = intent.getExtras().getInt("pk");

        TextView textView = (TextView) findViewById(R.id.assignmentName);
        textView.setText(token + "\n" + Integer.toString(pk));
    }
}
