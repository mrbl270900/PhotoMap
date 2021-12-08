package com.example.photomap;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.photomap.databinding.ActivityMapsBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final LatLng RUC = new LatLng(55.652330724, 12.137999448);
    private Marker mRUC;
    private String location;
    private LatLng latLng;
    private Marker searchMarker;
    private int SELECT_IMAGE;
    private ArrayList<Uri> pictureUri;
    SearchView searchView;
    Button minknap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pictureUri = new ArrayList<>();
        searchView = findViewById(R.id.idSearchView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        minknap = findViewById(R.id.button6);
        minknap.setOnClickListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || location.equals("")) {
                    // on below line we are creating and initializing a geo coder.
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        // on below line we are getting location from the
                        // location name and adding that location to address list.
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // on below line we are getting the location
                    // from our list a first position.
                    Address address = addressList.get(0);

                    // on below line we are creating a variable for our location
                    // where we will add our locations latitude and longitude.
                    latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // on below line we are adding marker to that position.
                    if (searchMarker != null) {
                        searchMarker.remove();
                    }
                    searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("searchMarker"));

                    // below line is to animate camera to that position.
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == minknap) {
            if (searchMarker != null) {
                searchMarker.remove();
                String name = String.valueOf(pictureUri.size());
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Søg efter en lokation først", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                //File selectedImageFile = getFile(selectedImageUri);
                String selectedImagePath = getPath(selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                pictureUri.add(selectedImageUri);
                ExifInterface exif;
                try {
                    exif = new ExifInterface(getFile(selectedImageUri));
                    String lat = ExifInterface.TAG_GPS_LATITUDE;
                    String lng = ExifInterface.TAG_GPS_LONGITUDE;
                    if (!lat.isEmpty()){
                        String lat_data = exif.getAttribute(lat);
                        System.out.println(lat_data);
                    }
                    if (!lng.isEmpty()) {
                        String lng_data = exif.getAttribute(lng);
                        System.out.println(lng_data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    public File getFile(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, uri.getPath());//create path from uri
        try {
            // Make sure the Pictures directory exists.
            file.getParentFile().mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    public String getPath(Uri uri) {
        File file = new File(uri.getPath());//create path from uri
        String filePath = file.getPath();
        return filePath;
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            Uri badge;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
            if (marker.equals(searchMarker)) {
                Toast toast = Toast.makeText(getApplicationContext(), "du har trykket på searchmarkeren", Toast.LENGTH_SHORT);
                toast.show();
            }else {
                badge = pictureUri.get(Integer.parseInt(marker.getTitle()));
                ((ImageView) view.findViewById(R.id.badge)).setImageURI(badge);
            }

        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        float zoomLevel = 14.0f;
        // Add a marker in Sydney and move the camera

        //mMap.addMarker(new MarkerOptions().position(RUC).title("Marker ved RUC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RUC, zoomLevel));
        mMap.setOnMarkerClickListener(this);
        //mRUC = mMap.addMarker(new MarkerOptions().position(RUC).title("Marker ved RUC").infoWindowAnchor(0.5f, 0.5f));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14.0f));
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.equals(mRUC)) {
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class)); // placeholder sends you to settings
        }

    }
}