package com.example.composer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.composer.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseMessaging.getInstance().subscribeToTopic("postLiked")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val user = GoogleSignIn.getLastSignedInAccount(this)

        bottomNavigationView.setupWithNavController(navController)
        if (user == null) {
            val menu = bottomNavigationView.menu
            val logoutGuest = menu.findItem(R.id.return_login)
            logoutGuest.isVisible = true
            val logOutItem = menu.findItem(R.id.profileInfoFragment)
            logOutItem.isVisible = false

            bottomNavigationView.menu.findItem(R.id.return_login).setOnMenuItemClickListener {
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                true
            }
        }
        bottomNavigationView.menu.findItem(R.id.profileInfoFragment).setOnMenuItemClickListener {
            Navigation.findNavController(this, R.id.fragmentContainerView)
                .popBackStack(R.id.profileInfoFragment, false)
            super.onOptionsItemSelected(bottomNavigationView.menu.findItem(R.id.profileInfoFragment))
        }

//
//        findViewById<Button>(R.id.openSheet).setOnClickListener {
//            val intent = Intent(this, Sheet::class.java)
//            startActivity(intent)
//        }


        findViewById<ImageView>(R.id.addButton).setOnClickListener {
            val intent = Intent(this, Piano::class.java)
            startActivity(intent)
        }


        findViewById<ImageView>(R.id.searchButton).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

    }

}





