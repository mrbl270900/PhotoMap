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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button minknap;
    Button minknap2;
    Button minknap3;
    Button minknap4;
    Button minknap5;
    EditText input;
    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

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

    @Override
    public void onClick(View v){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(v == minknap){
            if (user != null) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Log ind for at se kortet", Toast.LENGTH_SHORT);
                toast.show();
            }
        }else if(v == minknap2){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Du er logget ud", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
        }else if(v == minknap3){
            // Create and launch sign-in intent
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        }else if(v == minknap4){
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
            }else if(v == minknap5){
                input.setText(user.getUid());
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Log ind for at se kortet", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


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
