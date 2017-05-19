package org.lenchan139.trafficnews;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainMenuActivity extends AppCompatActivity {
    TextView txtEmail;
    Button btnTextToSpeech,btnSpeechToText,btnLogout,btnStreaming;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtEmail = (TextView) findViewById(R.id.userEmail);
        btnSpeechToText = (Button) findViewById(R.id.menuSpeechToText);
        btnTextToSpeech = (Button) findViewById(R.id.menuTextToSpeech);
        btnStreaming = (Button) findViewById(R.id.streamingList);
        btnLogout = (Button) findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.INVISIBLE);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());
                    txtEmail.setText(user.getEmail() );
                } else {
                    // User is signed out
                    Log.d("", "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainMenuActivity.this,MainActivity.class);
                    startActivity(intent);
                    MainMenuActivity.this.finish();
                }
                // ...
            }
        };

        btnSpeechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this,SpeechToTextActivity.class);
                startActivity(intent);
            }
        });
        btnTextToSpeech.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this,TextToSpeechActivity.class);
                startActivity(intent);
            }
        });
        btnStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this,StreamingDataActivity.class);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainMenuActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
