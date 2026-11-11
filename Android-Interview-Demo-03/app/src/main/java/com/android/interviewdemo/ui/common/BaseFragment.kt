package com.android.interviewdemo.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) :
    Fragment() {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding
            ?: throw RuntimeException("Cannot access binding before onCreateView or after onDestroyView")

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

    fun setDisplayHomeAsUpEnabled() = supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
    }

    fun setTitle(title: String? = null, subtitle: String? = null) = supportActionBar?.apply {
        this.title = title
        this.subtitle = subtitle
    }

    /**
     * Helper functions
     * */
    fun RecyclerView.addOnEndScrollListener(onScrollListener: (() -> Unit)? = null) {
        val mLayoutManager = layoutManager as LinearLayoutManager
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // When scrolling stops
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                    if (lastVisibleItemPosition == mLayoutManager.itemCount - 1) {
                        onScrollListener?.invoke()
                    }
                }
            }
        })
    }

    fun RecyclerView.addOnRightEndScrollListener(onScrollListener: (() -> Unit)? = null) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollHorizontally(1)) {
                    // Scrolled to the right end (last item)
                    onScrollListener?.invoke()
                }
            }
        })
    }

    fun RecyclerView.addOnBottomEndScrollListener(onScrollListener: (() -> Unit)? = null) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                    // Scrolled to the bottom end (last item)
                    onScrollListener?.invoke()
                }
            }
        })
    }

}