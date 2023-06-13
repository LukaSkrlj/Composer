package com.example.composer.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.composer.R
import com.example.composer.models.FavoriteModel
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.models.Measure
import com.example.composer.models.MeasureWithNotes
import com.example.composer.models.Note
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore

class PianoViewOnly : AppCompatActivity() {
    private lateinit var staff: Staff
    private var isPlaying = false
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano_view_only)
        staff = findViewById(R.id.staff)
        val extras = intent.extras
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_cyclic)
        val favoritesIcon = findViewById<ImageButton>(R.id.imageHeart)
        val currentUser = GoogleSignIn.getLastSignedInAccount(this)
        findViewById<ImageButton>(R.id.playButtonPiano).setOnClickListener {
            playSymphony(!isPlaying)
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }


        //when symphony exists
        if (extras != null) {

            val db = FirebaseFirestore.getInstance()
            val compositionId = extras.getString("compositionId")

            val document = compositionId?.let { db.collection("symphonies").document(it) }
            val measureWithNotesCopyMutable: MutableList<InstrumentWithMeasures> = mutableListOf()


            document?.get()?.addOnCompleteListener { composition ->
                if (composition.result.exists()) {
                    findViewById<TextView>(R.id.symphonyName).text =
                        composition.result.get("symphonyName") as CharSequence?

                    if (currentUser != null) {
                        currentUser.id?.let {
                            checkIfSymphonyIsLiked(
                                db,
                                it,
                                compositionId
                            )
                        }

                        favoritesIcon.setOnClickListener {
                            if (isLiked) {
                                addLikeBackground(false)
                                db.collectionGroup("favorites")
                                    .whereEqualTo("userID", currentUser.id.toString())
                                    .whereEqualTo("compositionId", compositionId).get()
                                    .addOnSuccessListener {
                                        it.documents.forEach { document ->
                                            document.reference.delete()
                                        }
                                    }
                            } else {

                                val data = FavoriteModel(
                                    composition.result.get("symphonyName").toString(),
                                    currentUser.id.toString(),
                                    compositionId,
                                    (composition.result.get("symphonyDurationSeconds") as Long).toInt(),
                                    composition.result.get("symphonyComposer").toString(),
                                    currentUser.displayName.toString()
                                )

                                addLikeBackground(true)
                                db.collection("favorites").add(data)
                            }
                        }
                    }

                }

            }

            document?.collection("sheet")?.document("music")?.get()
                ?.addOnCompleteListener { sheet ->

                    if (sheet.result.exists()) {
                        val measures =
                            sheet.result.data?.get("measures") as ArrayList<HashMap<Any, Any>>

                        for ((measureIndex, element) in measures.withIndex()) {

                            val measure = element["measure"] as HashMap<*, *>
                            val notes = element["notes"] as ArrayList<HashMap<Any, Any>>
                            var newMeasureWithNotes: MeasureWithNotes = MeasureWithNotes()
                            val newNotesList: MutableList<Note> = mutableListOf()
                            val newMeasure = Measure(
                                id = (measure["id"] as Long).toInt(),
                                timeSignatureTop = (measure["timeSignatureTop"] as Long).toInt(),
                                timeSignatureBottom = (measure["timeSignatureBottom"] as Long).toInt(),
                                keySignature = measure["keySignature"] as String,
                                compositionId = (measure["compositionId"] as Long).toInt(),
                                clef = measure["clef"] as String,
                                instrumentId = 0
                            )

                            newMeasureWithNotes.measure = newMeasure

                            for ((noteIndex, note) in notes.withIndex()) {
                                val newNote = Note(
                                    right = (note["right"] as Long).toInt(),
                                    bottom = (note["bottom"] as Long).toInt(),
                                    dx = (note["dx"] as Double).toFloat(),
                                    dy = (note["dy"] as Double).toFloat(),
                                    measureId = (note["measureId"] as Long).toInt(),
                                    key = note["key"] as String
                                )
                                newNotesList.add(noteIndex, newNote)
                            }
                            newMeasureWithNotes.notes = newNotesList.toList()
//                        measureWithNotesCopyMutable.add(measureIndex, newMeasureWithNotes)

                        }
                        staff.drawNotes(measureWithNotesCopyMutable)
                        progressBar.visibility = View.GONE

                    } else {
                        progressBar.visibility = View.GONE
                    }


                }
        }
        this.hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Content visible behind bars
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )

        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        actionBar?.hide()
    }

    private fun checkIfSymphonyIsLiked(
        db: FirebaseFirestore,
        userID: String,
        compositionId: String
    ) {
        db.collectionGroup("favorites")
            .whereEqualTo("userID", userID)
            .whereEqualTo("compositionId", compositionId).get()
            .addOnCompleteListener {
                it.exception?.localizedMessage?.let { it1 -> Log.d("tu smo ej", it1) }
                if (it.result.isEmpty) {
                    addLikeBackground(false)

                } else {
                    addLikeBackground(true)
                }
            }

    }

    private fun addLikeBackground(colorBackground: Boolean) {
        isLiked = colorBackground
        val favoritesButton = findViewById<ImageButton>(R.id.imageHeart)
        if (colorBackground) {
            favoritesButton.setImageResource(R.drawable.ic_favorite_full)

        } else {
            favoritesButton.setImageResource(R.drawable.ic_favorite_empty)
        }
    }

    fun playSymphony(playSymphony: Boolean) {
        isPlaying = playSymphony
        val playButton = findViewById<ImageButton>(R.id.playButtonPiano)

        if (playSymphony) {
            playButton.setImageResource(R.drawable.ic_pause)
            staff.setIsMusicPlaying(true, playButton)
        } else {
            playButton.setImageResource(R.drawable.ic_play)
            staff.setIsMusicPlaying(false)
        }
    }
}
