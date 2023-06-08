package com.example.composer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composer.R
import com.example.composer.adapters.SymphoniesAdapter
import com.example.composer.models.MusicModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source


class AllSymphoniesFragment : Fragment() {
    var isGettingNewData = false
    val LIMIT: Long = 10


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_symphonies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swipeToRefresh = view.findViewById(R.id.swipeToRefresh) as SwipeRefreshLayout
        swipeToRefresh.setOnRefreshListener {
            getDataFromServer()
        }
        getDataFromServer()
    }


    private fun getDataFromServer() {
        val recyclerViewVertical = view?.findViewById(R.id.symphoniesList) as RecyclerView
        val recyclerViewHorizontal = view?.findViewById(R.id.mostLikedSymphonies) as RecyclerView
        val db = FirebaseFirestore.getInstance()
        val musicListAll: ArrayList<MusicModel> = arrayListOf()
        val musicListMostLiked: ArrayList<MusicModel> = arrayListOf()
        val docRefAll = db.collection("symphonies").limit(LIMIT)
        val docRefMostLiked = db.collection("symphonies").orderBy("likes", Query.Direction.DESCENDING).limit(LIMIT)
        var isScrolling = false
        var isLastItemReached = false

        docRefAll.get(Source.SERVER)
            .addOnSuccessListener { collection ->
                (view?.findViewById(R.id.swipeToRefresh) as SwipeRefreshLayout).isRefreshing = false
                if (collection != null) {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                        View.GONE
                    for (document in collection.documents) {
                        musicListAll.add(
                            MusicModel(
                                document.get("symphonyName") as String?,
                                document.get("symphonyComposer") as String?,
                                (document.get("symphonyDurationSeconds") as Long).toInt(),
                                document.id
                            )
                        )
                    }
                    val allSymphoniesAdapter =
                        activity?.let { SymphoniesAdapter(it, musicListAll, R.layout.music_row) }

                    recyclerViewVertical.adapter = allSymphoniesAdapter
                    recyclerViewVertical.layoutManager = LinearLayoutManager(activity)


                    var lastVisible = collection.documents[collection.size() - 1]

                    recyclerViewVertical.addOnScrollListener(object :
                        RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true
                            }
                        }


                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)

                            val llm: LinearLayoutManager =
                                recyclerView.layoutManager as LinearLayoutManager
                            val firstVisibleItem = llm.findFirstVisibleItemPosition()
                            val visibleItemsCount = llm.childCount
                            val totalItemCount = llm.itemCount


                            if (isScrolling && (firstVisibleItem + visibleItemsCount == totalItemCount) && !isLastItemReached && !isGettingNewData) {
                                isGettingNewData = true
                                val next = db.collection("symphonies")
                                    .startAfter(lastVisible)
                                    .limit(LIMIT)

                                next.get().addOnCompleteListener { newCollection ->
                                    isGettingNewData = false
                                    if (newCollection.result != null) {
                                        for (document in newCollection.result.documents) {
                                            musicListAll.add(
                                                MusicModel(
                                                    document.get("symphonyName") as String?,
                                                    document.get("symphonyComposer") as String?,
                                                    (document.get("symphonyDurationSeconds") as Long).toInt(),
                                                    document.id
                                                )
                                            )
                                        }

                                        allSymphoniesAdapter?.notifyDataSetChanged()

                                        lastVisible =
                                            newCollection.result.documents[newCollection.result.size() - 1]

                                        if (newCollection.result.size() < LIMIT) {
                                            isLastItemReached = true
                                        }
                                    }
                                }
                            }
                        }

                    })
                } else {
                    Log.d("Document data", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Document data", "get failed with ", exception)
            }

        docRefMostLiked.get(Source.SERVER).addOnSuccessListener { collection ->
            if (collection != null) {
                for (document in collection.documents) {
                    musicListMostLiked.add(
                        MusicModel(
                            document.get("symphonyName") as String?,
                            document.get("symphonyComposer") as String?,
                            (document.get("symphonyDurationSeconds") as Long).toInt(),
                            document.id
                        )
                    )
                }

                val mostLikedSymphoniesAdapter =
                    activity?.let {
                        SymphoniesAdapter(
                            it,
                            musicListMostLiked,
                            R.layout.music_column
                        )
                    }

                recyclerViewHorizontal.adapter = mostLikedSymphoniesAdapter
                recyclerViewHorizontal.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            } else {
                Log.d("Document data", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Document data", "get failed with ", exception)
        }
    }

}
