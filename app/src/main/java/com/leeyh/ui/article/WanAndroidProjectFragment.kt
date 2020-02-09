package com.leeyh.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.leeyh.R

class WanAndroidProjectFragment : Fragment() {

    companion object {
        fun newInstance() = WanAndroidProjectFragment()
    }

    private lateinit var viewModel: WanAndroidProjectViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.wan_android_fragment_project, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WanAndroidProjectViewModel::class.java)
    }

}
