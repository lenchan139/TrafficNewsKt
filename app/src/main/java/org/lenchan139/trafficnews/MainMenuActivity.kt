package org.lenchan139.trafficnews

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

import org.w3c.dom.Text

import java.io.IOException

class MainMenuActivity : AppCompatActivity() {
    internal var txtEmail: TextView? = null
    internal var btnTextToSpeech: Button? = null
    internal var btnSpeechToText: Button? = null
    internal var btnLogout: Button? = null
    internal var btnStreaming: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        txtEmail = findViewById(R.id.userEmail) as TextView
        btnSpeechToText = findViewById(R.id.menuSpeechToText) as Button
        btnTextToSpeech = findViewById(R.id.menuTextToSpeech) as Button
        btnStreaming = findViewById(R.id.streamingList) as Button
        btnLogout = findViewById(R.id.logout) as Button
        mAuth = FirebaseAuth.getInstance()
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        fab.visibility = View.INVISIBLE
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d("", "onAuthStateChanged:signed_in:" + user.uid)
                txtEmail!!.text = user.email
            } else {
                // User is signed out
                Log.d("", "onAuthStateChanged:signed_out")
                val intent = Intent(this@MainMenuActivity, MainActivity::class.java)
                startActivity(intent)
                this@MainMenuActivity.finish()
            }
            // ...
        }

        btnSpeechToText!!.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, SpeechToTextActivity::class.java)
            startActivity(intent)
        }
        btnTextToSpeech!!.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, TextToSpeechActivity::class.java)
            startActivity(intent)
        }
        btnStreaming!!.setOnClickListener {
            val intent = Intent(this@MainMenuActivity, StreamingDataActivity::class.java)
            startActivity(intent)
        }
        btnLogout!!.setOnClickListener {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@MainMenuActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }
}
