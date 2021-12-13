package com.example.photomap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    private ArrayList<Marker> markerList;
    private ArrayList<Uri> urlList;
    SearchView searchView;
    Button minknap;
    double latFinal;
    double lngFinal;
    boolean noGPS = false;
    Uri selectedImageUri;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("images");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // dette skal måske byttes ud da det gør vores app onresponciv
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        com.example.photomap.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pictureUri = new ArrayList<>();
        markerList = new ArrayList<>();
        urlList = new ArrayList<Uri>();
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
                    if(noGPS) {
                        noGPS = false;
                        StorageReference picFromUser = imagesRef.child(selectedImageUri.getLastPathSegment());
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("image/jpg")
                                .setCustomMetadata("lng", String.valueOf(address.getLongitude()))
                                .setCustomMetadata("lat", String.valueOf(address.getLatitude()))
                                .build();
                        UploadTask uploadTask = picFromUser.putFile(selectedImageUri, metadata);

                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast toast = Toast.makeText(getApplicationContext(), "upload er ikke lykkedes", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast toast = Toast.makeText(getApplicationContext(), "upload er lykkedes", Toast.LENGTH_SHORT);
                                toast.show();
                                picFromUser.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                {
                                    @Override
                                    public void onSuccess(Uri downloadUrl)
                                    {
                                        urlList.add(downloadUrl);
                                        String name = String.valueOf(downloadUrl);
                                        markerList.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).draggable(true)));
                                    }
                                });
                            }
                        });

                    }else{
                        searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("searchMarker"));
                    }
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
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, SELECT_IMAGE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                selectedImageUri = data.getData();
                String selectedImagePath = selectedImageUri.getPath();
                System.out.println("Image Path : " + selectedImagePath);
                StorageReference picFromUser = imagesRef.child(selectedImageUri.getLastPathSegment());


                try (InputStream inputStream = this.getContentResolver().openInputStream(selectedImageUri)) {
                    ExifInterface exif = new ExifInterface(inputStream);
                    String lat = ExifInterface.TAG_GPS_LATITUDE;
                    String lng = ExifInterface.TAG_GPS_LONGITUDE;
                    String latData = exif.getAttribute(lat);
                    String lngData = exif.getAttribute(lng);
                    try {
                        if (!latData.equals(null)) {
                            System.out.println(latData);
                            String convertetLatData = latData.replaceAll("/", ",");
                            String[] latArray = convertetLatData.split(",");
                            Double nDegree = Double.parseDouble(latArray[0]);
                            Double nDegreeDenumurator = Double.parseDouble(latArray[1]);
                            Double nMinute = Double.parseDouble(latArray[2]);
                            Double nMinuteDenumurator = Double.parseDouble(latArray[3]);
                            Double nSecond = Double.parseDouble(latArray[4]);
                            Double nSecondDenumurator = Double.parseDouble(latArray[5]);
                            latFinal = nDegree / nDegreeDenumurator + (nMinute / 60) / nMinuteDenumurator + (nSecond / 3600) / nSecondDenumurator;
                            System.out.println(latFinal);
                        }
                        if (!lngData.equals(null)) {
                            System.out.println(lngData);
                            String convertetLngData = lngData.replaceAll("/", ",");
                            String[] lngArray = convertetLngData.split(",");
                            Double eDegree = Double.parseDouble(lngArray[0]);
                            Double eDegreeDenumurator = Double.parseDouble(lngArray[1]);
                            Double eMinute = Double.parseDouble(lngArray[2]);
                            Double eMinuteDenumurator = Double.parseDouble(lngArray[3]);
                            Double eSecond = Double.parseDouble(lngArray[4]);
                            Double eSecondDenumurator = Double.parseDouble(lngArray[5]);
                            lngFinal = eDegree / eDegreeDenumurator + (eMinute / 60) / eMinuteDenumurator + (eSecond / 3600) / eSecondDenumurator;
                            System.out.println(lngFinal);
                            LatLng picLatLng = new LatLng(latFinal, lngFinal);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(picLatLng, 14.0f));
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setContentType("image/jpg")
                                    .setCustomMetadata("lng", String.valueOf(lngFinal))
                                    .setCustomMetadata("lat", String.valueOf(latFinal))
                                    .build();
                            UploadTask uploadTask = picFromUser.putFile(selectedImageUri, metadata);

                            // Register observers to listen for when the download is done or if it fails
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "upload er ikke lykkedes", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "upload er lykkedes", Toast.LENGTH_SHORT);
                                    toast.show();
                                    picFromUser.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast toast = Toast.makeText(getApplicationContext(), "Url kunne ikke findes", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Uri>()
                                    {
                                        @Override
                                        public void onSuccess(Uri downloadUrl)
                                        {
                                            urlList.add(downloadUrl);
                                            System.out.println(downloadUrl);
                                            String name = String.valueOf(downloadUrl);
                                            markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name)));
                                        }
                                    });
                                }
                            });
                            pictureUri.add(selectedImageUri);
                        }
                    }catch (NullPointerException j){
                        j.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(), "Dette billede har ingen GPS data søg efter lokation", Toast.LENGTH_SHORT);
                        toast.show();
                        pictureUri.add(selectedImageUri);
                        noGPS = true;
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
            if (marker.getTitle().equals("searchMarker")) {
                Toast toast = Toast.makeText(getApplicationContext(), "Du har trykket på searchmarkeren", Toast.LENGTH_SHORT);
                toast.show();
                ((ImageView) view.findViewById(R.id.badge)).setImageURI(null);
            }else {
                String url = marker.getTitle();
                try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    Drawable d = Drawable.createFromStream(is, "src name");
                    ((ImageView) view.findViewById(R.id.badge)).setImageDrawable(d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        float zoomLevel = 14.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RUC, zoomLevel));
        mMap.setOnMarkerClickListener(this);
        System.out.println("her");
        imagesRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            System.out.println(String.valueOf(item));
                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    LatLng picLatLng = new LatLng(Double.parseDouble(storageMetadata.getCustomMetadata("lat")), Double.parseDouble(storageMetadata.getCustomMetadata("lng")));
                                    item.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast toast = Toast.makeText(getApplicationContext(), "url kunnne ikke findes", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Uri>()
                                    {
                                        @Override
                                        public void onSuccess(Uri downloadUrl)
                                        {
                                            String name = String.valueOf(downloadUrl);
                                            markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name)));
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Problemer med at hente metadata fra server", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(), "Problemer med at hente data fra server", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
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