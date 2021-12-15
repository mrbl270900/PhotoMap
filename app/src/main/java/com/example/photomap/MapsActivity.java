package com.example.photomap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.content.ClipData;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, View.OnClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveListener {
//her sætter vi vores refarancer og variabler
    private GoogleMap mMap;
    private static final LatLng RUC = new LatLng(55.652330724, 12.137999448);
    private String location;
    private LatLng latLng;
    private Marker searchMarker;
    private ArrayList <Marker> markerList;
    SearchView searchView;
    Button minknap;
    double latFinal;
    double lngFinal;
    boolean noGPS = false;
    boolean ditKort = false;
    private BlankFragment blank;
    Uri selectedImageUri;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference imagesRef;


//dette er hvad bliver lavet når activiteten først bliver startet
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disse 2 linjer kode gør at vores app kan bruges så mange resucer som den behøver til at lave ting
        //dette kan gøre vores app lidt langsom men det var nødvendigt for listall funktionen længere nede
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //her sætter vi vores fragment så vi kan kalde dens funktioner
        blank = new BlankFragment(null, this);
        //her starter vores liste af markeres
        markerList = new ArrayList<>();

        //her tjekker vi om vi har fået et kortid sent med da activitetne blev åbnet hvis der gjorde så
        //bruges denne data som vores imagesRef ellers bruges vores Uid til dette
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                imagesRef = storageRef.child(sharedText);
            }
        }else{
            ditKort = true;
            imagesRef = storageRef.child(user.getUid());
        }

        //her sættes vores layout
        com.example.photomap.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //her finder vi vores searchView
        searchView = findViewById(R.id.idSearchView);

        //her hentes fragmentet for kortet og gøres så vi venter på at kortet er klar
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        //her sætter vi vores knap op og vores searchView gør så man kan søge i det
        minknap = findViewById(R.id.button6);
        minknap.setOnClickListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //dette sker når brugeren søgere på noget
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
                        //her tjekker vi om der er blevet lagt et billede op som ikke har gps data
                        //og gør så at det brugeren søger på er billedes nye gps data
                        noGPS = false;
                        //her bygger vi metadataen som skal være en del af billede
                        StorageReference picFromUser = imagesRef.child(selectedImageUri.getLastPathSegment());
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("image/jpg")
                                .setCustomMetadata("lng", String.valueOf(address.getLongitude()))
                                .setCustomMetadata("lat", String.valueOf(address.getLatitude()))
                                .setCustomMetadata("drag", "true")
                                .build();
                        //her sætter vi vores uploadtask op
                        UploadTask uploadTask = picFromUser.putFile(selectedImageUri, metadata);

                        //her starter vi vores uploadtask og tjekeker om det fejler eller lykkes
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //hvis det fejler fortæller vi det til brugeren
                                Toast toast = Toast.makeText(getApplicationContext(), "upload er ikke lykkedes", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //hvis det lykkes fortæller vi det til brugeren og henter downloadUrlen fra billede
                                Toast toast = Toast.makeText(getApplicationContext(), "upload er lykkedes", Toast.LENGTH_SHORT);
                                toast.show();
                                picFromUser.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                {
                                    @Override
                                    public void onSuccess(Uri downloadUrl)
                                    {
                                        //hvis vi lykkes med at hente downloadurlen så tilføjere vi en ny marker med pos ud fra søgningen
                                        //og med urlen som title i dette tilfælde er den også dragable så brugeren kan flytte den
                                        String name = String.valueOf(downloadUrl);
                                        markerList.add(mMap.addMarker(new MarkerOptions().position(latLng).title(name).draggable(true)));
                                    }
                                });
                            }
                        });

                    }else{
                        //dette sker hvis brugeren ikke har et billede der mangler gps så sætter vi en marker der hvor at brugeren søgte efter og flytter camaraet derhen
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

    //her tjekker vi om vores bruger har trykket på knappen og hvis de har sender vi dem til gallariet for at vælge et billede
    //eller flere billeder hvis de er logget ind
    @Override
    public void onClick(View v) {
        if(v == minknap) {
            if(ditKort == true) {
                Intent getIntent = new Intent();
                getIntent.setType("image/*");
                getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                getIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(getIntent, "Select Image"), 1);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Du kan ikke ligge billeder op på andres kort", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    //dette er hvad sker når brugeren har valgt et billede eller flere
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (data.getData() != null) {
                    //her tjekker vi om data.getdata er null hvis den ikke er så har brugeren kun valgt et billede ellers har de valgt flere
                    //her under sætter vi dataen til en selectedImageUri og ud fra den henter dens path og sætter en StorageReference fra den
                    selectedImageUri = data.getData();
                    String selectedImagePath = selectedImageUri.getPath();
                    StorageReference picFromUser = imagesRef.child(selectedImageUri.getLastPathSegment());

                    // her henter vi billede ud fra vores selectedImageUri og tager dens gps data med ExifInterface
                    try (InputStream inputStream = this.getContentResolver().openInputStream(selectedImageUri)) {
                        ExifInterface exif = new ExifInterface(inputStream);
                        String lat = ExifInterface.TAG_GPS_LATITUDE;
                        String lng = ExifInterface.TAG_GPS_LONGITUDE;
                        String latData = exif.getAttribute(lat);
                        String lngData = exif.getAttribute(lng);
                        try {
                            //alt under her bruges til at omdanne gps dataen fra Degrees Minutes Seconds til Decimal Degrees
                            if (!latData.equals(null)) {
                                String convertetLatData = latData.replaceAll("/", ",");
                                String[] latArray = convertetLatData.split(",");
                                Double nDegree = Double.parseDouble(latArray[0]);
                                Double nDegreeDenumurator = Double.parseDouble(latArray[1]);
                                Double nMinute = Double.parseDouble(latArray[2]);
                                Double nMinuteDenumurator = Double.parseDouble(latArray[3]);
                                Double nSecond = Double.parseDouble(latArray[4]);
                                Double nSecondDenumurator = Double.parseDouble(latArray[5]);
                                latFinal = nDegree / nDegreeDenumurator + (nMinute / 60) / nMinuteDenumurator + (nSecond / 3600) / nSecondDenumurator;
                            }
                            if (!lngData.equals(null)) {
                                String convertetLngData = lngData.replaceAll("/", ",");
                                String[] lngArray = convertetLngData.split(",");
                                Double eDegree = Double.parseDouble(lngArray[0]);
                                Double eDegreeDenumurator = Double.parseDouble(lngArray[1]);
                                Double eMinute = Double.parseDouble(lngArray[2]);
                                Double eMinuteDenumurator = Double.parseDouble(lngArray[3]);
                                Double eSecond = Double.parseDouble(lngArray[4]);
                                Double eSecondDenumurator = Double.parseDouble(lngArray[5]);
                                lngFinal = eDegree / eDegreeDenumurator + (eMinute / 60) / eMinuteDenumurator + (eSecond / 3600) / eSecondDenumurator;

                                //her under sætter vi vores nye data i en LatLng samt flytter kamaraet der til
                                LatLng picLatLng = new LatLng(latFinal, lngFinal);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(picLatLng, 14.0f));
                                //her sætter vi vores metadata til billede op
                                StorageMetadata metadata = new StorageMetadata.Builder()
                                        .setContentType("image/jpg")
                                        .setCustomMetadata("lng", String.valueOf(lngFinal))
                                        .setCustomMetadata("lat", String.valueOf(latFinal))
                                        .setCustomMetadata("drag", "false")
                                        .build();
                                //her undes sendes bilelde til databasen med dens metadata
                                UploadTask uploadTask = picFromUser.putFile(selectedImageUri, metadata);

                                // Register observers to listen for when the download is done or if it fails
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //hvius upload fejler fortælles bruger dette
                                        Toast toast = Toast.makeText(getApplicationContext(), "upload er ikke lykkedes", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //hvis upload lykkes fortælles bruger dette samt så hentes url til billede
                                        Toast toast = Toast.makeText(getApplicationContext(), "upload er lykkedes", Toast.LENGTH_SHORT);
                                        toast.show();
                                        picFromUser.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                //hvis dette fejler fortælles bruger dette
                                                Toast toast = Toast.makeText(getApplicationContext(), "Url kunne ikke findes", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri downloadUrl) {
                                                //hvis det lykkes så laver vi en marker ud fra data med url som navn
                                                String name = String.valueOf(downloadUrl);
                                                markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name)));
                                            }
                                        });
                                    }
                                });
                            }
                        } catch (NullPointerException j) {
                            //her ses der at billede har ingen gps data dette fortælles bruger også sættes notGPS til true så næste gang de søgere sættes deres marker
                            j.printStackTrace();
                            Toast toast = Toast.makeText(getApplicationContext(), "Dette billede har ingen GPS data søg efter lokation", Toast.LENGTH_SHORT);
                            toast.show();
                            noGPS = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    //her under sker det samme som over bare med ClipData da denne bruges hvis der er valgt mere end et billede
                    ClipData clipdata = data.getClipData();
                    for (int i=0; i<clipdata.getItemCount();i++)
                    {
                        selectedImageUri = clipdata.getItemAt(i).getUri();
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
                                            .setCustomMetadata("drag", "false")
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
                                            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri downloadUrl) {
                                                    System.out.println(downloadUrl);
                                                    String name = String.valueOf(downloadUrl);
                                                    markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name)));
                                                }
                                            });
                                        }
                                    });
                                }
                            } catch (NullPointerException j) {
                                j.printStackTrace();
                                Toast toast = Toast.makeText(getApplicationContext(), "Dette billede har ingen GPS data søg efter lokation", Toast.LENGTH_SHORT);
                                toast.show();
                                noGPS = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }


    //her uploader vi vores ændret gpsdata hvis en bruger flytter en dragable maker og de fortælles dette
    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        LatLng pos = marker.getPosition();
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(marker.getTitle());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("lng", String.valueOf(pos.longitude))
                .setCustomMetadata("lat", String.valueOf(pos.latitude))
                .setCustomMetadata("drag", "true")
                .build();
        storageReference.updateMetadata(metadata)
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Opdatere gps data", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Fejl ved at opdatere gps data", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }


    //dette bruges til at se hvis brugeren flytter kortet så slettes blankfragmentet hvis det er åbnet
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCameraMove() {
            blank.close();
    }

    //her sætter vi vores infowindow til hver marker
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;
        private final View mContents;

        //her hentes vores data fra vores infowindow
        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }


        //her kaldes render for windowet
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        //her kaldes render for windowet
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        //her render vi vores infowindow hvis du ikke har trykket på search markereren
        private void render(Marker marker, View view) {
            if (marker.getTitle().equals("searchMarker")) {
                Toast toast = Toast.makeText(getApplicationContext(), "Du har trykket på searchmarkeren", Toast.LENGTH_SHORT);
                toast.show();
                ((ImageView) view.findViewById(R.id.badge)).setImageURI(null);
            }else {
                //her tager vi vores title som en url og henter billede fra denne url og viser det til brugeren
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

    //dette sker når koretet er klar
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //vi sætter vores værdier
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RUC, 14.0f));
        mMap.setOnMarkerClickListener(this);
        //her lister vi alle vores filer i databasen
        imagesRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    //dette sker hvis det lykkes at hente alle filer fra databasen
                    @Override
                    public void onSuccess(ListResult listResult) {
                        //for hver file skal vi gøre noget
                        for (StorageReference item : listResult.getItems()) {
                            //her henter vi metadataen fra filen
                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    //her sætter vi vores LatLng til billede fra metadataen
                                    LatLng picLatLng = new LatLng(Double.parseDouble(Objects.requireNonNull(storageMetadata.getCustomMetadata("lat"))), Double.parseDouble(Objects.requireNonNull(storageMetadata.getCustomMetadata("lng"))));
                                    //her hentes downloadURL fra filen
                                    item.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            //her fortælles brugerne noget gik galt
                                            Toast toast = Toast.makeText(getApplicationContext(), "url kunnne ikke findes", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri downloadUrl) {
                                            //her laver vi en marker ud fra metadata til position og med url som title samt kan den drages hvis dette er i meta dataen
                                            String name = String.valueOf(downloadUrl);
                                            if (storageMetadata.getCustomMetadata("drag").equals("true")) {
                                                markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name).draggable(true)));
                                            } else {
                                                markerList.add(mMap.addMarker(new MarkerOptions().position(picLatLng).title(name)));
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //her fortælles brugerne hvis noget er gået galt med metadata
                                    Toast toast = Toast.makeText(getApplicationContext(), "Problemer med at hente metadata fra server", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //her fortælles brugerne hvis noget er gået galt med at hente fra databasen
                Toast toast = Toast.makeText(getApplicationContext(), "Problemer med at hente data fra server", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //her flyter vi brugeren til den marker som de trykker på og vi åbner markerens infowindow
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14.0f));
        marker.showInfoWindow();
        return true;
    }



    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        //her starter vi slette fragmentet blankfragment hvis brugeren ejere kortet og ellers fortælles de at de ikke ejere kortet
        if(user.getUid().equals(imagesRef.getName())){
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout3, blank = new BlankFragment(marker.getTitle(), this)).commit();
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Du ejer ikke dette kort", Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    //denne funktion kaldes når man fjerne en marker fra kortet
    public void removeMarker(String title){
        for (int i = 0; i < markerList.size(); i++) {
            if(markerList.get(i).getTitle().equals(title)){
                markerList.get(i).remove();
                break;
            }
        }
    }
}