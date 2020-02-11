package com.leeyh.ui.system

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.leeyh.model.bean.System

class SystemAdapter(layoutId: Int, data: List<System>) : BaseQuickAdapter<System, BaseViewHolder>(layoutId), LoadMoreModule {
    override fun convert(helper: BaseViewHolder, item: System?) {
    }
}