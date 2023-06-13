package com.example.composer.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.composer.R
import com.example.composer.activities.Piano
import com.example.composer.activities.MainActivity
import com.example.composer.activities.PianoViewOnly
import com.example.composer.activities.SymphonyActivity
import com.example.composer.models.MusicModel

class SymphoniesAdapter(
    var context: Context,
    var symphoniesList: ArrayList<MusicModel>,
    val layoutID: Int,
    val isSymphonyMine: Boolean = false
) : RecyclerView.Adapter<SymphoniesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(layoutID, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.symphonyName?.text = symphoniesList[position].symphonyName
        holder.composerName?.text = symphoniesList[position].symphonyComposer
        holder.symphonyImage.setImageResource(R.drawable.music_placeholder)

        val minutes = (symphoniesList[position].symphonyDurationSeconds?.rem(3600))?.div(60)
        val seconds = symphoniesList[position].symphonyDurationSeconds?.rem(60)
        val timeString = String.format("%02d:%02d", minutes, seconds)

        holder.duration?.text = timeString

        holder.cardView?.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                   if(isSymphonyMine) Piano::class.java else PianoViewOnly::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("compositionId", symphoniesList[position].id)
                    .putExtra("isSymphonyMine", isSymphonyMine)
            )
        }

    }

    override fun getItemCount(): Int {
        return symphoniesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var symphonyImage: ImageView
        var composerName: TextView? = null
        var symphonyName: TextView? = null
        var duration: TextView? = null
        var cardView: ConstraintLayout? = null

        init {
            symphonyImage = itemView.findViewById(R.id.symphonyImage)
            composerName = itemView.findViewById(R.id.composerName)
            symphonyName = itemView.findViewById(R.id.symphonyName)
            duration = itemView.findViewById(R.id.duration)
            cardView = itemView.findViewById(R.id.columnContainer)
        }
    }
}
