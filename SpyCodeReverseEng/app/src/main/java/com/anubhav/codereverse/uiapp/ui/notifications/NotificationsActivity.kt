package com.anubhav.codereverse.uiapp.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.customprogresshud.CustomProgressHUD
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.FragMainNotificationBinding
import com.anubhav.codereverse.uiapp.adapters.NotificationsListAdapter
import com.anubhav.codereverse.uiapp.common.IConstants
import com.anubhav.codereverse.uiapp.database.retrofit.entity.NotificationListData
import com.anubhav.codereverse.uiapp.database.retrofit.remote.RetrofitBuilder
import com.anubhav.codereverse.uiapp.database.retrofit.remote.addCallback
import com.anubhav.codereverse.uiapp.database.retrofit.remote.parseResponse
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.ui.webservice.showWebViewLoadingDialog
import com.anubhav.codereverse.utils.Utils
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader


class NotificationsActivity : BaseActivity<FragMainNotificationBinding>(), NotificationsListAdapter.Listener {

    private lateinit var progress: CustomProgressHUD
    private lateinit var mAdapter: NotificationsListAdapter
    private var serveBaseUrl: String = ""
    private val fileName = "notifications.txt"

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NotificationsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = FragMainNotificationBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()
        //progress.show()

        binding.layActionbar.actionBarTitle = getString(R.string.notifications)
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }
        binding.layActionbar.btnSubmit.text = "Delete"
        binding.layActionbar.btnSubmit.visibility = View.VISIBLE
        binding.layActionbar.btnSubmit.setOnSingleClickListener {
            showConfirmDialog(R.string.confirm_delete_title, R.string.confirm_deleteall_desc) {
                val postData = JSONObject()
                postData.put("user_id", preferences.loggedUID())
                if (preferences.canRequestWithWebView()) {
                    showWebViewLoadingDialog("$serveBaseUrl${IConstants.DELETE_NOTIFICATIONS}", postData.toString()) { dialog, json ->
                        dialog?.dismiss()
                        onDeleteContentResponse(json)
                    }
                } else {
                    progress.show()
                    RetrofitBuilder.create(this).deleteNotifications(postData.toString()).addCallback { isSuccessful, response ->
                        if (isSuccessful) {
                            onDeleteContentResponse(response.parseResponse())
                        }
                        progress.dismiss()
                    }
                }
            }
        }

        serveBaseUrl = preferences.domainBaseUrl()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mAdapter = NotificationsListAdapter(this, this)

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@NotificationsActivity, LinearLayoutManager.VERTICAL))
        }

        val postData = JSONObject()
        postData.put("user_id", preferences.loggedUID())
        if (preferences.canRequestWithWebView()) {
            showWebViewLoadingDialog("$serveBaseUrl${IConstants.GET_NOTIFICATIONS}", postData.toString()) { dialog, json ->
                onGetContentResponse(json) { dialog?.dismiss() }
            }
        } else {
            progress.show()
            RetrofitBuilder.create(this).getNotifications(postData.toString()).addCallback { isSuccessful, response ->
                if (isSuccessful) {
                    onGetContentResponse(response.parseResponse()) { progress.dismiss() }
                }
            }
        }
    }

    private fun updateRecyclerView(list: MutableList<NotificationListData>) {
        if (list.isEmpty()) {
            //list.add(getEmptyItem())
            binding.layNotFound.rootView.visibility = View.VISIBLE
            binding.layActionbar.btnSubmit.visibility = View.GONE
        } else {
            binding.layNotFound.rootView.visibility = View.GONE
            binding.layActionbar.btnSubmit.visibility = View.VISIBLE
            list.sortWith(Comparator { lhs, rhs ->
                val lhsTime = Utils.dateToMillis(lhs.dateTime)
                val rhsTime = Utils.dateToMillis(rhs.dateTime)
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhsTime > rhsTime) -1 else if (lhs.dateTime < rhs.dateTime) 1 else 0
            })
        }
        mAdapter.submitList(list)
    }

    override fun onItemClick(data: NotificationListData) {
    }

    override fun onItemLongClick(data: NotificationListData) {
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
                //setTextFromWebView(binding.websiteUrl, json.optString("absoluteUrl", "This domain not configured yet."))

                val content = json.optString("message")
                Utils.saveStringContent(this@NotificationsActivity, fileName, content)
            } else {
                throw Exception("Response status false")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //val doc = Jsoup.parse(json.optString("message", "Check internet connection or may be this domain not configured yet."))
            //val text = doc.body().text()
            //setErrorFromWebView(binding.websiteUrl, text)

        } finally {
            val list: MutableList<NotificationListData> = mutableListOf()
            try {
                val fin: FileInputStream = openFileInput(fileName)
                val reader = BufferedReader(InputStreamReader(fin))
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    val items = line!!.split(" --> ")
                    list.add(NotificationListData(items[0], items[1], items[2].replace("<<br>>", "\n"), items[3], items[4]))
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