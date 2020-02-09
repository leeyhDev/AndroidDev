package com.leeyh.ui.note

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.core.base.BaseFragment
import com.leeyh.R
import kotlinx.android.synthetic.main.note_fragment.*

class NoteFragment : BaseFragment() {

    lateinit var viewModel: JavaNoteViewModel

    override fun getLayoutResId(): Int = R.layout.note_fragment

    override fun initViewModel() {
        viewModel = ViewModelProvider(this).get(JavaNoteViewModel::class.java)
        viewModel.titles.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val fragments = ArrayList<NoteListFragment>()
            it.forEach { title ->
                fragments.add(NoteListFragment.newInstance("note/$title"))
            }
            viewPager.offscreenPageLimit = it.size
            viewPager.adapter = object : FragmentPagerAdapter(parentFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int): Fragment = fragments[position]
                override fun getCount(): Int = fragments.size
                override fun getPageTitle(position: Int): CharSequence? = it[position]
            }
            tabLayout.setupWithViewPager(viewPager)
        })
    }

    override fun initView() {
    }

    override fun initData() {
    }
}