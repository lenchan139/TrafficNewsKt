package org.lenchan139.trafficnews

import android.*
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import java.io.IOException
import java.util.ArrayList
import java.util.Locale

class SpeechToTextActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    internal var mGoogleApiClient: GoogleApiClient? = null
    private var txtSpeechInput: TextView? = null
    private var btnSpeak: Button? = null
    private var btnUpload: Button? = null
    private val REQ_CODE_SPEECH_INPUT = 100
    internal val inputLocate = "zh-HK"
    internal var lng: Double = 0.toDouble()
    internal var lat = -999.0
    internal var locationManager: LocationManager? = null
    internal var locationListener: LocationListener? = null
    internal var txtCurrLoc: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_to_text)
        txtSpeechInput = findViewById(R.id.txtSpeechInput) as TextView
        btnSpeak = findViewById(R.id.btnSpeak) as Button
        btnUpload = findViewById(R.id.upload) as Button
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationManagerX()
        txtCurrLoc = findViewById(R.id.currLocat) as TextView
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this@SpeechToTextActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
        btnSpeak!!.setOnClickListener() // Create an instance of GoogleAPIClient.

        {
            promptSpeechInput()
            requireLocationCombile()
        }
        btnUpload!!.setOnClickListener { UploadVoiceMsg(txtSpeechInput!!.text.toString()) }
        requireLocationCombile()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    internal fun requireLocationCombile() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager!!.removeUpdates(locationListener)
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
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
                Toast.makeText(this, "獲取地理位置失敗！", Toast.LENGTH_SHORT).show()
            }

        }

        val geocoder = Geocoder(baseContext, Locale.TRADITIONAL_CHINESE)
        try {
            val address = geocoder.getFromLocation(lat, lng, 1).get(0)
            txtCurrLoc!!.text = address.thoroughfare
            Log.v("currLoc",address.thoroughfare)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    internal fun UploadVoiceMsg(msg: String) {
        if (lng != -999.0 && lat != -999.0) {
            // Write a message to the database
            val database = FirebaseDatabase.getInstance()
            val key = database.getReference("message").child(txtCurrLoc!!.text.toString()).push().key
            database.getReference("message").child(txtCurrLoc!!.text.toString()).child(key).child("msg").setValue(msg)
            //database.getReference("message").child(key).child("location").setValue(LocationChecker(msg));
            database.getReference("message").child(txtCurrLoc!!.text.toString()).child(key).child("lat").setValue(lat)
            database.getReference("message").child(txtCurrLoc!!.text.toString()).child(key).child("lng").setValue(lng)
            Toast.makeText(this@SpeechToTextActivity, "上傳成功!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@SpeechToTextActivity, "上傳失敗！無法定位！", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say something")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext,
                    "你嘅手機唔支援語音輸入！",
                    Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Receiving speech input
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    Log.d("dosth1", "isTrue")
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    txtSpeechInput!!.text = result[0]
                    Log.v("ResultText", result[0])
                    btnUpload!!.isEnabled = true
                } else {
                    txtSpeechInput!!.text = null
                    btnUpload!!.isEnabled = false
                }
            }
        }
    }

    override fun onConnected(bundle: Bundle?) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 2)
            return
        }
        val mLastLocation: Location?
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient)
        if (mLastLocation != null) {
            lat = mLastLocation.latitude
            lng = mLastLocation.longitude
            Log.v("location", lat.toString() + "," + lng)
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    internal inner class LocationManagerX : LocationListener {
        override fun onLocationChanged(loc: Location) {
            Toast.makeText(
                    baseContext,
                    "座標變更: Lat: " + loc.latitude + " Lng: "
                            + loc.longitude, Toast.LENGTH_SHORT).show()
            val longitude = "Longitude: " + loc.longitude
            Log.v("longitude: ", longitude)
            val latitude = "Latitude: " + loc.latitude
            Log.v("latitude: ", latitude)
            lat = loc.latitude
            lng = loc.longitude
            val geocoder = Geocoder(baseContext, Locale.TRADITIONAL_CHINESE)
            try {
                val address = geocoder.getFromLocation(lat, lng, 1)[0]
                txtCurrLoc!!.text = address.thoroughfare
                Log.v("locat1", address.thoroughfare)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    override fun onStart() {
        mGoogleApiClient!!.connect()
        super.onStart()
    }

    override fun onStop() {
        mGoogleApiClient!!.disconnect()
        super.onStop()
    }

}
