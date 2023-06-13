package com.example.composer.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
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
        val addCompositionButton = findViewById<AppCompatButton>(R.id.submit)
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        authorNameField.editText?.doOnTextChanged { text, start, before, count ->
            authorNameField.error = null
        }
        compostionNameField.editText?.doOnTextChanged { text, start, before, count ->
            compostionNameField.error = null
        }

        addCompositionButton.setOnClickListener {
            val compositionNameText = compostionNameField.editText?.text
            val authorNameText = authorNameField.editText?.text

            if (compositionNameText?.length == 0 && authorNameText?.length == 0) {
                compostionNameField.error = "Composition name is required"
                authorNameField.error = "Author name is required"
                return@setOnClickListener
            } else if (compositionNameText?.length == 0) {
                compostionNameField.error = "Composition name is required"
                return@setOnClickListener
            } else if (authorNameText?.length == 0) {
                authorNameField.error = "Author name is required"
                return@setOnClickListener
            }

            addCompositionButton.isEnabled = false


            val compositionModel = ViewModelProvider(this)[CompositionViewModel::class.java]
            compositionModel.insertComposition(
                Composition(
                    name = compositionNameText.toString(),
                    author = authorNameText.toString(),
                )
            ).observe(this, Observer {
                this.startActivity(
                    Intent(
                        this,
                        Piano::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("isSymphonyMine", true)
                        .putExtra("compositionId", it.toInt())
                )
            })

        }

    }

}
