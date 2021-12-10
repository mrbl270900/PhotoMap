package com.example.photomap;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.photomap.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private static final LatLng RUC = new LatLng(55.652330724, 12.137999448);
    private String location;
    private LatLng latLng;
    private Marker searchMarker;
    private final int SELECT_IMAGE = 1;
    private ArrayList<Uri> pictureUri;
    SearchView searchView;
    Button minknap;
    double latFinal;
    double lngFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.photomap.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
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
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, SELECT_IMAGE);
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
                String selectedImagePath = selectedImageUri.getPath();
                System.out.println("Image Path : " + selectedImagePath);
                pictureUri.add(selectedImageUri);
                try (InputStream inputStream = this.getContentResolver().openInputStream(selectedImageUri)) {
                    ExifInterface exif = new ExifInterface(inputStream);
                    String lat = ExifInterface.TAG_GPS_LATITUDE;
                    String lng = ExifInterface.TAG_GPS_LONGITUDE;
                    String latData = exif.getAttribute(lat);
                    String lngData = exif.getAttribute(lng);
                    if (!latData.isEmpty()){
                        System.out.println(latData);
                        String convertetLatData = latData.replaceAll(",", ".");
                        String[] latArray = convertetLatData.split("/");
                        for (int i = 0; i < latArray.length; i++) {
                            System.out.println(latArray[i]);
                        }
                        Double nDegree = Double.parseDouble(latArray[0]);
                        Double nMinute = Double.parseDouble(latArray[1]);
                        Double nSecond = Double.parseDouble(latArray[2]);
                        Double nSecDenumurator = Double.parseDouble(latArray[3]);
                        latFinal = nDegree + nMinute/60 + (nSecond/3600)/nSecDenumurator;
                        System.out.println(latFinal);
                    }
                    if (!lngData.isEmpty()) {
                        System.out.println(lngData);
                        String convertetLngData = lngData.replaceAll(",", ".");
                        String[] lngArray = convertetLngData.split("/");
                        for (int i = 0; i < lngArray.length; i++) {
                            System.out.println(lngArray[i]);
                        }
                        Double eDegree = Double.parseDouble(lngArray[0]);
                        Double eMinute = Double.parseDouble(lngArray[1]);
                        Double eSecond = Double.parseDouble(lngArray[2]);
                        Double eSecDenumurator = Double.parseDouble(lngArray[3]);
                        lngFinal = eDegree + eMinute/60 + (eSecond/3600)/eSecDenumurator;
                        System.out.println(lngFinal);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
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
                Toast toast = Toast.makeText(getApplicationContext(), "Du har trykket på searchmarkeren", Toast.LENGTH_SHORT);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RUC, zoomLevel));
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14.0f));
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        /*if (marker.equals(mRUC)) {
            startActivity(new Intent(MapsActivity.this, SettingsActivity.class)); // placeholder sends you to settings
        }*/

    }
}