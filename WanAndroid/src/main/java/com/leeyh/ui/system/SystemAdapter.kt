package com.leeyh.ui.system

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.flowlayout.FlowLayout
import com.flowlayout.TagAdapter
import com.flowlayout.TagFlowLayout
import com.leeyh.R
import com.leeyh.model.bean.System

class SystemAdapter(layoutId: Int, data: List<System>?) : BaseQuickAdapter<System, BaseViewHolder>(layoutId), LoadMoreModule {
    override fun convert(helper: BaseViewHolder, item: System?) {
        item?.let {
            helper.setText(R.id.nameTv, it.name)
            val tagFlowLayout = helper.getView<TagFlowLayout>(R.id.tagFlowLayout)
            val childNames = ArrayList<String>()
            it.children.forEach { child ->
                childNames.add(child.name)
            }
            tagFlowLayout.adapter = object : TagAdapter(childNames) {
                override fun getView(parent: FlowLayout, position: Int, content: String): View {
                    val textView = LayoutInflater.from(context).inflate(R.layout.item_sliding, tagFlowLayout, false) as TextView
                    textView.text = content
                    return textView
                }
            }
            tagFlowLayout.setOnTagClickListener(object : TagFlowLayout.OnTagClickListener {
                override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
                    ToastUtils.showShort("点击了${childNames[position]}")
                    return true
                }
            })
        }
    }
}