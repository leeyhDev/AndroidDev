package com.leeyh.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.leeyh.R

class NoteAdapter(private val data: ArrayList<String>) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.noteTitleTv.text = data[position]
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitleTv: TextView = view.findViewById(R.id.mNoteTitleTv)
    }
}