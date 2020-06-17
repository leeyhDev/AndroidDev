package com.leeyh.note.ui.article

import android.text.Html
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.core.constant.RouterPath
import com.leeyh.R
import com.leeyh.model.bean.Article

class ArticleAdapter(layoutId: Int, data: List<Article>?) : BaseQuickAdapter<Article, BaseViewHolder>(layoutId), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: Article) {
        item.let {
            holder.setText(R.id.titleTv, Html.fromHtml(it.title))
            holder.setText(R.id.chapterTv, "${it.superChapterName}/${it.chapterName}")
            holder.setText(
                R.id.shareTv,
                if (StringUtils.isEmpty(it.shareUser)) it.niceShareDate
                else "${it.shareUser}   ${it.niceShareDate} "
            )
            holder.itemView.setOnClickListener { view ->
                ARouter.getInstance().build(RouterPath.Browser).withString("url", item.link).navigation()
            }
        }
    }
}