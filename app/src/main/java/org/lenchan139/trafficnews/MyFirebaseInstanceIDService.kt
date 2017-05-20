package org.lenchan139.trafficnews

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("tokenUpdated", "Refreshed token: " + refreshedToken!!)

        val database = FirebaseDatabase.getInstance()
        val key = database.getReference("users").push().key
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val token = FirebaseInstanceId.getInstance().token
        val email = FirebaseAuth.getInstance().currentUser!!.email
        if (uid != null && token != null) {
            database.getReference("users").child(uid).child("token").setValue(token)
            database.getReference("users").child(uid).child("email").setValue(email)

        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }
}
