package com.anubhav.codereverse.uiapp.fcm

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.anubhav.commonutility.backgroundtasks.AppExecutors
import com.anubhav.codereverse.uiapp.utils.Log
import com.anubhav.codereverse.uiapp.common.IConstants
import com.anubhav.codereverse.uiapp.utils.JSONUtils
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.json.JSONObject

object FCMUtils {

    fun sendNotification(fcmToken: String, notifyTitle: String? = null, notifyContent: String? = null, model: NotificationData? = null, b: ((String) -> Unit)? = null) {
        AppExecutors.getInstance().networkIO().execute {
            try {
                val json = JSONObject()
                json.put("to", fcmToken)
                if (!TextUtils.isEmpty(notifyTitle) && !TextUtils.isEmpty(notifyContent)) {
                    val notificationJson = JSONObject()
                    notificationJson.put("title", notifyTitle)
                    notificationJson.put("body", notifyContent)
                    json.put("notification", notificationJson)
                }
                if (model != null) {
                    val dataJson = JSONObject()
                    dataJson.put(IConstants.FCM_DATA, JSONUtils.objectToJson(model))
                    if (JSONUtils.checkObjectSize(dataJson.toString().toByteArray()) > 4000) {
                        throw Exception("The data payload limit according to the current documentation is 4K. \nSee: https://firebase.google.com/docs/cloud-messaging/concept-options")
                    }
                    json.put("data", dataJson)
                }
                if (!json.has("notification") && !json.has("data")) {
                    throw Exception("Notification or Data payload not available to send them!")
                }

                // send notification now
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                val request = Request.Builder()
                    .header("Authorization", "key=" + IConstants.FCM_SERVER_KEY)
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .build()
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                // check notification sent success or not
                val finalResponse = response.body().string()
                val resultJson = JSONObject(finalResponse)
                val success = resultJson.getInt("success")
                val failure = resultJson.getInt("failure")
                var msg = "Firebase push message Failed\n" + "Success : " + success + "Failure : " + failure
                if (success > 0 && failure == 0) {
                    msg = "Firebase push message sent successfully."
                }
                Log.i("FirebaseUtils", "sendNotification ---> $finalResponse")
                Handler(Looper.getMainLooper()).post {
                    b?.invoke(msg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.getErrors(this, e)
                Handler(Looper.getMainLooper()).post {
                    b?.invoke(e.message ?: e.toString())
                }
            }
        }
    }

}