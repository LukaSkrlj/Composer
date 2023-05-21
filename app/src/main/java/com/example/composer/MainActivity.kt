package com.example.composer

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val musicList: ArrayList<MusicModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ImageButton>(R.id.addButton).setOnClickListener {
            val intent = Intent(this, Piano::class.java)
            startActivity(intent)
        }

        val recyclerViewVertical: RecyclerView = findViewById(R.id.symphoniesList)
        val recyclerViewHorizontal: RecyclerView = findViewById(R.id.newSymphonies)

        setUpMusicList()

        val allSymphoniesAdapter = SymphoniesAdapter(this, musicList, R.layout.music_row)
        val newSymphoniesAdapter = SymphoniesAdapter(this, musicList, R.layout.music_column)
        recyclerViewVertical.adapter = allSymphoniesAdapter
        recyclerViewVertical.layoutManager  = LinearLayoutManager(this)

        recyclerViewHorizontal.adapter = newSymphoniesAdapter
        recyclerViewHorizontal.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)

//
//        findViewById<Button>(R.id.openSheet).setOnClickListener {
//            val intent = Intent(this, Sheet::class.java)
//            startActivity(intent)
//        }

        findViewById<CardView>(R.id.frameAllSymphonies).setOnClickListener {
            findViewById<ImageButton>(R.id.allSymphoniesButton).setColorFilter(Color.BLACK)
            findViewById<TextView>(R.id.allSymphoniesTextView).setTextColor(Color.BLACK)

            findViewById<ImageButton>(R.id.mySymphoniesButton).setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.inactive_footer_btn
                )
            )
            findViewById<TextView>(R.id.mySymphoniesTextView).setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.inactive_footer_btn
                )
            )
        }

        findViewById<CardView>(R.id.frameMySymphonies).setOnClickListener {
            findViewById<ImageButton>(R.id.mySymphoniesButton).setColorFilter(Color.BLACK)
            findViewById<TextView>(R.id.mySymphoniesTextView).setTextColor(Color.BLACK)

            findViewById<ImageButton>(R.id.allSymphoniesButton).setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.inactive_footer_btn
                )
            )
            findViewById<TextView>(R.id.allSymphoniesTextView).setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.inactive_footer_btn
                )
            )
        }

        findViewById<ImageButton>(R.id.allSymphoniesButton).setColorFilter(Color.BLACK)
        findViewById<TextView>(R.id.allSymphoniesTextView).setTextColor(Color.BLACK)

        findViewById<ImageButton>(R.id.searchButton).setOnClickListener {
            val intent = Intent(this, SearchDialog::class.java)
            startActivity(intent)
        }

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                Log.d("search", "Search")
            }
        }
    }

    private fun setUpMusicList() {
        val symphonyName = "Thunderstruck"
        val composerName = "AC/DC"
        val duration = 120

        for (i in 1..20) {
            musicList.add(MusicModel(symphonyName, composerName, duration))
        }
    }


}
