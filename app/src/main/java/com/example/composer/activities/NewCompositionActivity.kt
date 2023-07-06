package com.example.composer.activities


import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.composer.R
import com.example.composer.constants.INSTRUMENTS
import com.example.composer.databinding.NewCompositionActivityBinding
import com.example.composer.models.Composition
import com.example.composer.viewmodel.CompositionViewModel
import com.google.android.material.textfield.TextInputLayout


class NewCompositionActivity : AppCompatActivity() {
    private lateinit var binding: NewCompositionActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewCompositionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, R.layout.list_item, INSTRUMENTS)
        binding.dropdown.setAdapter(adapter)
        val authorNameField = findViewById<TextInputLayout>(R.id.author_name)
        val compostionNameField = findViewById<TextInputLayout>(R.id.compostion_name)
        val addCompositionButton = findViewById<AppCompatButton>(R.id.submit)
        val instrumentType = findViewById<TextInputLayout>(R.id.instrumentType)
        val compositionSpeedField = findViewById<TextInputLayout>(R.id.composition_speed)
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }
        var instrumentTypeText: String? = null
        var compositionSpeed = 0

        instrumentType.editText?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                instrumentType.error = null
            }
            instrumentTypeText = text.toString().lowercase()
        }

        authorNameField.editText?.doOnTextChanged { text, start, before, count ->
            if (text != null) {
                authorNameField.error = null
            }
        }
        compostionNameField.editText?.doOnTextChanged { text, start, before, count ->
            if (text != null) {
                compostionNameField.error = null
            }
        }

        compositionSpeedField.editText?.doOnTextChanged { text, _, _, _ ->
            compositionSpeed = 0
            if (text != null && text.toString() != "" && text.toString().length <= 3) {
                compositionSpeedField.error = null
                compositionSpeed = text.toString().toInt()
                if (compositionSpeed > 260) {
                    compositionSpeedField.editText!!.setText("260")
                    compositionSpeed = 260
                }
            }
        }

        addCompositionButton.setOnClickListener {
            val compositionNameText = compostionNameField.editText?.text
            val authorNameText = authorNameField.editText?.text

            if (compositionNameText?.length == 0) {
                compostionNameField.error = "Composition name is required"
            }
            if (authorNameText?.length == 0) {
                authorNameField.error = "Author name is required"
            }
            if (!INSTRUMENTS.any { instrument -> instrument.lowercase() == instrumentTypeText }) {
                instrumentType.error = "Instrument type is required"
            }
            if (compositionSpeed == 0) {
                instrumentType.error = "Composition speed can't be 0"
            }
            if (compostionNameField.error != null || instrumentType.error != null || authorNameField.error != null || compositionSpeed == 0) {
                return@setOnClickListener
            }

            addCompositionButton.isEnabled = false


            val compositionModel = ViewModelProvider(this)[CompositionViewModel::class.java]
            compositionModel.insertComposition(
                Composition(
                    name = compositionNameText.toString(),
                    author = authorNameText.toString(),
                    compositionSpeed = compositionSpeed
                )
            ).observe(this, Observer {
                this.startActivity(
                    Intent(
                        this,
                        Piano::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("isSymphonyMine", true)
                        .putExtra("compositionId", it.toInt())
                        .putExtra(
                            "instrumentType",
                            instrumentTypeText
                        ).putExtra("compositionSpeed", compositionSpeed)
                )
                finish()
            })

        }

    }

}
