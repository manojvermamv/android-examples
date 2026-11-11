package com.anubhav.codereverse.uiapp.ui.socialengineering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.anubhav.codereverse.databinding.ActivityIpfinderBinding
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.utils.AddressUtils
import com.anubhav.customprogresshud.CustomProgressHUD
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.URL

class IpFinderActivity : BaseActivity<ActivityIpfinderBinding>(), OnMapReadyCallback {

    private lateinit var progress: CustomProgressHUD
    private val searchIp by lazy { intent.getStringExtra("searchIp") ?: "" }

    companion object {
        fun start(context: Context, searchIp: String = "") {
            val intent = Intent(context, IpFinderActivity::class.java)
            intent.putExtra("searchIp", searchIp)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = ActivityIpfinderBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()

        binding.mapView.onCreate(savedInstanceState)
        binding.layActionbar.actionBarTitle = "IP Finder"
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }

        binding.inputIpAddress.setText(searchIp)
        binding.inputIpAddress.onDone {
            val inputIp = binding.inputIpAddress.text.toString().trim()
            if (inputIp.isEmpty() || !AddressUtils.isIPAddress(inputIp)) {
                showToast("Please ensure you've entered valid IP Address")
                return@onDone
            }
            getIpResponse(inputIp)
        }
        binding.inputIpAddress.setOnRightDrawableClickListener {
            val inputIp = it.text.toString().trim()
            if (inputIp.isEmpty() || !AddressUtils.isIPAddress(inputIp)) {
                showToast("Please ensure you've entered valid IP Address")
                return@setOnRightDrawableClickListener
            }
            getIpResponse(inputIp)
        }

        getIpResponse(searchIp)
    }

    private fun getIpResponse(inputIp: String) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            progress.show()
        }, doInBackground = {
            val url = preferences.ipFinderApiUrl().replace("<IP>", inputIp)
            val response = URL(url).readText()
            try {
                Gson().fromJson(response, IpResponse::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                IpResponse(false)
            }
        }, onPostExecute = { ipResponse ->
            if (ipResponse == null || !ipResponse.status) {
                showToast("Please ensure you've entered your token correctly or An error occurred.")
            } else {
                showIpInfo(ipResponse)
                setMapView(ipResponse.loc)
                binding.displayTitle.visibility = View.VISIBLE
                binding.layData.visibility = View.VISIBLE
                progress.dismiss()
            }
        })
    }

    private fun showIpInfo(ipResponse: IpResponse) {
        binding.layIncludeIp.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/ip.png")
        binding.layIncludeIp.title.text = "IP Address: "
        binding.layIncludeIp.value.text = ipResponse.ip

        binding.layIncludeCountry.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/country.png")
        binding.layIncludeCountry.title.text = "Country: "
        binding.layIncludeCountry.value.text = ipResponse.country

        binding.layIncludeRegion.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/region.png")
        binding.layIncludeRegion.title.text = "Region: "
        binding.layIncludeRegion.value.text = ipResponse.region

        binding.layIncludeCity.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/city.png")
        binding.layIncludeCity.title.text = "City: "
        binding.layIncludeCity.value.text = ipResponse.city

        binding.layIncludeLocation.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/latitude.png")
        binding.layIncludeLocation.title.text = "Location: "
        binding.layIncludeLocation.value.text = ipResponse.loc
        binding.layIncludeLocation.rootView.setOnSingleClickListener { setMapView(ipResponse.loc) }

        binding.layIncludeOrg.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/organization.png")
        binding.layIncludeOrg.title.text = "Organization: "
        binding.layIncludeOrg.value.text = ipResponse.org

        binding.layIncludePostol.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/longitude.png")
        binding.layIncludePostol.title.text = "Postal: "
        binding.layIncludePostol.value.text = ipResponse.postal

        binding.layIncludeTimezone.imgIcon.loadImage("https://www.iplocation.net/assets/img/icons/isp.png")
        binding.layIncludeTimezone.title.text = "Timezone: "
        binding.layIncludeTimezone.value.text = ipResponse.timezone
    }

    private lateinit var mMap: GoogleMap
    private var latLng: LatLng = LatLng("28.6137959".toDouble(), "77.2292638".toDouble())

    private fun setMapView(location: String) {
        try {
            val splits = location.trim().split(",")
            latLng = LatLng(splits[0].toDouble(), splits[1].toDouble());
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        binding.mapView.getMapAsync(this)
        binding.mapView.visibility = View.VISIBLE
        binding.displayTitle2.visibility = View.VISIBLE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(applicationContext);
        mMap = googleMap
        mMap.clear()
        mMap.addMarker(getMarkerOptions())

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener { marker ->
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
            true
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        binding.mapView.onResume()
    }

    private fun getMarkerOptions(): MarkerOptions {
        return MarkerOptions()
            .position(latLng)
            .draggable(false)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

}

data class IpResponse constructor(
    @SerializedName("status") var status: Boolean = true,
    @SerializedName("ip") var ip: String = "",
    @SerializedName("city") var city: String = "",
    @SerializedName("region") var region: String = "",
    @SerializedName("country") var country: String = "",
    @SerializedName("loc") var loc: String = "",
    @SerializedName("org") var org: String = "",
    @SerializedName("postal") var postal: String = "",
    @SerializedName("timezone") var timezone: String = ""
)