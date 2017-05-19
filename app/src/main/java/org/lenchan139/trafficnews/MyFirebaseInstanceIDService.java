package org.lenchan139.trafficnews;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("tokenUpdated", "Refreshed token: " + refreshedToken);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String key = database.getReference("users").push().getKey();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(uid != null && token != null) {
            database.getReference("users").child(uid).child("token").setValue(token);
            database.getReference("users").child(uid).child("email").setValue(email);

        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }
}
