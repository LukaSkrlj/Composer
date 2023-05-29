package com.example.composer

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
import com.example.composer.adapters.SymphoniesAdapter
import com.example.composer.models.MusicModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val LIMIT: Long = 10
var isGettingNewData = false

/**
 * A simple [Fragment] subclass.
 * Use the [AllSymphoniesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllSymphoniesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }


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
            swipeToRefresh.isRefreshing = false
        }
        getDataFromServer()
    }


    private fun getDataFromServer() {
        val recyclerViewVertical = view?.findViewById(R.id.symphoniesList) as RecyclerView
        val recyclerViewHorizontal = view?.findViewById(R.id.newSymphonies) as RecyclerView
        val db = FirebaseFirestore.getInstance()
        val musicListAll: ArrayList<MusicModel> = arrayListOf()
        val musicListNewest: ArrayList<MusicModel> = arrayListOf()
        val docRefAll = db.collection("symphonies").limit(LIMIT)
        val docRefNewest = db.collection("symphonies").limit(LIMIT)
        var isScrolling = false
        var isLastItemReached = false

        docRefAll.get(Source.SERVER)
            .addOnSuccessListener { collection ->
                if (collection != null) {
                    requireView().findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                        View.GONE
                    for (document in collection.documents) {
                        document.toObject(MusicModel::class.java)?.let { musicListAll.add(it) }
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
                                            document.toObject(MusicModel::class.java)
                                                ?.let { musicListAll.add(it) }
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

        docRefNewest.get(Source.SERVER).addOnSuccessListener { collection ->
            if (collection != null) {
                for (document in collection.documents) {
                    document.toObject(MusicModel::class.java)?.let { musicListNewest.add(it) }
                }

                val newSymphoniesAdapter =
                    activity?.let {
                        SymphoniesAdapter(
                            it,
                            musicListNewest,
                            R.layout.music_column
                        )
                    }

                recyclerViewHorizontal.adapter = newSymphoniesAdapter
                recyclerViewHorizontal.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            } else {
                Log.d("Document data", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Document data", "get failed with ", exception)
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AllSymphoniesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllSymphoniesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
