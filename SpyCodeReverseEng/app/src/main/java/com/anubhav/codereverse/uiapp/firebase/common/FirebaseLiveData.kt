package com.anubhav.codereverse.uiapp.firebase.common

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query

class FirebaseLiveData(private val query: Query, private val isSingleValueListener: Boolean = false) :
    LiveData<DataSnapshot>() {
    private val listener =  ValueEventListenerAdapter {
        if (it != null) {
            value = it
        }
    }

    override fun onActive() {
        super.onActive()
        if (isSingleValueListener) {
            query.addListenerForSingleValueEvent(listener)
        } else {
            query.addValueEventListener(listener)
        }
    }

    override fun onInactive() {
        super.onInactive()
        query.removeEventListener(listener)
    }
}

class FirebaseLiveDataNullable(private val query: Query, private val isSingleValueListener: Boolean = false) :
    LiveData<DataSnapshot?>() {
    private val listener =  ValueEventListenerAdapter {
        value = it
    }

    override fun onActive() {
        super.onActive()
        if (isSingleValueListener) {
            query.addListenerForSingleValueEvent(listener)
        } else {
            query.addValueEventListener(listener)
        }
    }

    override fun onInactive() {
        super.onInactive()
        query.removeEventListener(listener)
    }
}