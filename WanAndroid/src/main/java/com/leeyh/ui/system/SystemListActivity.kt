package com.leeyh.ui.system

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.core.base.BaseActivity
import com.core.constant.RouterPath
import com.leeyh.R
import com.leeyh.model.bean.SystemChildren
import kotlinx.android.synthetic.main.system_list_activity.*

@Route(path = RouterPath.SystemList)
class SystemListActivity : BaseActivity() {

    companion object {
        const val SYSTEM = "system"
    }

    override fun getLayoutResId(): Int = R.layout.system_list_activity

    override fun initView() {
        ARouter.getInstance().inject(this@SystemListActivity)
        viewPager.offscreenPageLimit = 5
    }

    override fun initData() {
        val parcelableExtra = intent.getParcelableExtra<SystemChildren>("child")
        LogUtils.d("name:${parcelableExtra?.name}")
    }

}