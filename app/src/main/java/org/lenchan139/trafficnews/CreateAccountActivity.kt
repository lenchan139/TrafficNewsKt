package org.lenchan139.trafficnews

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import java.util.Objects

class CreateAccountActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal var edtEmail: EditText? = null
    internal var edtPassword: EditText? = null
    internal var edtConfirmPassword: EditText? = null
    internal var btnCreate: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        mAuth = FirebaseAuth.getInstance()
        edtConfirmPassword = findViewById(R.id.confirmPassword) as EditText
        edtEmail = findViewById(R.id.email) as EditText
        edtPassword = findViewById(R.id.password) as EditText
        btnCreate = findViewById(R.id.btnCreate) as Button

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d("", "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d("", "onAuthStateChanged:signed_out")

            }
            // ...
        }

        btnCreate!!.setOnClickListener {
            val email1 : String = edtEmail!!.text.toString()
            val password1 = edtPassword!!.text.toString()
            val cPassword1 = edtConfirmPassword!!.text.toString()
            if (password1 != cPassword1) {
                Toast.makeText(this@CreateAccountActivity, "Password not same!", Toast.LENGTH_SHORT).show()
            } else if (email1.isEmpty() || password1.isEmpty()  || cPassword1.isEmpty()) {
                Toast.makeText(this@CreateAccountActivity, "enter your email and password!", Toast.LENGTH_SHORT).show()
            } else {
                CreateNewUser(email1, password1)
            }
        }
        //onCreate End
    }

    internal fun CreateNewUser(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d("", "createUserWithEmail:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Toast.makeText(this@CreateAccountActivity, "Create Account Failed!",
                                Toast.LENGTH_SHORT).show()
                        Log.d("CreateUserFailed", task.toString())
                    } else {
                        Toast.makeText(this@CreateAccountActivity, "Account Created!",
                                Toast.LENGTH_SHORT).show()
                        finish()
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

}
