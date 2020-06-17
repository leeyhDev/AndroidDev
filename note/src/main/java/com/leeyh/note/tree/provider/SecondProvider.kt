package com.leeyh.note.tree.provider

import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.leeyh.note.R
import com.leeyh.note.tree.SecondNode
import com.leeyh.note.ui.MarkdownActivity

class SecondProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = 2

    override val layoutId: Int
        get() = R.layout.item_node_second

    override fun convert(helper: BaseViewHolder, data: BaseNode) {
        val entity = data as SecondNode
        helper.setText(R.id.title, entity.title)
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
        val entity = data as SecondNode
        if (entity.childNode.isNullOrEmpty()) {
            val intent = Intent(context, MarkdownActivity::class.java)
            intent.putExtra("markdownPath", entity.path)
            context.startActivity(intent)
        }
    }
}