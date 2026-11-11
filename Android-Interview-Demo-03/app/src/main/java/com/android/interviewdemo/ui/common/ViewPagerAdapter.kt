package com.android.interviewdemo.ui.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    private val manager: FragmentManager,
    private var fragments: MutableList<BaseFragment<out ViewBinding>>,
    lifecycle: Lifecycle
) : FragmentStateAdapter(manager, lifecycle) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long = fragments[position].hashCode().toLong()

    override fun containsItem(itemId: Long): Boolean = fragments.any { it.hashCode().toLong() == itemId }


    fun currentFragment(position: Int): Fragment? = manager.findFragmentByTag("f$position")

    fun add(index: Int, fragment: BaseFragment<out ViewBinding>) {
        fragments.add(index, fragment)
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        fragments.removeAt(index)
        notifyItemChanged(index)
    }

    fun refreshFragment(index: Int, fragment: BaseFragment<out ViewBinding>) {
        fragments[index] = fragment
        notifyItemChanged(index)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseFragment<out ViewBinding>> getItem(position: Int): T {
        val fragment = fragments[position]
        return fragment as T
    }

}