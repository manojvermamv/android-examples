package com.android.interviewdemo.viewmodel

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException


class MainActivityViewModel : ViewModel() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var isPlaying = false
    private val handler = Handler()
    private val recordingDuration = 2 * 60 * 1000 // 2 minutes

    companion object {
        const val IDEAL = 0
        const val RECORDING = 1
        const val PLAYING = 2
    }


    var state = MutableLiveData<Int>()
    init {
        state.value = IDEAL
    }

    private fun changeState(newState: Int) {
        state.value = newState
    }

    fun startRecording(outputFile: String?) {
        if (!isRecording) {
            try {
                mediaRecorder = MediaRecorder()
                mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mediaRecorder!!.setOutputFile(outputFile)
                mediaRecorder!!.prepare()
                mediaRecorder!!.start()
                isRecording = true

                changeState(RECORDING)

                // Automatically stop recording after the specified duration
                handler.postDelayed(Runnable { stopRecording() }, recordingDuration.toLong())
            } catch (e: IOException) {
                e.printStackTrace()
                changeState(IDEAL)
            }
        }
    }

    fun stopRecording() {
        if (isRecording) {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
            isRecording = false
        }
        changeState(IDEAL)
    }

    fun startPlayback(audioFilePath: String?) {
        if (!isPlaying) {
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setDataSource(audioFilePath)
                mediaPlayer!!.prepare()
                mediaPlayer!!.start()
                mediaPlayer!!.setOnCompletionListener { changeState(IDEAL) }
                isPlaying = true

                changeState(PLAYING)
            } catch (e: IOException) {
                e.printStackTrace()
                changeState(IDEAL)
            }
        }
    }

    fun stopPlayback() {
        if (isPlaying) {
            mediaPlayer!!.release()
            mediaPlayer = null
            isPlaying = false
        }
        changeState(IDEAL)
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

}