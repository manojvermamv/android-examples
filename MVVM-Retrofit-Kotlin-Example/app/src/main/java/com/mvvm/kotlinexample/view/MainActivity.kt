package com.mvvm.kotlinexample.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mvvm.kotlinexample.R
import com.mvvm.kotlinexample.databinding.ActivityMainBinding
import com.mvvm.kotlinexample.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var views: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        views.btnClick.setOnClickListener {

            views.progressBar.isVisible = true

            viewModel.getUser()!!.observe(this, Observer {

                views.progressBar.isVisible = false

                views.lblResponse.text = it.message
            })
        }
    }

}