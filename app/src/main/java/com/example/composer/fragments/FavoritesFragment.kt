package com.example.composer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composer.R
import com.example.composer.adapters.SymphoniesAdapter
import com.example.composer.models.MusicModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.callbackFlow
import java.util.Collections


class FavoritesFragment : Fragment() {
    private var isGettingNewData = false
    private val LIMIT: Long = 12

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = GoogleSignIn.getLastSignedInAccount(requireContext())
        val swipeToRefresh = view.findViewById(R.id.swipeToRefresh) as SwipeRefreshLayout

        if (currentUser != null) {
            getDataFromServer(currentUser.id.toString())

            swipeToRefresh.setOnRefreshListener {
                getDataFromServer(currentUser.id.toString())
            }
        }
    }


    private fun getDataFromServer(userID: String) {
        val recyclerViewVertical = view?.findViewById(R.id.favoritesList) as RecyclerView
        val db = FirebaseFirestore.getInstance()
        val musicListFavorites: ArrayList<MusicModel> = arrayListOf()
        val docRefFavorites =
            db.collection("favorites").whereEqualTo("userID", userID).limit(LIMIT)
        var isScrolling = false
        var isLastItemReached = false

        docRefFavorites.get(Source.SERVER)
            .addOnSuccessListener { collection ->
                (view?.findViewById(R.id.swipeToRefresh) as SwipeRefreshLayout).isRefreshing = false
                if (collection != null) {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                        View.GONE
                    if (collection.documents.isNotEmpty()) {
                        for (document in collection.documents) {
                            musicListFavorites.add(
                                MusicModel(
                                    document.get("symphonyName") as String?,
                                    document.get("symphonyComposer") as String?,
                                    (document.get("symphonyDurationSeconds") as Long).toInt(),
                                    document.get("symphonyID").toString()
                                )
                            )
                        }
                        val allSymphoniesAdapter =
                            activity?.let {
                                SymphoniesAdapter(
                                    it,
                                    musicListFavorites,
                                    R.layout.music_row
                                )
                            }

                        recyclerViewVertical.adapter = allSymphoniesAdapter
                        recyclerViewVertical.layoutManager = LinearLayoutManager(activity)

                        Log.d("tute smo", "we bich here")


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
                                    val next =
                                        db.collection("favorites").whereEqualTo("userID", userID)
                                            .startAfter(lastVisible)
                                            .limit(LIMIT)

                                    next.get().addOnCompleteListener { newCollection ->
                                        isGettingNewData = false
                                        if (newCollection.result.size() > 0) {
                                            for (document in newCollection.result.documents) {
                                                musicListFavorites.add(
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
                        TODO("Implement later if enough time")
//                        handleDragAndDrop(musicListFavorites)
                    } else {
                        Log.d("Document data", "No such document")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Document data", "get failed with ", exception)
            }
    }


    private fun handleDragAndDrop(
        musicListFavorites: ArrayList<MusicModel>,
    ) {
        val recyclerViewVertical = view?.findViewById(R.id.favoritesList) as RecyclerView
        val callback = object :
            ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                0
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(musicListFavorites, fromPosition, toPosition)
                recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerViewVertical)


    }


}
