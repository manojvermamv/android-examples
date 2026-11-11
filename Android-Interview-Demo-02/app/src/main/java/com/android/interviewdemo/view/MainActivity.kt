package com.android.interviewdemo.view

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ActivityMainBinding
import com.android.interviewdemo.model.AudioFile
import com.android.interviewdemo.utils.setLightStatusBar
import com.android.interviewdemo.utils.goToSettings
import com.android.interviewdemo.utils.registerForPermissionResult
import com.android.interviewdemo.utils.requestPermission
import com.android.interviewdemo.utils.showToast
import com.android.interviewdemo.view.common.BaseActivity
import com.android.interviewdemo.viewmodel.MainActivityViewModel
import com.android.interviewdemo.viewmodel.MainActivityViewModel.Companion.PLAYING
import com.android.interviewdemo.viewmodel.MainActivityViewModel.Companion.RECORDING
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var viewModel: MainActivityViewModel

    private val audio by lazy { AudioFile(this, "recording") }

    private var alertDialog: AlertDialog? = null

    private val recordAudioResult = registerForPermissionResult { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            showToast("Audio recording permission denied! Go to permissions settings")
            goToSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(window, true)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        title = getString(R.string.audio_recorder)
        setAudioAvailability()

        viewModel.state.observe(this, Observer {
            it?.let { state ->
                when (state) {
                    RECORDING -> {
                        if (alertDialog == null || alertDialog?.isShowing == false) {
                            alertDialog =
                                MaterialAlertDialogBuilder(this@MainActivity).setTitle("Recording sound...")
                                    .setCancelable(false)
                                    .setNegativeButton("Stop") { dialog, _ ->
                                        dialog.dismiss()
                                        binding.fabRecord.performClick()
                                    }.create()
                            alertDialog?.show()
                        }
                    }

                    PLAYING -> {
                        if (alertDialog == null || alertDialog?.isShowing == false) {
                            alertDialog =
                                MaterialAlertDialogBuilder(this@MainActivity).setTitle("Playing sound...")
                                    .setCancelable(false)
                                    .setNegativeButton("Stop") { dialog, _ ->
                                        dialog.dismiss()
                                        binding.fabPlay.performClick()
                                    }.create()
                            alertDialog?.show()
                        }
                    }

                    else -> {
                        if (alertDialog?.isShowing == true) {
                            alertDialog?.dismiss()
                            alertDialog = null
                            setAudioAvailability()
                        }
                    }
                }
            }
        })

        binding.fabRecord.setOnClickListener {
            if (viewModel.isRecording()) {
                viewModel.stopRecording()
                setAudioAvailability()
                showToast("Recording stopped.")
            } else {
                requestPermission(Manifest.permission.RECORD_AUDIO, recordAudioResult, this::onPermissionGranted)
            }
        }

        binding.fabPlay.setOnClickListener {
            if (viewModel.isPlaying()) {
                viewModel.stopPlayback()
                showToast("Stop playing")
            } else {
                showToast("Start playing")
                viewModel.startPlayback(audio.getPath())
            }
        }
    }

    private fun setAudioAvailability() {
        if (audio.exists()) {
            binding.textViewTitle.text = "Recording Path: \n${audio.getPath()}"
            binding.fabPlay.visibility = View.VISIBLE
        } else {
            binding.textViewTitle.text = getString(R.string.recording_not_found)
            binding.fabPlay.visibility = View.GONE
        }
    }

    private fun onPermissionGranted() {
        // Permission granted, you can now use the camera
        showToast("Recording started.")
        viewModel.startRecording(audio.getPath())
    }

}