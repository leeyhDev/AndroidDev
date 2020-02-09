package com.leeyh.ui.note

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class JavaNoteViewModel(application: Application) : AndroidViewModel(application) {
    var titles: MutableLiveData<List<String>> = MutableLiveData()
    init {
        val list = application.assets.list("note")
        list?.let {
            titles.postValue(it.toList())
        }
    }
}
