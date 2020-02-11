package com.leeyh.ui.system

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.core.SpaceItemDecoration
import com.core.base.BaseVMFragment
import com.leeyh.R
import com.leeyh.model.bean.System
import kotlinx.android.synthetic.main.system_fragment.*

class SystemFragment : BaseVMFragment<SystemViewModel>() {
    private lateinit var systemAdapter: SystemAdapter
    override fun providerVMClass(): Class<SystemViewModel>? = SystemViewModel::class.java

    override fun getLayoutResId(): Int = R.layout.system_fragment

    override fun initView() {
        systemRv.run {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(SpaceItemDecoration(SizeUtils.dp2px(12f)))
        }
        initAdapter()
    }

    override fun initData() {
        refresh()
    }

    private fun initAdapter() {
        systemAdapter = SystemAdapter(R.layout.item_home, viewModel.systemList.value!!)
        systemAdapter.run {
            setOnItemClickListener { _, _, position ->
                LogUtils.d("点击了${position}")
            }
        }
        systemRv.adapter = systemAdapter
    }

    private fun refresh() {
        systemAdapter.loadMoreModule?.isEnableLoadMore = false
    }

    override fun startObserve() {
        super.startObserve()
        viewModel.apply {
            systemList.observe(this@SystemFragment, Observer { it ->
                it?.let { setArticles(it) }
            })
        }
    }

    private fun setArticles(systemList: List<System>) {
        systemAdapter.run {
            replaceData(systemList)
            systemAdapter.loadMoreModule?.let {
                it.isEnableLoadMore = true
                it.loadMoreComplete()
            }
        }
    }
}