package com.anubhav.codereverse.uiapp.firebase.common

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

val auth: FirebaseAuth = FirebaseAuth.getInstance()
val database: DatabaseReference = FirebaseDatabase.getInstance().reference
val storage: StorageReference = FirebaseStorage.getInstance().reference
fun DatabaseReference.liveDataNullable(): LiveData<DataSnapshot?> = FirebaseLiveDataNullable(this)
fun DatabaseReference.liveData(): LiveData<DataSnapshot> = FirebaseLiveData(this)


fun <T> wrapSnapshotToClass(className: Class<T>, snap: DataSnapshot): T? {
    return snap.getValue(className)
}

fun <T> wrapSnapshotToArrayList(className: Class<T>, snap: DataSnapshot): MutableList<T> {
    val arrayList: MutableList<T> = arrayListOf()
    for (child in snap.children) {
        child.getValue(className)?.let { arrayList.add(it) }
    }
    return arrayList
}