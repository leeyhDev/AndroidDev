package com.leeyh.note.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.base.BaseFragment
import com.leeyh.note.R
import com.leeyh.note.adapter.NotetemAdapter
import kotlinx.android.synthetic.main.note_fragment_item_list.*

class NoteListFragment : BaseFragment() {

    override fun getLayoutResId(): Int = R.layout.note_fragment_item_list

    override fun initView() {
    }

    override fun initData() {
        arguments?.let { bundle ->
            val type = bundle.getString(NOTE_TYPE, "note/Android")

            val list = context?.assets?.list(type)
            list?.let {
                recyclerView.layoutManager = LinearLayoutManager(context)
                val notetemAdapter =
                    NotetemAdapter(it.asList(), type)
                recyclerView.adapter = notetemAdapter
                notetemAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val NOTE_TYPE = "note_type"
        @JvmStatic
        fun newInstance(type: String) =
            NoteListFragment().apply {
                arguments = Bundle().apply {
                    putString(NOTE_TYPE, type)
                }
            }
    }
}
