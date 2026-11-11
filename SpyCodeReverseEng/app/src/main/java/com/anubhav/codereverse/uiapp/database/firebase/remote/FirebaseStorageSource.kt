package com.anubhav.codereverse.uiapp.database.firebase.remote

import android.net.Uri
import com.anubhav.codereverse.uiapp.common.IConstants.*
import com.anubhav.codereverse.uiapp.firebase.common.addOnFailure
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.firebase.common.storage
import com.anubhav.commonutility.HelperMethod
import com.google.firebase.storage.FirebaseStorage

// Task based
class FirebaseStorageSource {

    private fun getUserImagesPath(userID: String): String {
        return "$REF_USERS_ROOT/$userID/$REF_USER_IMAGES"
    }

    private fun getChatImagePath(userID: String): String {
        return "$REF_USERS_ROOT/$userID/$REF_CHAT_PHOTO_UPLOAD"
    }

    private fun getAttachmentFilePath(userID: String): String {
        return "$REF_USERS_ROOT/$userID/$REF_CHAT_ATTACHMENT"
    }

    fun uploadUserAvatar(userID: String, localImage: Uri, b: ((Uri?, Exception?) -> Unit)) {
        val ref = storage.child(getUserImagesPath(userID)).child("user_avatar")
        ref.putFile(localImage).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ref.downloadUrl.addOnSuccessListener {
                    b.invoke(it, null)
                }.addOnFailureListener { exception -> b.invoke(null, exception) }
            } else {
                b.invoke(null, task.exception)
            }
        }
    }

    fun uploadUserImage(userID: String, localImage: Uri, b: ((Uri?, Exception?) -> Unit)) {
        val fileName = HelperMethod.getFileName(localImage.toString())
        val ref = storage.child(getUserImagesPath(userID)).child(fileName)
        ref.putFile(localImage).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ref.downloadUrl.addOnSuccessListener {
                    b.invoke(it, null)
                }.addOnFailureListener { exception -> b.invoke(null, exception) }
            } else {
                b.invoke(null, task.exception)
            }
        }
    }

    fun uploadChatImage(userID: String, localImage: Uri, fileName: String, b: (Result<Uri>) -> Unit) {
        b.invoke(Result.Loading)
        val ref = storage.child(getChatImagePath(userID)).child(fileName)
        ref.putFile(localImage).addOnSuccessListener { task ->
            ref.downloadUrl.addOnSuccessListener {
                b.invoke(Result.Success(it))
            }.addOnFailure(b)
        }.addOnFailure(b)
    }

    fun uploadChatAttachment(userID: String, localImage: Uri, fileName: String, b: (Result<Uri>) -> Unit) {
        b.invoke(Result.Loading)
        val ref = storage.child(getAttachmentFilePath(userID)).child(fileName)
        ref.putFile(localImage).addOnSuccessListener { task ->
            ref.downloadUrl.addOnSuccessListener {
                b.invoke(Result.Success(it))
            }.addOnFailure(b)
        }.addOnFailure(b)
    }

    fun deleteUserImage(imageUrl: String, b: (() -> Unit)) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        ref.delete().addOnSuccessListener {
            b.invoke()
        }.addOnFailure()
    }

}