package com.anubhav.codereverse.uiapp.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

interface PostDelayedContainer {
    fun <T : View> T.delay(delayMs: Long, runnable: T.() -> Unit): Delayed
    fun <T : View> T.cancelDelayed()
}

interface Delayed {
    fun start()
    fun stop()
}

class PostDelayedLifecycleContainer : PostDelayedContainer {
    private val phase = PostDelayedPhase()

    //Yes, we use ViewLifecycleOwnerLiveData, which is recreated every time view is created,
    //so old LifecycleOwner gets discarded, but I think it is more readable, safe and clean to
    //keep in mind currentOwner and call removeObserver, when new is received
    private var currentOwner: LifecycleOwner? = null

    fun attach(fragment: Fragment) {
        //we need to call liveData instead of viewLifecycleOwner, since attach method
        //can be called outside onCreateView/onDestroyView life phase, and
        //viewLifecycleOwner is not acceptable outside onCreateView/onDestroyView
        fragment.viewLifecycleOwnerLiveData.observeForever {
            currentOwner?.lifecycle?.removeObserver(phase)
            currentOwner = it?.also {
                it.lifecycle.addObserver(phase)
                if (it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                    phase.start()
                }
            }
        }
    }

    override fun <T : View> T.delay(delayMs: Long, runnable: T.() -> Unit) =
        phase.postDelayed(this, delayMs, runnable)

    override fun <T : View> T.cancelDelayed() = phase.stopDelayed(this)
}

internal class PostDelayedPhase : LifecycleObserver {
    private var active: Boolean = false
    private val viewMap = LinkedHashMap<View, MutableList<InternalDelayed>>()

    private val removeQueue: Queue<InternalDelayed> = LinkedList()
    private var isTraversing: Boolean = false
    private val remover: (InternalDelayed) -> Unit = {
        if (isTraversing) {
            removeQueue.offer(it)
        } else {
            remove(it)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        active = true
        forEachDelayed { it.start() }
    }

    inline fun <T : View> postDelayed(view: T, delayMs: Long, crossinline runnable: T.() -> Unit) =
        postDelayed(view, delayMs, Runnable { view.runnable() })

    fun postDelayed(view: View, delayMs: Long, runnable: Runnable): Delayed {
        val newDelayed = InternalDelayed(view, runnable, delayMs, remover)
        registerDelayed(newDelayed)
        if (active) newDelayed.start()
        return newDelayed
    }

    private fun registerDelayed(delayed: InternalDelayed) {
        viewMap.getOrPut(delayed.owner) { ArrayList() }.add(delayed)
    }

    fun stopDelayed(view: View) = traverse { viewMap[view]?.forEach { it.stop() } }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun end() {
        forEachDelayed { it.stop() }
        viewMap.clear()
        active = false
    }

    private inline fun forEachDelayed(block: (InternalDelayed) -> Unit) =
        traverse { viewMap.forEach { (_, delayedList) -> delayedList.forEach(block) } }

    private inline fun traverse(block: () -> Unit) {
        isTraversing = true
        block()
        isTraversing = false
        removeAll()
    }

    private fun removeAll() {
        var delayed: InternalDelayed? = null
        do {
            delayed = removeQueue.poll()
            delayed?.let { remove(it) }
        } while (delayed != null)
    }

    private fun remove(delayed: InternalDelayed) {
        viewMap[delayed.owner]?.let { list ->
            list.remove(delayed)
            if (list.isEmpty()) {
                viewMap.remove(delayed.owner)
            }
        }
    }

    private data class InternalDelayed(
        val owner: View,
        val runnable: Runnable,
        val delayMs: Long,
        val remover: (InternalDelayed) -> Unit
    ) : Delayed {
        override fun start() {
            owner.postDelayed({
                runnable.run()
                remover.invoke(this)
            }, delayMs)
        }

        override fun stop() {
            owner.removeCallbacks(runnable)
            remover.invoke(this)
        }
    }
}