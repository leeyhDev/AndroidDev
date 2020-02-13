package com.leeyh.ui.system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.core.constant.RouterPath
import com.flowlayout.FlowLayout
import com.flowlayout.TagAdapter
import com.flowlayout.TagFlowLayout
import com.leeyh.R
import com.leeyh.model.bean.SystemChildren
import com.leeyh.model.bean.SystemType

class SystemAdapter(layoutId: Int, data: List<SystemType>?) : BaseQuickAdapter<SystemType, BaseViewHolder>(layoutId), LoadMoreModule {
    override fun convert(helper: BaseViewHolder, item: SystemType?) {
        item?.let {
            helper.setText(R.id.nameTv, it.name)
            val tagFlowLayout = helper.getView<TagFlowLayout<SystemChildren>>(R.id.tagFlowLayout)
            val childNames = ArrayList<String>()
            it.children.forEach { child ->
                childNames.add(child.name)
            }
            tagFlowLayout.adapter = object : TagAdapter<SystemChildren>(it.children) {
                override fun getView(parent: FlowLayout, position: Int, item: SystemChildren): View {
                    val textView = LayoutInflater.from(context).inflate(R.layout.item_tag_flow, tagFlowLayout, false) as TextView
                    textView.text = item.name
                    return textView
                }
            }
            tagFlowLayout.setOnTagClickListener(object : TagFlowLayout.OnTagClickListener {
                override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
                    ToastUtils.showShort("点击了${childNames[position]}")
                    ARouter.getInstance().build(RouterPath.SystemList).withParcelable("child", it.children[position])
                        .withInt("position", position).navigation()
                    return true
                }
            })
        }
    }
}