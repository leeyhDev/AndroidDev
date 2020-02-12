package com.flowlayout

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.flowlayout.TagAdapter.OnDataChangedListener
import java.util.*

class TagFlowLayout<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FlowLayout(context, attrs, defStyle), OnDataChangedListener {
    private lateinit var mTagAdapter: TagAdapter<T>
    private var mSelectedMax = -1 //-1为不限制数量
    private val mSelectedView: MutableSet<Int> = HashSet()
    private var mOnSelectListener: OnSelectListener? = null
    private var mOnTagClickListener: OnTagClickListener? = null

    interface OnSelectListener {
        fun onSelected(selectPosSet: Set<Int>)
    }

    interface OnTagClickListener {
        fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cCount = childCount
        for (i in 0 until cCount) {
            val tagView: TagView = getChildAt(i) as TagView
            if (tagView.visibility == View.GONE) {
                continue
            }
            if (tagView.tagView.visibility == View.GONE) {
                tagView.visibility = View.GONE
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setOnSelectListener(onSelectListener: OnSelectListener) {
        mOnSelectListener = onSelectListener
    }

    fun setOnTagClickListener(onTagClickListener: OnTagClickListener?) {
        mOnTagClickListener = onTagClickListener
    }

    private fun changeAdapter() {
        removeAllViews()
        val adapter = mTagAdapter
        var tagViewContainer: TagView? = null
        for (i in 0 until adapter.count) {
            val tagView = adapter.getView(this, i, adapter.getItem(i))
            tagViewContainer = TagView(context)
            tagView.isDuplicateParentStateEnabled = true
            if (tagView.layoutParams != null) {
                tagViewContainer.layoutParams = tagView.layoutParams
            } else {
                val lp = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                lp.setMargins(
                    dip2px(context, 5f),
                    dip2px(context, 5f),
                    dip2px(context, 5f),
                    dip2px(context, 5f)
                )
                tagViewContainer.layoutParams = lp
            }
            val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            tagView.layoutParams = lp
            tagViewContainer.addView(tagView)
            addView(tagViewContainer)
            if (mTagAdapter.setSelected(i, adapter.getItem(i))) {
                setChildChecked(i, tagViewContainer)
            }
            tagView.isClickable = false
            val finalTagViewContainer: TagView = tagViewContainer
            tagViewContainer.setOnClickListener(OnClickListener {
                doSelect(finalTagViewContainer, i)
                mOnTagClickListener?.onTagClick(finalTagViewContainer, i, this@TagFlowLayout)
            })
        }
    }

    fun setMaxSelectCount(count: Int) {
        if (mSelectedView.size > count) {
            Log.w(TAG, "you has already select more than $count views , so it will be clear .")
            mSelectedView.clear()
        }
        mSelectedMax = count
    }

    val selectedList: Set<Int>
        get() = HashSet(mSelectedView)

    private fun setChildChecked(position: Int, view: TagView) {
        view.isChecked = true
        mTagAdapter.onSelected(position, view.tagView)
    }

    private fun setChildUnChecked(position: Int, view: TagView) {
        view.isChecked = false
        mTagAdapter.unSelected(position, view.tagView)
    }

    private fun doSelect(child: TagView, position: Int) {
        if (!child.isChecked) { //处理max_select=1的情况
            if (mSelectedMax == 1 && mSelectedView.size == 1) {
                val iterator: Iterator<Int> = mSelectedView.iterator()
                val preIndex = iterator.next()
                val pre: TagView = getChildAt(preIndex) as TagView
                setChildUnChecked(preIndex, pre)
                setChildChecked(position, child)
                mSelectedView.remove(preIndex)
                mSelectedView.add(position)
            } else {
                if (mSelectedMax > 0 && mSelectedView.size >= mSelectedMax) {
                    return
                }
                setChildChecked(position, child)
                mSelectedView.add(position)
            }
        } else {
            setChildUnChecked(position, child)
            mSelectedView.remove(position)
        }
        mOnSelectListener?.onSelected(HashSet(mSelectedView))
    }

    var adapter: TagAdapter<T>
        get() = mTagAdapter
        set(adapter) {
            mTagAdapter = adapter
            mTagAdapter.setOnDataChangedListener(this)
            mSelectedView.clear()
            changeAdapter()
        }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState())
        var selectPos = ""
        if (mSelectedView.size > 0) {
            for (key in mSelectedView) {
                selectPos += "$key|"
            }
            selectPos = selectPos.substring(0, selectPos.length - 1)
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val mSelectPos = state.getString(KEY_CHOOSE_POS)
            if (!TextUtils.isEmpty(mSelectPos)) {
                val split = mSelectPos!!.split("\\|").toTypedArray()
                for (pos in split) {
                    val index = pos.toInt()
                    mSelectedView.add(index)
                    val tagView: TagView = getChildAt(index) as TagView
                    setChildChecked(index, tagView)
                }
            }
            super.onRestoreInstanceState(state.getParcelable(KEY_DEFAULT))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onChanged() {
        mSelectedView.clear()
        changeAdapter()
    }

    companion object {
        private const val TAG = "TagFlowLayout"
        private const val KEY_CHOOSE_POS = "key_choose_pos"
        private const val KEY_DEFAULT = "key_default"
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout)
        mSelectedMax = ta.getInt(R.styleable.TagFlowLayout_max_select, -1)
        ta.recycle()
    }
}