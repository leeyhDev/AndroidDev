package com.leeyh.ui.note


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.leeyh.BrowserNormalActivity
import com.leeyh.R
import kotlinx.android.synthetic.main.fragment_item.view.*

class NotetemAdapter(private val notes: List<String>, private val type :String) : RecyclerView.Adapter<NotetemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notes[position]
        holder.title.text = item

        with(holder.view) {
            tag = item
            setOnClickListener { v ->
                val intent = Intent(v.context, BrowserNormalActivity::class.java)
                intent.putExtra(BrowserNormalActivity.URL, "file:///android_asset/$type/$item")
                v.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = notes.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
    }
}
