package com.example.composer.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.composer.R
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException


class LoginActivity : AppCompatActivity() {

    lateinit var gso: GoogleSignInOptions
    lateinit var gsc: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso)

        findViewById<AppCompatButton>(R.id.signInBtn).setOnClickListener {
            signIn()
        }
        findViewById<Button>(R.id.guestLogin).setOnClickListener {
            navigateToMain()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.statusBarColor = Color.parseColor("#121C32")
        WindowInsetsControllerCompat(window,
            window.decorView
        ).isAppearanceLightStatusBars = false;


    }

    private fun signIn() {
        val signInIntent: Intent = gsc.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    task.getResult()
                    navigateToMain()
                } catch (e: ApiException) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun navigateToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

}
