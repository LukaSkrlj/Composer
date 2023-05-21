package com.example.composer

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class SearchDialog : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_dialog)

        val search = findViewById<SearchView>(R.id.search)
        search.gravity = Gravity.CENTER
        search.isActivated = true;
        search.queryHint = "Search for symphonies";
        search.onActionViewExpanded();

        findViewById<ImageButton>(R.id.backButton).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}
