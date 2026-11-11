package com.anubhav.codereverse.uiapp.firebase.common;

import android.net.Uri
import android.util.Log
import com.anubhav.codereverse.uiapp.ui.common.executeAsyncTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import java.io.File

class FirebaseUploader(private val uploadRef: StorageReference, private val uploadListener: UploadListener) {

    private var uploadTask: UploadTask? = null

    fun uploadFile(scope: CoroutineScope, file: File) {
        scope.executeAsyncTask(onPreExecute = {
            // ...
        }, doInBackground = {
            // send data to "onPostExecute"
            try {
                Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }, onPostExecute = {
            // here "it" is a data returned from "doInBackground"
            if (it != null) {
                checkIfExists(it)
            }
        })
    }

    private fun checkIfExists(fileUri: Uri) {
        uploadRef.downloadUrl.addOnSuccessListener {
            if (it != null) {
                uploadListener.onUploadSuccess(it.toString())
            } else {
                upload(fileUri)
            }
        }.addOnFailureListener { upload(fileUri) }
    }

    private fun upload(fileUri: Uri) {
        Log.e("FirebaseUploader", "Upload fileURI:::: $fileUri")

        uploadTask = uploadRef.putFile(fileUri)
        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Task unsuccessful")
            }
            // Continue with the task to get the download URL
            return@continueWithTask uploadRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri: Uri = task.result
                uploadListener.onUploadSuccess(downloadUri.toString());
            } else {
                uploadListener.onUploadFail(task.exception?.message ?: "unsuccessful");
            }
        }?.addOnFailureListener {
            uploadListener.onUploadFail(it.message ?: "Failed")
        }

        uploadTask?.addOnProgressListener { taskSnapshot ->
            val progress: Long = 100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            uploadListener.onUploadProgress(progress.toInt())
        }
    }

    fun cancelUpload() {
        if (uploadTask != null && uploadTask?.isInProgress == true) {
            uploadTask?.cancel();
            uploadListener.onUploadCancelled();
        }
    }
}

interface UploadListener {
    fun onUploadFail(message: String)
    fun onUploadSuccess(downloadUrl: String)
    fun onUploadProgress(progress: Int)
    fun onUploadCancelled()
}