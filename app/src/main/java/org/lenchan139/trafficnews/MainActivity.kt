package org.lenchan139.trafficnews

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    internal var edtEmail: EditText? =null
    internal var edtPassword: EditText? = null
    internal var btnLogin: Button? = null
    internal var btnCreateUser: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        edtEmail = findViewById(R.id.email) as EditText
        edtPassword = findViewById(R.id.password) as EditText
        btnCreateUser = findViewById(R.id.newAccount) as Button
        btnLogin = findViewById(R.id.login) as Button
        val fab = findViewById(R.id.fab) as FloatingActionButton
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                val intent = Intent(this@MainActivity, MainMenuActivity::class.java)
                startActivity(intent)
                finish()
                //Toast.makeText(MainActivity.this,"Login Successfully!",Toast.LENGTH_SHORT).show();
            } else {
                // User is signed out
                //Toast.makeText(MainActivity.this,"User isn't login yet!",Toast.LENGTH_SHORT).show();
            }
            // ...
        }
        fab.setOnClickListener { LoginButtonFunction() }
        btnLogin!!.setOnClickListener { LoginButtonFunction() }

        btnCreateUser!!.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        //onCreate End
    }

    internal fun LoginButtonFunction() {
        val email1 = edtEmail!!.text.toString()
        val password1 = edtPassword!!.text.toString()
        if (email1.isEmpty() || password1.isEmpty()) {
            Toast.makeText(this@MainActivity, "請輸入帳號密碼！", Toast.LENGTH_SHORT).show()

        } else {
            Login(email1, password1)
        }
    }

    internal fun Login(email: String, password: String) {

        val TAG = "yo"
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@MainActivity) { task ->
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithEmail:failed", task.exception)
                        Toast.makeText(this@MainActivity, "登入失敗!",
                                Toast.LENGTH_SHORT).show()
                    }

                    // ...
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
