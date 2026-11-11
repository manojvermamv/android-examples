package com.anubhav.codereverse.uiapp.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.anubhav.codereverse.databinding.BsAddmaskingUrlBinding
import com.anubhav.codereverse.uiapp.data.model.MaskingUrlData
import com.anubhav.codereverse.uiapp.dialogs.common.BaseBottomSheetDialogFragment
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.utils.Utils
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class BsAddMaskingUrlFragment() : BaseBottomSheetDialogFragment<BsAddmaskingUrlBinding>() {

    private val shortingApis = mutableListOf("https://tinyurl.com/api-create.php?url=", "https://vurl.com/api.php?url=", "https://da.gd/s/?url=",
        "https://is.gd/create.php?format=simple&url=", "https://api.shrtco.de/v2/shorten?url=")
    private val progress by lazy { requireActivity().getCustomProgress() }
    private lateinit var mListener: Listener

    companion object {
        const val TAG = "BsOtpVerification"

        fun show(manager: FragmentManager) {
            val instance = BsAddMaskingUrlFragment()
            instance.isCancelable = true
            instance.show(manager, TAG)
        }
    }

    override fun getViewBinding() = BsAddmaskingUrlBinding.inflate(layoutInflater)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        coordinateBtnAndInputs(binding.btnSubmit, binding.inputOriginalLink, binding.inputMaskLink, binding.inputWords)
        binding.imgClose.setOnClickListener {
            requireActivity().hideKeyboard()
            dismiss()
        }

        binding.inputOriginalLink.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter URL/Link here (with http or https)")
        }

        binding.inputMaskLink.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter Domain to mask the URL (Ex: https://google.com , http://facebook.com)")
        }

        binding.inputWords.onDone { binding.btnSubmit.performClick() }
        binding.inputWords.setOnRightDrawableClickListener {
            requireContext().showConfirmDialog("Info", "Enter social engineering words (Ex: free-money, free-insta-followers)")
        }

        binding.btnSubmit.setOnClickListener { v ->
            val originalUrl = binding.inputOriginalLink.text.toString().trim()
            val maskUrl = binding.inputMaskLink.text.toString().trim()
            val maskWords = binding.inputWords.text.toString().trim()
                .replace(" ", "").replace("\n", "")

            if (!urlChecker(originalUrl) || !urlChecker(maskUrl)) {
                requireContext().showToast("Invalid URL. Please use http or https.")
            } else {
                // Generating Masking Link ...
                progress.show()
                getShortenUrl(originalUrl) { success, res ->
                    if (success) {
                        val shorterUrl = res.replace("http://", "").replace("https://", "")
                        val finalUrl = "$maskUrl-$maskWords@$shorterUrl"
                        mListener.onGenerateSuccessfully(MaskingUrlData(originalUrl, finalUrl, Utils.getTimeStamp()))
                    } else {
                        mListener.onGenerateFailed(res)
                    }
                    progress.dismiss()
                    dismiss()
                }
            }
        }
    }

    private fun getShortenUrl(maskingUrl: String, b: (Boolean, String) -> Unit) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ...
        }, doInBackground = {
            try {
                val requestUrl = URL(preferences.shortingApiUrl() + maskingUrl)
                val httpConn: HttpURLConnection = requestUrl.openConnection() as HttpURLConnection
                httpConn.requestMethod = "GET"

                val responseStatus = httpConn.responseCode == 200
                val inputStream = if (responseStatus) httpConn.inputStream else httpConn.errorStream
                val s: Scanner = Scanner(inputStream).useDelimiter("\\A")
                val response = if (s.hasNext()) s.next() else ""
                if (responseStatus && !response.isNullOrEmpty()) {
                    Pair(true, response)
                } else {
                    throw Exception(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message.isNullOrEmpty()) {
                    Pair(false, "Masking link failed.")
                } else {
                    Pair(false, "Masking link failed: ${e.message!!}")
                }
            }
        }, onPostExecute = { response ->
            b(response.first, response.second)
        })
    }

    private fun urlChecker(url: String): Boolean {
        // Invalid URL. Please use http or https.
        if (url.startsWith("http://")) {
            return true
        } else if (url.startsWith("https://")) {
            return true
        }
        return false
    }

    interface Listener {
        fun onGenerateSuccessfully(data: MaskingUrlData)
        fun onGenerateFailed(message: String)
    }

}