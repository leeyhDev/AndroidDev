package com.flowlayout

import android.view.View

abstract class TagAdapter<T> constructor(datas: List<T>) {
    private var mTagDatas: List<T> = datas
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

    fun getItem(position: Int): T {
        return mTagDatas[position]
    }

    abstract fun getView(parent: FlowLayout, position: Int, item: T): View
    fun onSelected(position: Int, view: View) {
    }

    fun unSelected(position: Int, view: View) {
    }

    fun setSelected(position: Int, item: T): Boolean {
        return false
    }
}