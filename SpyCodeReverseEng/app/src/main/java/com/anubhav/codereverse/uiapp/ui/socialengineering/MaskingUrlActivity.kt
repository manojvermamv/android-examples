package com.anubhav.codereverse.uiapp.ui.socialengineering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.ActivityMaskingurlBinding
import com.anubhav.codereverse.uiapp.adapters.MaskingUrlAdapter
import com.anubhav.codereverse.uiapp.data.model.MaskingUrlData
import com.anubhav.codereverse.uiapp.dialogs.BsAddMaskingUrlFragment
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.utils.Utils
import com.anubhav.customprogresshud.CustomProgressHUD
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject


class MaskingUrlActivity : BaseActivity<ActivityMaskingurlBinding>(), MaskingUrlAdapter.Listener, BsAddMaskingUrlFragment.Listener {

    private lateinit var progress: CustomProgressHUD
    private lateinit var mAdapter: MaskingUrlAdapter
    private val localFileName = "maskingurls.json"

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MaskingUrlActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = ActivityMaskingurlBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()

        binding.layActionbar.actionBarTitle = "Masking Url"
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }
        initRecyclerView()

        binding.btnAddLink.setOnSingleClickListener {
            BsAddMaskingUrlFragment.show(supportFragmentManager)
        }
    }

    override fun onGenerateSuccessfully(data: MaskingUrlData) {
        if (mAdapter.itemExits(data.maskingUrl)) {
            showToast("This link already masked!")
        } else {
            Utils.saveJsonObjectInArray(this@MaskingUrlActivity, localFileName, JSONObject(Gson().toJson(data)))
            updateRecyclerView()
        }
    }

    override fun onGenerateFailed(message: String) {
        showToast(message)
    }

    private fun initRecyclerView() {
        mAdapter = MaskingUrlAdapter(this, this)

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@MaskingUrlActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@MaskingUrlActivity, LinearLayoutManager.VERTICAL))
        }
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        val list: MutableList<MaskingUrlData> = mutableListOf()
        try {
            var maskingUrlArray = Utils.getStringFromFile(this@MaskingUrlActivity, localFileName)
            if (TextUtils.isEmpty(maskingUrlArray)) maskingUrlArray = "[]";
            val jsonArr = JsonParser.parseString(maskingUrlArray).asJsonArray
            for (element in jsonArr) {
                list.add(Gson().fromJson(element, MaskingUrlData::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (list.isEmpty()) {
            binding.layNotFound.rootView.visibility = View.VISIBLE
        } else {
            binding.layNotFound.rootView.visibility = View.GONE
            list.sortWith(Comparator { lhs, rhs ->
                val lhsTime = Utils.dateToMillis(lhs.dateTime)
                val rhsTime = Utils.dateToMillis(rhs.dateTime)
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhsTime > rhsTime) -1 else if (lhs.dateTime < rhs.dateTime) 1 else 0
            })
        }
        mAdapter.submitList(list)
    }

    override fun onItemLongClick(data: MaskingUrlData) {
        showConfirmDialog(R.string.confirm_delete_title, R.string.confirm_delete_desc) {
            try {
                val updatedArray = JSONArray()
                for (i in 0 until mAdapter.itemCount) {
                    val item = mAdapter.getItemAt(i)
                    if (item.dateTime != data.dateTime) {
                        updatedArray.put(JSONObject(Gson().toJson(item)))
                    }
                }
                Utils.saveStringContent(this@MaskingUrlActivity, localFileName, updatedArray.toString())
                updateRecyclerView()
                showToast("Masking link deleted successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error: ${e.message}")
            }
        }
    }

}