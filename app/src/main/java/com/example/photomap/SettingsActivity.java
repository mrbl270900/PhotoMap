package com.example.photomap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    Button knap1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        knap1 = findViewById(R.id.button4);
        knap1.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == knap1){
            System.out.println("test");
        }
    }
}