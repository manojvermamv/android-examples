package com.anubhav.codereverse.uiapp.data.model

import com.google.gson.annotations.SerializedName

data class DefaultConfig(
    @SerializedName("globalCallInfo") val globalCallInfo: GlobalAppInfo = GlobalAppInfo(),
    @SerializedName("vipPaymentInfo") val vipPaymentInfo: VipPaymentInfo = VipPaymentInfo(),
    @SerializedName("vipMembership") val vipMembership: ArrayList<VipMembershipModel> = arrayListOf(),
    @SerializedName("diamondsInfo") val diamondsInfo: ArrayList<DiamondsInfoModel> = arrayListOf()
)

data class GlobalAppInfo(
    @SerializedName("perMinCharge") val perMinCharge: Int = 10,
    @SerializedName("imagesBaseUrl") val imagesBaseUrl: String = "https://funboxlive.netlify.app/",
    @SerializedName("videoCallEnabled") val videoCallEnabled: Boolean = true,
    @SerializedName("privacyPolicyUrl") val privacyPolicyUrl: String = "",
    @SerializedName("userAgreementUrl") val userAgreementUrl: String = "",
    @SerializedName("aboutUsUrl") val aboutUsUrl: String = ""
)

data class VipPaymentInfo(
    @SerializedName("payeeVpa") val payeeVpa: String = "",
    @SerializedName("payeeName") val payeeName: String = "",
    @SerializedName("payeeMerchantCode") val payeeMerchantCode: String = "",
    @SerializedName("payeeDescription") val payeeDescription: String = "VIP Membership"
)

data class VipMembershipModel(
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("payment") val payment: Int = 0
)

data class DiamondsInfoModel(
    @SerializedName("diamonds") val diamonds: Int = 0,
    @SerializedName("payment") val payment: Int = 0
)