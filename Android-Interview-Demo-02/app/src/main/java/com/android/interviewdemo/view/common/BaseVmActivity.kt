package com.android.interviewdemo.view.common

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.android.interviewdemo.R
import java.lang.reflect.ParameterizedType

abstract class BaseVmActivity<B : ViewBinding, VM : ViewModel>(val bindingFactory: (LayoutInflater) -> B) : AppCompatActivity() {

    private var _binding: B? = null
    protected val binding get() = _binding!!

    lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(getViewModelClass())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getViewModelClass(): Class<VM> {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        return type as Class<VM>
    }

}