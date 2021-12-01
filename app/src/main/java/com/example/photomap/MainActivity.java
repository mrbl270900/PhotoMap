package com.example.photomap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button minknap;
    TextView hello;
    Button minknap2;
    Button minknap3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minknap = findViewById(R.id.button);
        minknap.setOnClickListener(this);

        minknap2 = findViewById(R.id.button2);
        minknap2.setOnClickListener(this);

        minknap3 = findViewById(R.id.button3);
        minknap3.setOnClickListener(this);

        hello = findViewById(R.id.textView);

    }

    @Override
    public void onClick(View v){
        if(v == minknap){
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }else if(v == minknap2){
            hello.setText("hej med dig");
        }else if(v == minknap3){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    }
}