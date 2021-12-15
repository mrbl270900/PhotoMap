package com.example.photomap;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BlankFragment extends Fragment implements View.OnClickListener {
    //her definere vi vores variabler knap og kort
    View view;
    String url;
    Button sletKnap;
    MapsActivity m;
    boolean open = false;

    //dette er vores constructor som anvendes til at definere hvilken marker som brugeren har trykket på og hvilket kort de kommer fra
    public BlankFragment(String title, MapsActivity mapsActivity) {
        url = title;
        m = mapsActivity;
    }

    //her sættes vores savedInstanceState
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //her fylder vi vores container med vores layout samt sætter vi vores knap
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_blank, container, false);
        sletKnap = view.findViewById(R.id.button4);
        sletKnap.setOnClickListener(this);
        open = true;
        return view;
    }

    //her tjekker vi om der er trykket på vores knap og hvis der er så sletter vi det billede der er på markeren
    //fra databasen samt kalder en funktion fra vores mapsactivity til at slette markeren
    //og lukke dette fragment
    @Override
    public void onClick(View view) {
        if(view == sletKnap){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    m.removeMarker(url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    System.out.println("fail");
                }
            });
            open = false;
            this.getFragmentManager().beginTransaction().remove(this).commit();
        }
    }


    //denne funktion lukker fragmentet hvis open er sand
    public void close(){
        if(open) {
            open = false;
            this.getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}