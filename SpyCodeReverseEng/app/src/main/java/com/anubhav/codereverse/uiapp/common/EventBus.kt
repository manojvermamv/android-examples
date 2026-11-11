package com.anubhav.codereverse.uiapp.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

object EventBus {
    private val liveDataBus = MutableLiveData<MyEvent>()
    val events: LiveData<MyEvent> = liveDataBus

    fun publish(event: MyEvent) {
        liveDataBus.value = event
    }
}

sealed class MyEvent {
    data class CreateMessage(val postId: String, val uid: String) : MyEvent()
    data class CreateAudioCall(val postId: String, val uid: String) : MyEvent()
    data class CreateVideoCall(val postId: String, val uid: String) : MyEvent()
    data class CreateFollow(val fromUid: String, val toUid: String) : MyEvent()
}

open class Event<out T>(private val content: T) {
    private var isHandled = false

    fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            null
        } else {
            isHandled = true
            content
        }
    }
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let { onEventUnhandledContent(it) }
    }
}