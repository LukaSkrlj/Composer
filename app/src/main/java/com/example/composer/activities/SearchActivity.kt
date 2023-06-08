package com.example.composer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.composer.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.composer.adapters.SearchAdapter
import com.example.composer.models.SearchResult
import com.google.firebase.firestore.FirebaseFirestore


class SearchActivity : AppCompatActivity() {
    var searchResult: ArrayList<SearchResult> = arrayListOf<SearchResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val db = FirebaseFirestore.getInstance()
        val symphonies = db.collection("symphonies")
        val search = findViewById<SearchView>(R.id.search)
        search.gravity = Gravity.CENTER
        search.isActivated = true
        search.queryHint = "Search for symphonies"
        search.onActionViewExpanded()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchResult.clear()
                if (newText != null) {
                    if (newText.isNotEmpty()) {
                        symphonies.orderBy("symphonyName").startAt(newText)
                            .endAt(newText + "\uf8ff")
                            .get()
                            .addOnCompleteListener { collection ->
                                if (collection.result.documents.isNotEmpty()) {
                                    for (document in collection.result.documents) {
                                        val symphonyName = document.get("symphonyName") as String
                                        val symphonyComposer =
                                            document.get("symphonyComposer") as String
                                        searchResult.add(
                                            SearchResult(
                                                symphonyComposer,
                                                symphonyName,
                                                document.id
                                            )
                                        )
                                    }

                                    sendDataToAdapter(searchResult.toSet().toList())
                                    searchResult.clear()
                                } else {

                                    sendDataToAdapter(listOf<SearchResult>())
                                }
                            }.addOnFailureListener { exception ->

                                sendDataToAdapter(listOf<SearchResult>())
                            }
                    } else {
                        sendDataToAdapter(listOf<SearchResult>())
                    }
                }

                return true
            }
        })


    }

    private fun sendDataToAdapter(search: List<SearchResult>) {
        val recyclerViewSearch: RecyclerView = findViewById(R.id.searchRecyclerView)
        val context: Context = applicationContext()
        val searchAdapter =
            SearchAdapter(
                context,
                search,
            )

        recyclerViewSearch.adapter = searchAdapter
        recyclerViewSearch.layoutManager =
            LinearLayoutManager(context)

    }

    init {
        instance = this
    }

    companion object {
        private var instance: SearchActivity? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}
