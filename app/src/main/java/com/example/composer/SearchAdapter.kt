package com.example.composer


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var symphonyComposer: TextView
        var symphonyName: TextView
        var searchImage: ImageView
        var linkImage: ImageView

        init {
            symphonyComposer = itemView.findViewById(R.id.symphonyComposer)
            symphonyName = itemView.findViewById(R.id.symphonyName)
            searchImage = itemView.findViewById(R.id.search)
            linkImage = itemView.findViewById(R.id.link)
        }
    }

}
