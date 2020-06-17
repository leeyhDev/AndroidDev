package com.leeyh.note.ui

import br.tiagohm.markdownview.css.styles.Github
import com.core.base.BaseActivity
import com.leeyh.note.R
import kotlinx.android.synthetic.main.activity_markdown.*

class MarkdownActivity : BaseActivity() {

    override fun getLayoutResId(): Int = R.layout.activity_markdown

    override fun initView() {
        markdownView.addStyleSheet(Github())
    }

    override fun initData() {
        val markdownPath = intent.extras?.getString("markdownPath")
        markdownView.loadMarkdownFromAsset(markdownPath)
    }
}