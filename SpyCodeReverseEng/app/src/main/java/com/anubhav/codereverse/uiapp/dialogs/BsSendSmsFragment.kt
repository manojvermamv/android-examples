package com.anubhav.codereverse.uiapp.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.anubhav.codereverse.databinding.BsSendSmsBinding
import com.anubhav.codereverse.uiapp.dialogs.common.BaseBottomSheetDialogFragment
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.ui.socialengineering.SmsServiceType
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URL

class BsSendSmsFragment(
    private val smsServiceType: SmsServiceType,
    private val username: String,
    private val password: String,
    private val token: String
) : BaseBottomSheetDialogFragment<BsSendSmsBinding>() {

    private val progress by lazy { requireActivity().getCustomProgress() }
    private lateinit var mListener: Listener
    private var countryCode: String = "+91"

    companion object {
        const val TAG = "BsSendSmsFragment"

        fun show(manager: FragmentManager, smsServiceType: SmsServiceType, username: String, password: String, token: String) {
            val instance = BsSendSmsFragment(smsServiceType, username, password, token)
            instance.isCancelable = true
            instance.show(manager, TAG)
        }
    }

    override fun getViewBinding() = BsSendSmsBinding.inflate(layoutInflater)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        coordinateBtnAndInputs(binding.btnSubmit, binding.inputFromSenderId, binding.inputRecipientNumber, binding.inputMessage)
        binding.imgClose.setOnClickListener {
            requireActivity().hideKeyboard()
            dismiss()
        }

        binding.inputFromSenderId.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter Sender ID like (Ex: AM-Amazon or 567894)")
        }
        binding.inputRecipientNumber.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter Recipient numbers without +91 or if you want to send same sms to multiple numbers add like (Ex: 0123456789, 9876543210)")
        }

        binding.inputMessage.setTextCounter(binding.inputMessageCounter, 600)
        binding.inputMessage.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter Message here to be sent")
        }
        binding.inputMessage.onDone { binding.btnSubmit.performClick() }

        binding.tvCountryCode.setText(countryCode)
        binding.btnSubmit.setOnClickListener { v ->
            val code = binding.tvCountryCode.text.toString().trim()
            if (code.isNotEmpty()) countryCode = code
            val senderFrom = binding.inputFromSenderId.text.toString().trim()
            val numbers = binding.inputRecipientNumber.text.toString().trim()
            val message = binding.inputMessage.text.toString().trim()

            if (numbers.isEmpty() || countryCode.isEmpty()) {
                requireContext().showToast("Invalid Recipient numbers.")
            } else {
                progress.show()
                sendSms(senderFrom, numbers, message) { response ->
                    println(response)
                    //TO_FACTOR -> {"Status":"Success","Details":"e64f1100-7ef4-4600-99c8-b3b950c22da6"}
                    //FAST_TO_SMS -> {"return":true,"request_id":"lwdtp7cjyqxvfe9", "message":["Message sent successfully"]}
                    var success = false
                    var resMessage = "Sms sent failed."
                    try {
                        val jsonRes = JsonParser.parseString(response).asJsonObject
                        when (smsServiceType) {
                            SmsServiceType.TEXT_BELT -> {
                                success = jsonRes.get("success").asBoolean
                                resMessage = if (success) "Sms sent successfully." else jsonRes.get("error").asString
                            }
                            SmsServiceType.TO_FACTOR -> {
                                success = jsonRes.get("Status").asString == "Success"
                                if (success) resMessage = "Sms sent successfully."
                            }
                            SmsServiceType.WAY_TO_SMS -> {}
                            SmsServiceType.FAST_TO_SMS -> {
                                if (jsonRes.has("return")) {
                                    success = jsonRes.get("return").asBoolean
                                } else if (jsonRes.has("status_code")) {
                                    success = jsonRes.get("status_code").asInt == 200
                                }
                                resMessage = jsonRes.get("message").asString
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {}
                    mListener.onSent(success, resMessage)
                    progress.dismiss()
                    dismiss()
                }
            }
        }
    }

    private fun getUrl(): String {
        return when (smsServiceType) {
            SmsServiceType.TEXT_BELT -> { "https://textbelt.com/text" }
            SmsServiceType.TO_FACTOR -> { "http://2factor.in/API/V1/$token/ADDON_SERVICES/SEND/TSMS" }
            SmsServiceType.WAY_TO_SMS-> { "" }
            SmsServiceType.FAST_TO_SMS-> { "https://www.fast2sms.com/dev/bulkV2" }
            else -> ""
        }
    }

    private fun Request.Builder.getHeaders(): Request.Builder {
        return when (smsServiceType) {
            SmsServiceType.FAST_TO_SMS -> {
                addHeader("authorization", token)
            }
            else -> { this }
        }
    }

    private fun getPostData(from: String, numbers: String, msg: String): String {
        val recipientNumber = "$countryCode${numbers.split(",")[0].trim().takeLast(10)}"
        return when (smsServiceType) {
            SmsServiceType.TEXT_BELT -> {
                val json = JSONObject()
                json.put("phone", recipientNumber)
                json.put("message", msg)
                json.put("key", token)
                json.toString()
            }
            SmsServiceType.TO_FACTOR -> {
                val json = JSONObject()
                json.put("From", from)
                json.put("To", recipientNumber)
                json.put("Msg", msg)
                json.put("TemplateName", "SMS_TEMPLATE")
                json.toString()
            }
            SmsServiceType.WAY_TO_SMS-> { "" }
            SmsServiceType.FAST_TO_SMS-> {
                val json = JSONObject()
                json.put("route", "q")
                json.put("message", msg)
                json.put("language", "unicode")
                json.put("flash", 0)
                json.put("numbers", numbers)
                json.toString()
            }
            else -> ""
        }
    }

    private fun sendSms(senderFrom: String, numbers: String, msg: String, b: (String) -> Unit) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ...
        }, doInBackground = {
            try {
                //for (number in numbers) { }
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val postData = getPostData(senderFrom, numbers, msg)
                val request = Request.Builder()
                    .getHeaders()
                    .url(URL(getUrl()))
                    .post(postData.toRequestBody(mediaType))
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.body!!.string()

                println("Post Body: $postData")
                println("Response Body: $responseBody")
                responseBody
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }, onPostExecute = { response ->
            b(response ?: "")
        })
    }

    interface Listener {
        fun onSent(success: Boolean, message: String)
    }

}