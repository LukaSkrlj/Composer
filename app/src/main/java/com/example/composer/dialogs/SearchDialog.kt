package com.example.composer.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.composer.R
import com.example.composer.activities.MainActivity

class SearchDialog : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_dialog)

        val search = findViewById<SearchView>(R.id.search)
        search.gravity = Gravity.CENTER
        search.isActivated = true
        search.queryHint = "Search for symphonies"
        search.onActionViewExpanded()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}
