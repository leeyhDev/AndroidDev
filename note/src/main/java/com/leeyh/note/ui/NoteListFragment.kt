package com.leeyh.note.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.core.base.BaseFragment
import com.leeyh.note.R
import com.leeyh.note.adapter.NodeTreeAdapter
import com.leeyh.note.tree.FirstNode
import com.leeyh.note.tree.SecondNode
import kotlinx.android.synthetic.main.note_fragment_item_list.*
import java.util.*

class NoteListFragment : BaseFragment() {

    override fun getLayoutResId(): Int = R.layout.note_fragment_item_list

    override fun initView() {
    }

    override fun initData() {
        arguments?.let { bundle ->
            val type = bundle.getString(NOTE_TYPE, "note_md/Android")
            val rootDir = context?.assets?.list(type)
            val firstNode = ArrayList<BaseNode>()
            rootDir?.forEach { first ->
                if (!first.endsWith(".md")) {
                    val secondNodeList = ArrayList<BaseNode>()
                    val firstDir = context?.assets?.list("$type/$first")
                    firstDir?.forEach { second ->
                        val seNode = SecondNode(null, second, "$type/$first/$second")
                        secondNodeList.add(seNode)
                    }
                    val entity = FirstNode(secondNodeList, first, "$type/$first")
                    firstNode.add(entity)
                } else {
                    val entity = FirstNode(null, first, "$type/$first")
                    firstNode.add(entity)
                }
            }

            rootDir?.let {
                recyclerView.layoutManager = LinearLayoutManager(context)
                val nodeTreeAdapter = NodeTreeAdapter()
                recyclerView.adapter = nodeTreeAdapter
                nodeTreeAdapter.setList(firstNode)
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
