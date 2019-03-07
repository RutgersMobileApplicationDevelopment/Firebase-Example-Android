package com.rumad.firebaseexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var isCreate = false

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            successCallback()
        }

        button.setOnClickListener {
            buttonClickListener()
        }
    }

    fun buttonClickListener() {
        if(isCreate) {
            auth.createUserWithEmailAndPassword(
                    edittext_email.text.toString(),
                    edittext_pass.text.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Created", Toast.LENGTH_LONG).show()
                        successCallback()
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
            return
        }

        auth.signInWithEmailAndPassword(edittext_email.text.toString(), edittext_pass.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this, "Signed in", Toast.LENGTH_LONG).show()
                    successCallback()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                }
    }

    fun successCallback() {
        textview_curr_user.text = "Hello ${auth.currentUser?.email}"
        button.text = resources.getString(R.string.prompt_sign_out)
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
                    isCreate = false
                } else{
                    item.title = signInText
                    button.text = createText
                    isCreate = true
                }
                 return true
            }
        }

        return false
    }
}
