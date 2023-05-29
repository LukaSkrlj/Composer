package com.example.composer

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView




class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.setupWithNavController(navController)

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
            val intent = Intent(this, SearchDialog::class.java)
            startActivity(intent)
        }

    }
}


