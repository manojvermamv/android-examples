package com.anubhav.devtools.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anubhav.devtools.R
import com.anubhav.devtools.model.models.Tool
import com.anubhav.devtools.ui.activities.MainActivity
import kotlin.random.Random

class MainService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var tools: ArrayList<Tool>

    private var shuffle = false
    private var songPosition = 0
    private val localBinder: IBinder = LocalBinder(this)
    private val random: Random by lazy { Random.Default }

    private var currentTool: Tool? = null
    private val channelId = "my_channel_id"
    private val notificationId = 110
    private var requestCode = 112

    override fun onCreate() {
        super.onCreate()
        songPosition = 0
        mediaPlayer = MediaPlayer()
        initMusicPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("---> onStart ${intent?.action?: "null"}")
        intent?.action?.let {
            when (it) {
                Action.START.name -> start()
                Action.STOP.name -> stopSelf()
                Action.SHUFFLE.name -> setShuffle()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        println("---> onDestroy")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer.stop()
        mediaPlayer.release()
        return false
    }

    private fun start() {
        fun createNotificationChannel(channelId: String, channelName: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(notificationChannel)
            }
            return channelId
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { intent ->
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentTitle("Running")
            .setContentText(currentTool?.name ?: "")
            .setContentIntent(pendingIntent)
            .addAction(getActionIntent(R.drawable.ic_shuffle_24, "Shuffle", Action.SHUFFLE.name))
            .addAction(getActionIntent(R.drawable.ic_stop_circle, "Stop", Action.STOP.name))
            .setTicker(currentTool?.name ?: "")
            .build()
        createNotificationChannel(channelId, getString(R.string.app_name))
        startForeground(notificationId, notification)
    }

    private fun getActionIntent(icon: Int, title: String, action: String): NotificationCompat.Action {
        requestCode += 1
        val actionPi = PendingIntent.getBroadcast(this, requestCode,
            Intent(this, MainService::class.java).apply {
                this.action = action
            }, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(icon, title, actionPi)
    }


    private fun initMusicPlayer() {
        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
    }

    fun playSong() {
        mediaPlayer.reset()
        currentTool = tools[songPosition]
        val musicUri = Uri.EMPTY//currentTool?.getMusicUri() ?: return
        try {
            mediaPlayer.setDataSource(applicationContext, musicUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        mediaPlayer.prepare()
    }

    fun playPrev() {
        songPosition--
        if (songPosition < 0) songPosition = tools.size - 1
        playSong()
    }

    fun playNext() {
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = random.nextInt(tools.size)
            }
            songPosition = newSong
        } else {
            songPosition++
            if (songPosition >= tools.size) songPosition = 0
        }
        playSong()
    }

    fun setShuffle() {
        println("---> setShuffle")
        shuffle = !shuffle
    }

    fun setList(music: ArrayList<Tool>) {
        tools = music
    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    class LocalBinder(private val service: MainService) : Binder() {
        val getService: MainService
            get() = service
    }

    enum class Action {
        START,
        STOP,
        SHUFFLE,
    }

}