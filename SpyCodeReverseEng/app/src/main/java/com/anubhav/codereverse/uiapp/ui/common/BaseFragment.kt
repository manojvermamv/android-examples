package com.anubhav.codereverse.uiapp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.anubhav.codereverse.uiapp.ui.MyApp
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class BaseFragment<VB : ViewDataBinding> : Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val preferences get() = MyApp.preference

    // Stores RxJava calls
    private val compositeDisposable = CompositeDisposable()

    abstract fun getViewBinding(): VB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getViewBinding()
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        // Clear all RxJava calls that have not yet completed
        compositeDisposable.clear()
    }

    /**
     * Creates an RxJava equivalent to post/postDelayed
     * The disposable is cleared on pause
     * So functions that act on the view can be safely
     * invoked without causing a crash
     */
    protected fun postSafely(callback: () -> Unit, delay: Long = 0, delayTimeUnit: TimeUnit = TimeUnit.SECONDS) {
        // Be careful of the RxJava call ordering
        // Delay must be placed before observeOn
        Completable.complete()
            .subscribeOn(Schedulers.newThread())
            .delay(delay, delayTimeUnit)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(callback)
            .subscribe()
            .addToDisposables()
    }

    private fun Disposable.addToDisposables() = compositeDisposable.add(this)


    fun viewModelFactory(otherUserID: String? = null) = ViewModelFactory(requireActivity().application as MyApp, otherUserID)

    inline fun <reified T : ViewModel> initViewModel(otherUserID: String? = null): T =
        ViewModelProvider(this, viewModelFactory(otherUserID)).get(T::class.java)

    fun <T> LiveData<T>.observe(function: (T) -> Unit) =
        this.observe(viewLifecycleOwner, Observer {
            it?.let { function.invoke(it) }
        })

    companion object {
        const val TAG = "BaseFragment"
    }

}