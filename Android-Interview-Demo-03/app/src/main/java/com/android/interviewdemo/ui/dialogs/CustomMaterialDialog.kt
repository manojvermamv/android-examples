package com.android.interviewdemo.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.DialogNoInternetBinding

class CustomMaterialDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {

    private val binding by lazy { DialogNoInternetBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        setCancelable(false)

        // Set dialog window width to 82% of the screen width
        window?.apply {
            val metrics = context.resources.displayMetrics
            val width = (metrics.widthPixels * 0.82).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setDescription(description: String) {
        binding.tvDescription.text = description
    }

    fun setOnConfirmClickListener(listener: () -> Unit) {
        binding.btnConfirm.setOnClickListener {
            listener.invoke()
            dismiss()
        }
    }
}