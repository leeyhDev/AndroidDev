package com.leeyh.note.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.core.ui.BrowserActivity
import com.leeyh.note.R
import com.leeyh.note.ui.MarkdownActivity
import kotlinx.android.synthetic.main.fragment_item.view.*

class NoteAdapter(private val notes: List<String>, private val type: String) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

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
                    val intent = Intent(v.context, MarkdownActivity::class.java)
                    intent.putExtra("markdownPath", "$type/$item")
                    v.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = notes.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
    }
}
