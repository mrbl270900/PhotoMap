package com.example.photomap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int SELECT_IMAGE;
    private ImageView img;
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
        img = (ImageView)findViewById(R.id.imageView2);
    }

    @Override
    public void onClick(View v){
        if(v == minknap){
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }else if(v == minknap2){
            hello.setText("hej med dig");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
        }else if(v == minknap3){
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, new SettingsFragment()).commit();

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(selectedImagePath);
                    String lat = ExifInterface.TAG_GPS_LATITUDE;
                    if (!lat.isEmpty()){
                        String lat_data = exif.getAttribute(lat);
                    }
                    String lng = ExifInterface.TAG_GPS_LONGITUDE;
                    if (!lng.isEmpty()) {
                        String lng_data = exif.getAttribute(lng);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                img.setImageURI(selectedImageUri);
            }
        }
    }
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}