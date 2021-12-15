package com.example.photomap;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //her definere vi vores knapper og vores editText som er vores input i denne activity
    Button minknap;
    Button minknap2;
    Button minknap3;
    Button minknap4;
    Button minknap5;
    EditText input;
    //her definere vi vores singletonList som bruges til vores log ind og log ud gennem firebase
    List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());

    //her starter vores pogram med at override onCreate som bliver sat i gang når activiteten først starter
    //i denne funktion henter vi hvad vi skal se på skærmen og vores knapper bliver sat op
    //her sætters vores input felt også op
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

        minknap4 = findViewById(R.id.button7);
        minknap4.setOnClickListener(this);

        minknap5 = findViewById(R.id.button5);
        minknap5.setOnClickListener(this);

        input = findViewById(R.id.plain_text_input);


        //her tjekker vi om vores bruger er logget ind og hvis de er sker der intet og hvis de ikke er
        //sender vi dem til en log ind skærm
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
        } else {
            // No user is signed in
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        }
    }

    //her nede overrider vi vores onClick funktion denne funktion bliver kaldt når brugeren kliker på en knap
    @Override
    public void onClick(View v){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //her tjekker vi med if og if else statments hvilken knap brugeren har trykket på
        if(v == minknap){
            //dette er skift til kort knappen som sender brugeren til deres eget kort hvis de er logget ind
            //ellers får de en besked om at de skal logge ind
            if (user != null) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Log ind for at se kortet", Toast.LENGTH_SHORT);
                toast.show();
            }
        }else if(v == minknap2){
            //dette er log ud knappen som kalder en firebase funktion der logger brugeren ud og når de er logget ud
            //fortælles brugeren dette
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Du er logget ud", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
        }else if(v == minknap3){
            //dette er log ind knappen denne sender brugeren til en log ind side
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        }else if(v == minknap4){
            //Denne kanp er til at gå til en andens kort ud fra et kortid i input feltet og hvis kortid
            //er kortere eller længere end 28 chars så fortælles brugeren at den skal være 28 chars
            //Brugeren skal også være logget ind for at se andres kort
            if (user != null) {
                if(input.getText().toString().length()==28){
                    String mapID = input.getText().toString();
                    Intent sendIntent = new Intent(MainActivity.this, MapsActivity.class);
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mapID);
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "KortId skal være 28 karaktere", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Log ind for at se kortet", Toast.LENGTH_SHORT);
                toast.show();
            }
        }else if(v == minknap5){
            //denne knap henter dit UID som vi andvender til kortid og sætter det i input feltet hvis brugeren er logget ind
            if (user != null) {
                input.setText(user.getUid());
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Log ind for at få kortID", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    //dette er til log ind og ud for firebase
    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Dit log ind fejlede", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
