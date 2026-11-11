package com.anubhav.codereverse.uiapp.dialogs.common

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DimenRes
import androidx.core.view.setMargins
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.ViewModelFactory
import com.anubhav.commonutility.GlobalData
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class BaseDialogFragment<VB : ViewDataBinding> : DialogFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val preferences get() = MyApp.preference

    // Stores RxJava calls
    private val compositeDisposable = CompositeDisposable()

    abstract fun getViewBinding(): VB

    @DimenRes
    open fun getMargin(): Int = R.dimen.dialog_fragment_margin

    open fun translucentStatusBar(): Boolean = false

    override fun getTheme(): Int {
        return if (translucentStatusBar()) {
            R.style.BaseFragmentDialogTheme_Translucent
        } else {
            R.style.BaseFragmentDialogTheme
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            assert(view != null)
            val parent = view?.parent as View
            val layoutParams = parent.layoutParams as LinearLayout.LayoutParams
            //layoutParams.setMargins(0, 0, 0, 0)
            layoutParams.setMargins(resources.getDimensionPixelSize(getMargin()))
            parent.layoutParams = layoutParams
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.also {
            if (translucentStatusBar()) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                it.window?.setLayout(width, height)
            } else {
                GlobalData.showStatusBar(it.window, true)
                GlobalData.setStatusBarColorRes(it.window, R.color.colorPrimaryDark)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Clear all RxJava calls that have not yet completed
        compositeDisposable.clear()
    }

    fun viewModelFactory(otherUserID: String? = null) =
        ViewModelFactory(requireActivity().application as MyApp, otherUserID)

    inline fun <reified T : ViewModel> initViewModel(otherUserID: String? = null): T =
        ViewModelProvider(this, viewModelFactory(otherUserID)).get(T::class.java)

    fun <T> LiveData<T>.observe(function: (T) -> Unit) =
        this.observe(viewLifecycleOwner, Observer {
            it?.let { function.invoke(it) }
        })

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

}