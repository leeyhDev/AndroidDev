package com.leeyh.ui.system

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.core.base.BaseVMFragment
import com.leeyh.R
import kotlinx.android.synthetic.main.system_fragment.*

class SystemFragment : BaseVMFragment<SystemViewModel>() {
    private lateinit var systemAdapter: SystemAdapter
    override fun providerVMClass(): Class<SystemViewModel>? = SystemViewModel::class.java

    override fun getLayoutResId(): Int = R.layout.system_fragment

    override fun initView() {
        systemRv.layoutManager = LinearLayoutManager(context)

        systemRefreshLayout.run {
            isRefreshing = true
            setOnRefreshListener { refresh() }
        }
        initAdapter()
    }

    override fun initData() {
        refresh()
    }

    private fun initAdapter() {
        systemAdapter = SystemAdapter(R.layout.item_system, viewModel.systemList.value)
        systemAdapter.run {
            setOnItemClickListener { _, _, position ->
                LogUtils.d("点击了${position}")
            }
        }
        systemRv.adapter = systemAdapter
    }

    private fun refresh() {
        viewModel.getSystemList()
    }

    override fun startObserve() {
        super.startObserve()
        viewModel.apply {
            systemList.observe(this@SystemFragment, Observer { it ->
                it?.let {
                    systemRefreshLayout.isRefreshing = false
                    systemAdapter.replaceData(it)
                }
            })
        }
    }
}