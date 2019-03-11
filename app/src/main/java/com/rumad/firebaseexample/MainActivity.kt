package com.rumad.firebaseexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var isCreate = false

    lateinit var auth : FirebaseAuth
    lateinit var googleSignInClient : GoogleSignInClient

    val RC_SIGN_IN = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        button.setOnClickListener {
            buttonClickListener()
        }

        button_gsignin.setOnClickListener {
            googleSignIn()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            successCallback(currentUser)
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun buttonClickListener() {
        if(isCreate) {
            auth.createUserWithEmailAndPassword(
                    edittext_email.text.toString(),
                    edittext_pass.text.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Created", Toast.LENGTH_LONG).show()
                        successCallback(it.user)
                    }
                    .addOnFailureListener {
                        failCallback()
                    }
            return
        }

        if(auth.currentUser != null) {
            auth.signOut()
            textview_curr_user.text = resources.getString(R.string.no_user)
            button.text = resources.getString(R.string.prompt_sign_in)
            button_gsignin.visibility = View.VISIBLE

            googleSignInClient.signOut()

            return
        }

        auth.signInWithEmailAndPassword(edittext_email.text.toString(), edittext_pass.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this, "Signed in", Toast.LENGTH_LONG).show()
                    successCallback(auth.currentUser!!)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                }
    }

    fun successCallback(user : FirebaseUser) {
        textview_curr_user.text = "Hello ${user?.email}"
        button.text = resources.getString(R.string.prompt_sign_out)
        button_gsignin.visibility = View.INVISIBLE
    }

    fun failCallback() {
        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.item_toggle -> {
                val signInText = resources.getString(R.string.prompt_sign_in)
                val createText = resources.getString(R.string.prompt_create)

                if(item.title == signInText) {
                    item.title = createText
                    button.text = signInText
                    button_gsignin.visibility = View.VISIBLE
                    isCreate = false
                } else{
                    item.title = signInText
                    button.text = createText
                    button_gsignin.visibility = View.INVISIBLE
                    isCreate = true
                }
                 return true
            }
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.d(javaClass.name, "Google sign in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(javaClass.name, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(javaClass.name, "signInWithCredential:success")
                        successCallback(auth.currentUser!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(javaClass.name, "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_LONG).show()
                    }
                }
    }
}
