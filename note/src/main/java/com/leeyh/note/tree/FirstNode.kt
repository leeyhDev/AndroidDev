package com.leeyh.note.tree

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class FirstNode(
    override val childNode: MutableList<BaseNode>?,
    val title: String,
    val path: String
) : BaseExpandNode() {
    init {
        isExpanded = false
    }
}