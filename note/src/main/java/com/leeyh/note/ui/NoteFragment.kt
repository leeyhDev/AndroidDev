package com.leeyh.note.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.core.base.BaseFragment
import com.leeyh.note.R
import kotlinx.android.synthetic.main.note_fragment.*

class NoteFragment : BaseFragment() {
    override fun getLayoutResId(): Int = R.layout.note_fragment
    override fun initView() {
    }

    override fun initData() {
        val list = context?.assets?.list("note_md")
        list?.let {
            val fragments = ArrayList<NoteListFragment>()
            it.forEach { title ->
                fragments.add(
                    NoteListFragment.newInstance(
                        "note_md/$title"
                    )
                )
            }
            viewPager.offscreenPageLimit = it.size
            viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int): Fragment = fragments[position]
                override fun getCount(): Int = fragments.size
                override fun getPageTitle(position: Int): CharSequence? = it[position]
            }
            tabLayout.setViewPager(viewPager)
        }
    }
}