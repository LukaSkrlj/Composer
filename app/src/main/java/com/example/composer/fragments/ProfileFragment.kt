package com.example.composer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.composer.R
import com.example.composer.activities.LoginActivity
import com.example.composer.activities.MainActivity
import com.example.composer.activities.SymphonyActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.imageview.ShapeableImageView


class ProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<CardView>(R.id.logout_container).setOnClickListener {
            signOut()
        }

        val user = context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (user != null) {
            val photoUrl = user.photoUrl.toString()
            val userName = user.displayName
            val userEmail = user.email

            view.findViewById<TextView>(R.id.user_name).text = userName
            view.findViewById<TextView>(R.id.user_email).text = userEmail
            val profileImage = view.findViewById<ShapeableImageView>(R.id.profile_image)
            Glide.with(this).load(photoUrl).into(profileImage)

            view.findViewById<CardView>(R.id.favourites_container).setOnClickListener {
                view.findNavController()
                    .navigate(R.id.action_profileInfoFragment_to_favoritesFragment)
            }
        }
    }

    private fun signOut() {
        val googleSignInClient =
            context?.let { it1 ->
                GoogleSignIn.getClient(
                    it1,
                    GoogleSignInOptions.DEFAULT_SIGN_IN
                )
            }
        googleSignInClient?.signOut()?.addOnSuccessListener {
            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT)
                .show()
            context?.startActivity(
                Intent(
                    context,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
