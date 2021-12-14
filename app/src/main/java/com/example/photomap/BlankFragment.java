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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BlankFragment extends Fragment implements View.OnClickListener {
    View view;
    String url;
    Button sletKnap;
    MapsActivity m;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public BlankFragment(String title, MapsActivity mapsActivity) {
        url = title;
        m = mapsActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_blank, container, false);
        sletKnap = view.findViewById(R.id.button4);
        sletKnap.setOnClickListener(this);
        return view;
    }

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
            this.getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}