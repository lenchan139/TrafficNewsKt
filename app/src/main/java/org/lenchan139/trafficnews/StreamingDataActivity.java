package org.lenchan139.trafficnews;

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.lenchan139.trafficnews.Class.MapApiUrlHandler;
import org.lenchan139.trafficnews.Class.MsgItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StreamingDataActivity extends AppCompatActivity {
    DatabaseReference dref;
    ListView listview;
    Button btnTtsOnOrOff;
    EditText edtTtsOnOrOff;
    ArrayList<String> list=new ArrayList<>();
    TextToSpeech tts;
    double lat = -999,lng = -999;
    LocationManager locationManager;
    LocationListener locationListener;
    ChildEventListener cel;
    ArrayList<MsgItem> locatList = new ArrayList<>();
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_data);
        listview=(ListView)findViewById(R.id.listview);
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,list);
        listview.setAdapter(adapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationManagerX();
        btnTtsOnOrOff = (Button) findViewById(R.id.btnOnOrOff);
        edtTtsOnOrOff = (EditText) findViewById(R.id.edtTtsOnOrOff);
        sp = getSharedPreferences("this",0);

        btnTtsOnOrOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getBoolean("ttsOn",true)){
                    sp.edit().putBoolean("ttsOn",false).commit();
                    edtTtsOnOrOff.setText("自動語音：閂");
                }else{
                    sp.edit().putBoolean("ttsOn",true).commit();
                    edtTtsOnOrOff.setText("自動語音：開");
                }
            }
        });
        tts = new TextToSpeech(StreamingDataActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.forLanguageTag("yue-Hant-HK"));
                }
            }
        });
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tts.speak(list.get(position), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        requireLocationCombile();
        dref= FirebaseDatabase.getInstance().getReference("message");
        dref.addChildEventListener(cel = new ChildEventListener()  {
            int count = 0;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                count++;
                String txtTts = dataSnapshot.child("msg").getValue(String.class);
                requireLocationCombile();
                double g = dataSnapshot.child("lng").getValue(double.class);
                double t = dataSnapshot.child("lat").getValue(double.class);
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.TRADITIONAL_CHINESE);
                String oldThor = null,newThor = null;
                    try {
                        List<Address> oldAdd = gcd.getFromLocation(t, g, 1);
                        oldThor = oldAdd.get(0).getThoroughfare();
                        List<Address> newAdd = gcd.getFromLocation(lat, lng, 1);
                        newThor = newAdd.get(0).getThoroughfare();

                        Log.d("json Old", oldAdd.get(0).getThoroughfare());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch (NullPointerException e){
                       e.printStackTrace();
                    }
                if(Objects.equals(oldThor, newThor) && oldThor != null && newThor!=null){

                list.add(txtTts);
                if(count >= dataSnapshot.getChildrenCount()){
                    if(sp.getBoolean("ttsOn",true)) {
                        tts.speak(list.get(list.size() - 1), TextToSpeech.QUEUE_FLUSH, null);

                        edtTtsOnOrOff.setText("自動語音：開");
                    }else{

                        edtTtsOnOrOff.setText("自動語音：閂");
                    }
                }else{

                }
                adapter.notifyDataSetChanged();
                listview.smoothScrollToPosition(listview.getAdapter().getCount()-1);

                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                list.remove(dataSnapshot.child("msg").getValue(String.class));
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        tts.stop();
}

    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();

        }
        finish();
        super.onPause();
    }

    class LocationManagerX implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "座標更變: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v("longitude: ", longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v("latitude: ", latitude);
            lat = loc.getLatitude();
            lng = loc.getLongitude();

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

    void requireLocationCombile(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }
        locationManager.removeUpdates(locationListener);
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
        Log.v("location",lat +"，" + lng);
    }

    @Override
    public void onBackPressed() {
        dref.removeEventListener(cel);
        finish();
        super.onBackPressed();
    }
}
