package com.example.composer.adapters

import com.example.composer.models.Note
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.composer.R
import com.example.composer.activities.Staff
import com.example.composer.constants.EIGHT_NOTE
import com.example.composer.constants.HALF_NOTE
import com.example.composer.constants.HUNDREDTWENTYEIGHT_NOTE
import com.example.composer.constants.QUARTER_NOTE
import com.example.composer.constants.SIXTEEN_NOTE
import com.example.composer.constants.SIXTYFOUR_NOTE
import com.example.composer.constants.THIRTYTWO_NOTE
import com.example.composer.constants.WHOLE_NOTE
import com.example.composer.viewmodel.NoteViewModel
import com.google.android.material.slider.Slider

private var blackNotes = arrayOf(
    "bb0",
    "db1",
    "eb1",
    "gb1",
    "ab1",
    "bb1",
    "db2",
    "eb2",
    "gb2",
    "ab2",
    "bb2",
    "db3",
    "eb3",
    "gb3",
    "ab3",
    "bb3",
    "db4",
    "eb4",
    "gb4",
    "ab4",
    "bb4",
    "db5",
    "eb5",
    "gb5",
    "ab5",
    "bb5",
    "db6",
    "eb6",
    "gb6",
    "ab6",
    "bb6",
    "db7",
    "eb7",
    "gb7",
    "ab7",
    "bb7"
)

private val whiteNotes = arrayOf(
    "a0",
    "b0",
    "c1",
    "d1",
    "e1",
    "f1",
    "g1",
    "a1",
    "b1",
    "c2",
    "d2",
    "e2",
    "f2",
    "g2",
    "a2",
    "b2",
    "c3",
    "d3",
    "e3",
    "f3",
    "g3",
    "a3",
    "b3",
    "c4",
    "d4",
    "e4",
    "f4",
    "g4",
    "a4",
    "b4",
    "c5",
    "d5",
    "e5",
    "f5",
    "g5",
    "a5",
    "b5",
    "c6",
    "d6",
    "e6",
    "f6",
    "g6",
    "a6",
    "b6",
    "c7",
    "d7",
    "e7",
    "f7",
    "g7",
    "a7",
    "b7",
    "c8"
)

private var allNotes = arrayOf(
    "a0",
    "bb0",
    "b0",
    "c1",
    "db1",
    "d1",
    "eb1",
    "e1",
    "f1",
    "gb1",
    "g1",
    "ab1",
    "a1",
    "bb1",
    "b1",
    "c2",
    "db2",
    "d2",
    "eb2",
    "e2",
    "f2",
    "gb2",
    "g2",
    "ab2",
    "a2",
    "bb2",
    "b2",
    "c3",
    "db3",
    "d3",
    "eb3",
    "e3",
    "f3",
    "gb3",
    "g3",
    "ab3",
    "a3",
    "bb3",
    "b3",
    "c4",
    "db4",
    "d4",
    "eb4",
    "e4",
    "f4",
    "gb4",
    "g4",
    "ab4",
    "a4",
    "bb4",
    "b4",
    "c5",
    "db5",
    "d5",
    "eb5",
    "e5",
    "f5",
    "gb5",
    "g5",
    "ab5",
    "a5",
    "bb5",
    "b5",
    "c6",
    "db6",
    "d6",
    "eb6",
    "e6",
    "f6",
    "gb6",
    "g6",
    "ab6",
    "a6",
    "bb6",
    "b6",
    "c7",
    "db7",
    "d7",
    "eb7",
    "e7",
    "f7",
    "gb7",
    "g7",
    "ab7",
    "a7",
    "bb7",
    "b7",
    "c8"
)

