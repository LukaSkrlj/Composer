package com.example.composer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SymphoniesAdapter(var context: Context, var symphoniesList: ArrayList<MusicModel>, val layoutID: Int) :
    RecyclerView.Adapter<SymphoniesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(layoutID, parent, false )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.symphonyName?.text = symphoniesList[position].symphonyName
        holder.composerName?.text = symphoniesList[position].symphonyComposer
        holder.symphonyImage.setImageResource(R.drawable.music_placeholder)
        val minutes = (symphoniesList[position].symphonyDurationSeconds % 3600) / 60;
        val seconds = symphoniesList[position].symphonyDurationSeconds % 60;
        val timeString = String.format("%02d:%02d", minutes, seconds);
        holder.duration?.text = timeString
    }
    override fun getItemCount(): Int {
        return symphoniesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var symphonyImage: ImageView
        var composerName: TextView? = null
        var symphonyName: TextView? = null
        var duration: TextView? = null

        init {
            symphonyImage = itemView.findViewById(R.id.symphonyImage)
            composerName = itemView.findViewById(R.id.composerName)
            symphonyName = itemView.findViewById(R.id.symphonyName)
            duration = itemView.findViewById(R.id.duration)
        }
    }
}
