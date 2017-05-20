package org.lenchan139.trafficnews

import android.*
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import org.jsoup.Jsoup
import org.lenchan139.trafficnews.Class.MapApiUrlHandler
import org.lenchan139.trafficnews.Class.MsgItem

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList
import java.util.Locale
import java.util.Objects

class StreamingDataActivity : AppCompatActivity() {
    internal var dref: DatabaseReference? = null
    internal var listview: ListView? = null
    internal var btnTtsOnOrOff: Button? = null
    internal var edtTtsOnOrOff: EditText? = null
    internal var list = ArrayList<String>()
    internal var lastLocat: String? = null
    internal var tts: TextToSpeech? = null
    internal var lat = -999.0
    internal var lng = -999.0
    internal var locationManager: LocationManager? = null
    internal var locationListener: LocationListener? = null
    internal var cel: ChildEventListener? = null
    internal var locatList = ArrayList<MsgItem>()
    internal var sp: SharedPreferences? = null
    internal var adapter: ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming_data)
        listview = findViewById(R.id.listview) as ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
        listview!!.adapter = adapter
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationManagerX()
        btnTtsOnOrOff = findViewById(R.id.btnOnOrOff) as Button
        edtTtsOnOrOff = findViewById(R.id.edtTtsOnOrOff) as EditText
        sp = getSharedPreferences("this", 0)

        btnTtsOnOrOff!!.setOnClickListener {
            if (sp!!.getBoolean("ttsOn", true)) {
                sp!!.edit().putBoolean("ttsOn", false).commit()
                edtTtsOnOrOff!!.setText("自動語音：閂")
            } else {
                sp!!.edit().putBoolean("ttsOn", true).commit()
                edtTtsOnOrOff!!.setText("自動語音：開")
            }
        }
        tts = TextToSpeech(this@StreamingDataActivity, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.forLanguageTag("yue-Hant-HK")
            }
        })
        listview!!.setOnItemClickListener(  AdapterView.OnItemClickListener() { parent, view, position, id ->
            tts!!.speak(list[position], TextToSpeech.QUEUE_ADD, null)
        })

        requireLocationCombile()
        tts!!.stop()
    }

    override fun onPause() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()

        }

        dref!!.removeEventListener(cel)
        finish()
        super.onPause()
    }

    internal inner class LocationManagerX : LocationListener {

        override fun onLocationChanged(loc: Location) {
            Toast.makeText(
                    baseContext,
                    "座標更變: Lat: " + loc.latitude + " Lng: "
                            + loc.longitude, Toast.LENGTH_SHORT).show()
            val longitude = "Longitude: " + loc.longitude
            Log.v("longitude: ", longitude)
            val latitude = "Latitude: " + loc.latitude
            Log.v("latitude: ", latitude)
            lat = loc.latitude
            lng = loc.longitude

        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

     fun requireLocationCombile() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager!!.removeUpdates(locationListener)
        locationManager!!.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)
        try {
            lat = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER).latitude
            lng = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER).longitude
        } catch (e: NullPointerException) {
            e.printStackTrace()
            try {

                locationManager!!.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null)
                lat = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).latitude
                lng = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).longitude
            } catch (e2: NullPointerException) {
                e2.printStackTrace()
                Toast.makeText(this, "獲取地理位置失敗！", Toast.LENGTH_SHORT)
            }

        }

        Log.v("location", lat.toString() + "，" + lng)
        val gcd = Geocoder(baseContext, Locale.TRADITIONAL_CHINESE)
        val oldThor: String? = null
        var newThor: String? = null

            //List<Address> oldAdd = gcd.getFromLocation(t, g, 1);
            // oldThor = oldAdd.get(0).getThoroughfare();
            val newAdd = gcd.getFromLocation(lat, lng, 1).get(0)
            newThor = newAdd.thoroughfare
            lastLocat = newThor

            // Log.d("json Old", oldAdd.get(0).getThoroughfare());
if(lastLocat == null){

}else {

    dref = FirebaseDatabase.getInstance().getReference("message").child(lastLocat)
    cel = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError?) {  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            //To change body of created functions use File | Settings | File Templates.
        }

        internal var count = 0
        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

            count++
            val txtTts = p0!!.child("msg").getValue(String::class.java)

            val g = p0!!.child("lng").getValue(Double::class.javaPrimitiveType)
            val t = p0!!.child("lat").getValue(Double::class.javaPrimitiveType)

            // if(Objects.equals(oldThor, newThor) && oldThor != null && newThor!=null){

            list.add(0, txtTts)
            if (sp!!.getBoolean("ttsOn", true)) {
                tts!!.speak(txtTts, TextToSpeech.QUEUE_FLUSH, null)

                edtTtsOnOrOff!!.setText("自動語音：開")
            } else {

                edtTtsOnOrOff!!.setText("自動語音：閂")
            }

            adapter!!.notifyDataSetChanged()
            listview!!.smoothScrollToPosition(0)
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            list.remove(p0!!.child("msg").getValue(String::class.java))
            adapter!!.notifyDataSetChanged()
        }
    }
    dref!!.addChildEventListener(cel)
}
    }

    override fun onBackPressed() {
        if(cel != null)
        dref!!.removeEventListener(cel)
        finish()
        super.onBackPressed()
    }
}
