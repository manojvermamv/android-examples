package com.anubhav.devtools.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) : Fragment() {

    private var _binding: B? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingFactory(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val supportActionBar: ActionBar? by lazy { (activity as? AppCompatActivity)?.supportActionBar }

    fun setSupportActionBar(toolbar: Toolbar) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
    }

    fun setDisplayHomeAsUp(enabled: Boolean = true) = supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(enabled)
        setHomeButtonEnabled(enabled)
    }

    fun setTitle(title: String? = null, subtitle: String? = null) = supportActionBar?.apply {
        this.title = title
        this.subtitle = subtitle
    }

}