package com.example.composer.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.composer.R
import com.example.composer.activities.PianoViewOnly
import com.example.composer.activities.SymphonyActivity
import com.example.composer.models.SearchResult

class SearchAdapter(var context: Context, var searchList: List<SearchResult>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.search_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.symphonyName.text = searchList[position].symphonyName
        holder.symphonyComposer.text = searchList[position].symphonyComposer
        holder.searchImage.setImageResource(R.drawable.ic_search)
        holder.linkImage.setImageResource(R.drawable.ic_arrow_top_left)

        holder.searchContainer?.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    PianoViewOnly::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("compositionId", searchList[position].id)
                    .putExtra("isSymphonyMine", false)//convert firebase id to int
            )
        }
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var symphonyComposer: TextView
        var symphonyName: TextView
        var searchImage: ImageView
        var linkImage: ImageView
        var searchContainer: ConstraintLayout? = null

        init {
            symphonyComposer = itemView.findViewById(R.id.symphonyComposer)
            symphonyName = itemView.findViewById(R.id.symphonyName)
            searchImage = itemView.findViewById(R.id.search)
            linkImage = itemView.findViewById(R.id.link)
            searchContainer = itemView.findViewById(R.id.search_result)
        }
    }

}
