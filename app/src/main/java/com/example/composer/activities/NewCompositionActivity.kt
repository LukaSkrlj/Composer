package com.example.composer.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.models.Composition
import com.example.composer.viewmodel.CompositionViewModel
import com.google.android.material.textfield.TextInputLayout


class NewCompositionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_composition_activity)
        val authorNameField = findViewById<TextInputLayout>(R.id.author_name)
        val compostionNameField = findViewById<TextInputLayout>(R.id.compostion_name)
        val compositionViewModel = ViewModelProvider(this)[CompositionViewModel::class.java]
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        authorNameField.editText?.doOnTextChanged { text, start, before, count ->
            authorNameField.error = null
        }
        compostionNameField.editText?.doOnTextChanged { text, start, before, count ->
            authorNameField.error = null
        }

        findViewById<AppCompatButton>(R.id.submit).setOnClickListener {
            val compositionNameText = compostionNameField.editText?.text
            val authorNameText = authorNameField.editText?.text
            if (compositionNameText?.length == 0) {
                compostionNameField.error = "Composition name is required"
                return@setOnClickListener
            }

            if (authorNameText?.length == 0) {
                authorNameField.error = "Author name is required"
                return@setOnClickListener
            }

             val compositionId = compositionViewModel.upsertComposition(
                Composition(
                    name = authorNameText.toString(),
                    author = compositionNameText.toString(),
                )
            )

            Log.d("tu smo ej", compositionId.toString())


//            Intent(
//                this,
//                Piano::class.java
//            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                .putExtra("isSymphonyMine", true)
//                .putExtra("compositionId", )
        }

    }

}
