package com.example.composer.activities
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.composer.R
import com.example.composer.models.FavoriteModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore



class SymphonyActivity : AppCompatActivity() {
    private var isLiked = false
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symphony)
        val db = FirebaseFirestore.getInstance()
        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }
        val favoritesIcon = findViewById<ImageButton>(R.id.imageHeart)

        findViewById<ImageButton>(R.id.playButton).setOnClickListener{
            playSymphony(!isPlaying)
        }

        val extras = intent.extras
        if (extras != null) {
            val symphonyID = extras.getString("symphonyID")

            val document = symphonyID?.let { db.collection("symphonies").document(it) }

            val currentUser = GoogleSignIn.getLastSignedInAccount(this)
            if (currentUser != null) {
                document?.get()?.addOnSuccessListener { symphony ->
                    findViewById<TextView>(R.id.symphonyName).text =
                        symphony.get("symphonyName") as CharSequence?

                    currentUser.id?.let {
                        checkIfSymphonyIsLiked(
                            db,
                            it,
                            symphonyID
                        )
                    }

                    favoritesIcon.setOnClickListener {
                        if (isLiked) {
                            addLikeBackground(false)
                            db.collectionGroup("favorites")
                                .whereEqualTo("userID", currentUser.id.toString())
                                .whereEqualTo("symphonyID", symphonyID).get().addOnSuccessListener {
                                    it.documents.forEach { document ->
                                        document.reference.delete()
                                    }
                                }
                        } else {

                            val data = FavoriteModel(
                                symphony.get("symphonyName").toString(),
                                currentUser.id.toString(),
                                symphonyID,
                                (symphony.get("symphonyDurationSeconds") as Long).toInt(),
                                symphony.get("symphonyComposer").toString(),
                                currentUser.displayName.toString()
                            )

                            addLikeBackground(true)
                            db.collection("favorites").add(data)
                        }
                    }

                }
            } else {
                findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                    View.GONE
                favoritesIcon.visibility = View.GONE
            }


        }

    }

    private fun checkIfSymphonyIsLiked(
        db: FirebaseFirestore,
        userID: String,
        symphonyID: String
    ) {
        db.collectionGroup("favorites")
            .whereEqualTo("userID", userID)
            .whereEqualTo("symphonyID", symphonyID).get()
            .addOnCompleteListener {
                findViewById<ProgressBar>(R.id.progressBar_cyclic).visibility =
                    View.GONE
                if (it.result.isEmpty) {
                    addLikeBackground(false)
                } else {
                    addLikeBackground(true)
                }
            }
    }


    private fun addLikeBackground(colorBackground: Boolean) {
        isLiked = colorBackground
        val favoritesButton= findViewById<ImageButton>(R.id.imageHeart)
        if (colorBackground) {
            favoritesButton.setImageResource(R.drawable.ic_favorite_full)

        } else {
            favoritesButton.setImageResource(R.drawable.ic_favorite_empty)
        }
    }

    private fun playSymphony(playSymphony: Boolean) {
        isPlaying = playSymphony
        val playButton= findViewById<ImageButton>(R.id.playButton)
        if (playSymphony) {
            playButton.setImageResource(R.drawable.ic_pause)

        } else {
            playButton.setImageResource(R.drawable.ic_play)
        }
    }

}