class EditNoteAdapter(
    var context: Context,
    private var notesList: MutableList<Note>,
    viewModelStoreOwner: ViewModelStoreOwner?,
    private val lifecycleOwner: LifecycleOwner?,
) : RecyclerView.Adapter<EditNoteAdapter.ViewHolder>() {
    var noteViewModel =
        viewModelStoreOwner?.let { ViewModelProvider(it) }?.get(NoteViewModel::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.note_edit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentNoteIndex = allNotes.indexOf(notesList[position].pitch)
        holder.noteType?.text = notesList[position].pitch.uppercase()
        changeImageView(holder, notesList[position].length)
        if (itemCount == 1) {
            holder.deleteButton?.isEnabled = false
        }

        holder.slider?.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> {
                    holder.slider?.setLabelFormatter { "1" }
                    changeLength(noteViewModel, position, WHOLE_NOTE)
                    changeImageView(holder, WHOLE_NOTE)
                }

                16.0f -> {
                    holder.slider?.setLabelFormatter { "1/2" }
                    changeLength(noteViewModel, position, HALF_NOTE)
                    changeImageView(holder, HALF_NOTE)
                }

                32.0f -> {
                    holder.slider?.setLabelFormatter { "1/4" }
                    changeLength(noteViewModel, position, QUARTER_NOTE)
                    changeImageView(holder, QUARTER_NOTE)
                }

                48.0f -> {
                    holder.slider?.setLabelFormatter { "1/8" }
                    changeLength(noteViewModel, position, EIGHT_NOTE)
                    changeImageView(holder, EIGHT_NOTE)
                }

                64.0f -> {
                    holder.slider?.setLabelFormatter { "1/16" }
                    changeLength(noteViewModel, position, SIXTEEN_NOTE)
                    changeImageView(holder, SIXTEEN_NOTE)
                }

                80.0f -> {
                    holder.slider?.setLabelFormatter { "1/32" }
                    changeLength(noteViewModel, position, THIRTYTWO_NOTE)
                    changeImageView(holder, THIRTYTWO_NOTE)
                }

                96.0f -> {
                    holder.slider?.setLabelFormatter { "1/64" }
                    changeLength(noteViewModel, position, SIXTYFOUR_NOTE)
                    changeImageView(holder, SIXTYFOUR_NOTE)
                }

                112.0f -> {
                    holder.slider?.setLabelFormatter { "1/128" }
                    changeLength(noteViewModel, position, HUNDREDTWENTYEIGHT_NOTE)
                    changeImageView(holder, HUNDREDTWENTYEIGHT_NOTE)
                }
            }

        }

        holder.upNote?.setOnClickListener {
            if (currentNoteIndex != allNotes.size - 1) {
                currentNoteIndex++
                changePitch(holder, currentNoteIndex, position)

            }
        }

        holder.downNote?.setOnClickListener {
            if (currentNoteIndex != 0) {
                currentNoteIndex--
                changePitch(holder, currentNoteIndex, position)
            }
        }

        holder.deleteButton?.setOnClickListener {
            if (lifecycleOwner != null) {

                noteViewModel?.deleteNote(notesList[position])?.observe(lifecycleOwner) {
                Log.d("tu smo ej", notesList.toString())
                    if (it != 0) {
                        notifyItemRemoved(position)
                        notesList.removeLast()
                        if(notesList.size == 2){
                            holder.deleteButton?.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun changeLength(noteViewModel: NoteViewModel?, currentPosition: Int, length: Float) {
        notesList[currentPosition].length = length
        noteViewModel?.updateNote(notesList[currentPosition])
    }

    private fun changePitch(holder: ViewHolder, currentNoteIndex: Int, currentPosition: Int) {
        holder.noteType?.text = allNotes[currentNoteIndex].uppercase()
        notesList[currentPosition].pitch = allNotes[currentNoteIndex]
        notesList[currentPosition].dy =
            Staff.getSpacing()
                .toInt() * 13 - (Staff.getSpacing() / 2) *
                    (if (allNotes[currentNoteIndex][1] == "b".single()) blackNotes.indexOf(allNotes[currentNoteIndex]) + 1
                    else whiteNotes.indexOf(allNotes[currentNoteIndex]))
        noteViewModel?.updateNote(notesList[currentPosition])
    }


    private fun changeImageView(holder: ViewHolder, length: Float) {
        holder.noteImage?.setImageResource(
            when (length) {
                WHOLE_NOTE -> {
                    holder.slider?.value = 0f
                    R.drawable.note_wholenote
                }

                HALF_NOTE -> {
                    holder.slider?.value = 16.0f
                    R.drawable.note_halfnote
                }

                QUARTER_NOTE -> {
                    holder.slider?.value = 32.0f
                    R.drawable.quarter_note
                }

                EIGHT_NOTE -> {
                    holder.slider?.value = 48.0f
                    R.drawable.note_th
                }

                SIXTEEN_NOTE -> {
                    holder.slider?.value = 64.0f
                    R.drawable.note_sixteenthnote
                }

                THIRTYTWO_NOTE -> {
                    holder.slider?.value = 80.0f
                    R.drawable.note_thirtysecondnote
                }

                SIXTYFOUR_NOTE -> {
                    holder.slider?.value = 96.0f
                    R.drawable.note_sixtyfourth
                }

                HUNDREDTWENTYEIGHT_NOTE -> {
                    holder.slider?.value = 112.0f
                    R.drawable.note_hundredtwentyeighthnote
                }

                else -> {
                    R.drawable.quarter_note
                }
            }
        )
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var noteImage: ImageView? = null
        var noteType: TextView? = null
        var deleteButton: ImageButton? = null
        var downNote: ImageButton? = null
        var upNote: ImageButton? = null
        var slider: Slider? = null

        init {
            noteImage = itemView.findViewById(R.id.note_image)
            noteType = itemView.findViewById(R.id.note_type)
            deleteButton = itemView.findViewById(R.id.delete_note)
            downNote = itemView.findViewById(R.id.down_note)
            upNote = itemView.findViewById(R.id.up_note)
            slider = itemView.findViewById(R.id.slider)
        }
    }
}
