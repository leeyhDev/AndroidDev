package com.leeyh.note.adapter

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.leeyh.note.tree.FirstNode
import com.leeyh.note.tree.SecondNode
import com.leeyh.note.tree.provider.FirstProvider
import com.leeyh.note.tree.provider.SecondProvider

class NodeTreeAdapter : BaseNodeAdapter() {
    override fun getItemType(
        data: List<BaseNode>,
        position: Int
    ): Int {
        val node = data[position]
        if (node is FirstNode) {
            return 1
        } else if (node is SecondNode) {
            return 2
        }
        return -1
    }

    companion object {
        const val EXPAND_COLLAPSE_PAYLOAD = 110
    }

    init {
        addNodeProvider(FirstProvider())
        addNodeProvider(SecondProvider())
    }
}