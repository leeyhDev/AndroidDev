package com.flowlayout

import android.view.View

abstract class TagAdapter constructor(datas: List<String>) {
    private var mTagDatas: List<String> = datas
    private var mOnDataChangedListener: OnDataChangedListener? = null

    interface OnDataChangedListener {
        fun onChanged()
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener) {
        mOnDataChangedListener = listener
    }

    val count: Int
        get() = mTagDatas.size

    fun notifyDataChanged() {
        mOnDataChangedListener?.onChanged()
    }

    fun getItem(position: Int): String {
        return mTagDatas[position]
    }

    abstract fun getView(parent: FlowLayout, position: Int, content: String): View
    fun onSelected(position: Int, view: View) {
    }

    fun unSelected(position: Int, view: View) {
    }

    fun setSelected(position: Int, content: String): Boolean {
        return false
    }
}