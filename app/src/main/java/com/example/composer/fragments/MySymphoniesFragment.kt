package com.example.composer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composer.R
import com.example.composer.adapters.SymphoniesAdapter
import com.example.composer.models.MusicModel
import com.example.composer.viewmodel.CompositionViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.Collections


class MySymphoniesFragment : Fragment() {
    val LIMIT: Long = 12
    var userID: String? = null
    var isGettingNewData = false
    private lateinit var compositionViewModel: CompositionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_symphonies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = GoogleSignIn.getLastSignedInAccount(requireContext())
        val recyclerView = view.findViewById(R.id.mySymphonesList) as RecyclerView
        compositionViewModel = ViewModelProvider(this)[CompositionViewModel::class.java]

        compositionViewModel.compositions.observe(viewLifecycleOwner) { compositions ->
            val symphonyList: ArrayList<MusicModel> = arrayListOf()
            for (composition in compositions) {
                symphonyList.add(
                    MusicModel(
                        symphonyName = composition.name,
                        symphonyComposer = composition.author,
                        symphonyDurationSeconds = 0,
                        id = composition.id.toString()
                    )
                )
            }

            val allSymphoniesAdapter =
                activity?.let {
                    SymphoniesAdapter(
                        it,
                        symphonyList,
                        R.layout.music_row,
                        true
                    )
                }

            recyclerView.adapter = allSymphoniesAdapter
            recyclerView.layoutManager = LinearLayoutManager(activity)

            requireView().findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                View.GONE
        }

    }
}
