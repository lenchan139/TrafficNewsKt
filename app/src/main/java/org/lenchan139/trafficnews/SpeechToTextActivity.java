package org.lenchan139.trafficnews;

import android.*;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechToTextActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient = null;
    private TextView txtSpeechInput;
    private Button btnSpeak, btnUpload;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    final String inputLocate = "zh-HK";
    double lng, lat = -999;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView txtCurrLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (Button) findViewById(R.id.btnSpeak);
        btnUpload = (Button) findViewById(R.id.upload);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationManagerX();
        txtCurrLoc = (TextView) findViewById(R.id.currLocat);
        if(mGoogleApiClient ==null)

        {
            mGoogleApiClient = new GoogleApiClient.Builder(SpeechToTextActivity.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        btnSpeak.setOnClickListener(new View.OnClickListener() {
// Create an instance of GoogleAPIClient.


            @Override
            public void onClick(View v) {
                promptSpeechInput();
                requireLocationCombile();

            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadVoiceMsg(txtSpeechInput.getText().toString());
            }
        });
        requireLocationCombile();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    void requireLocationCombile() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.removeUpdates(locationListener);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener,null);
        try {
            lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            lng = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        }catch (NullPointerException e){
            e.printStackTrace();
            try{

                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER ,locationListener,null);
                lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
                lng = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
            }catch (NullPointerException e2) {
                e2.printStackTrace();
                Toast.makeText(this, "獲取地理位置失敗！", Toast.LENGTH_SHORT);
            }
        }
        Geocoder geocoder = new Geocoder(getBaseContext(),Locale.TRADITIONAL_CHINESE);
        try {
            Address address = geocoder.getFromLocation(lat,lng,1).get(0);
            txtCurrLoc.setText(address.getThoroughfare());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void UploadVoiceMsg(String msg) {
        if (lng != -999 && lat != -999) {
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String key = database.getReference("message").child(txtCurrLoc.getText().toString()).push().getKey();
            database.getReference("message").child(txtCurrLoc.getText().toString()).child(key).child("msg").setValue(msg);
            //database.getReference("message").child(key).child("location").setValue(LocationChecker(msg));
            database.getReference("message").child(txtCurrLoc.getText().toString()).child(key).child("lat").setValue(lat);
            database.getReference("message").child(txtCurrLoc.getText().toString()).child(key).child("lng").setValue(lng);
            Toast.makeText(SpeechToTextActivity.this, "上傳成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SpeechToTextActivity.this, "上傳失敗！無法定位！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "你嘅手機唔支援語音輸入！",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    Log.d("dosth1", "isTrue");
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    Log.v("ResultText", result.get(0));
                    btnUpload.setEnabled(true);
                } else {
                    txtSpeechInput.setText(null);
                    btnUpload.setEnabled(false);
                }
                break;
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            return;
        }
        Location mLastLocation;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lng = mLastLocation.getLongitude();
            Log.v("location",lat + "," + lng);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class LocationManagerX implements LocationListener{
        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "座標變更: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v("longitude: ", longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v("latitude: ", latitude);
            lat = loc.getLatitude();
            lng = loc.getLongitude();
            Geocoder geocoder = new Geocoder(getBaseContext(),Locale.TRADITIONAL_CHINESE);
            try {
                Address address = geocoder.getFromLocation(lat,lng,1).get(0);
                txtCurrLoc.setText(address.getThoroughfare());
                Log.v("locat1",address.getThoroughfare());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
