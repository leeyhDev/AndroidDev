package com.leeyh.ui.system

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.core.base.BaseViewModel
import com.leeyh.model.bean.System
import com.leeyh.model.repository.SystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemViewModel : BaseViewModel() {
    private val repository by lazy { SystemRepository() }
    val systemList: MutableLiveData<List<System>> = MutableLiveData()

    fun getSystemList() {
        launch {
            val result = withContext(Dispatchers.IO) {
                repository.getSystem()
            }
            if (result.errorCode == 0) {
                systemList.value = result.data
            } else {
                LogUtils.d(result.errorMsg)
            }
        }
    }
}