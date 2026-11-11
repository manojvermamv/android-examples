package com.anubhav.codereverse.uiapp.dialogs.common

import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.DimenRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setMargins
import androidx.databinding.ViewDataBinding
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<VB : ViewDataBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val preferences get() = MyApp.preference

    override fun getTheme(): Int = R.style.BottomSheetTheme

    abstract fun getViewBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }


    @DimenRes
    open fun getMargin(): Int = R.dimen.bs_margin

    open fun getHeightInPercentage(): Float = 0f

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            assert(view != null)
            val parent = view?.parent as View
            val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
            //layoutParams.setMargins(0, 0, 0, 0)
            layoutParams.setMargins(resources.getDimensionPixelSize(getMargin()))
            parent.layoutParams = layoutParams
        }
    }

    override fun onStart() {
        super.onStart()
        val mHeight = calculateHeight()

        dialog?.also {
            val bottomSheet = dialog?.findViewById(R.id.design_bottom_sheet) as View
            bottomSheet.layoutParams?.height = mHeight
            val behavior = BottomSheetBehavior.from<View>(bottomSheet)

            if (mHeight > 0) {
                behavior.peekHeight = mHeight
                view?.requestLayout()
            } else {
                val layout = binding.root
                layout.viewTreeObserver?.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        try {
                            layout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                            behavior.peekHeight = layout.height
                            view?.requestLayout()
                        } catch (e: Exception) {
                        }
                    }
                })
            }
        }
    }

    private fun calculateHeight(): Int {
        if (getHeightInPercentage() >= 10f) {
            val displayHeight = Resources.getSystem().displayMetrics.heightPixels
            return calPercentage(displayHeight, getHeightInPercentage())
        }
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private fun calPercentage(value: Int, percentage: Float): Int {
        return (value * (percentage / 100.0f)).toInt()
    }

}