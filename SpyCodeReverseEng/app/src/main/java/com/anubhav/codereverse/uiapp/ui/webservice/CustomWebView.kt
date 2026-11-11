package com.anubhav.codereverse.uiapp.ui.webservice

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.webkit.*
import android.widget.Toast
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.common.IConstants.API_REQUEST_PAGE
import com.anubhav.codereverse.uiapp.database.preferences.domainBaseUrl
import com.anubhav.codereverse.uiapp.utils.TextUtil
import com.gargoylesoftware.htmlunit.HttpMethod
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebRequest
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.google.gson.JsonParser
import com.squareup.okhttp.*
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpCookie
import java.net.URL


class CustomWebView {
}

internal class MyJavaScriptInterface constructor(private val dialog: Dialog, private val callback: ((Dialog?, JSONObject) -> Unit)) {

    private val context: Context = dialog.context
    private val TAG = "MyJavaScriptInterface"

    fun processHTML(html: String): String {
        var result = ""
        try {
            result = TextUtil.parseHtml(html.replace("\\u003C", "<"))
            result = JsonParser.parseString(result).asString
        } catch (e: Exception) { }
        return result
    }

    @JavascriptInterface
    fun log(log: String?) {
        Log.e(TAG, "Log -> ${log ?: "null"}")
    }

    @JavascriptInterface
    fun showToast(string: String?) {
        Toast.makeText(context, string?: "null", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    fun onError(e: String?) {
        val error = Error(e);
        dialog.dismiss()
        Log.e(TAG, "Error -> ${error.message}")
        throw error
    }

    @JavascriptInterface
    fun onPostRequest(data: String?) {
        var json = JSONObject()
        if (data == null) {
            callback(dialog, json)
            return
        }
        try {
            json = JSONObject(data)
        } catch (e: Exception) {
            var html = ""
            try {
                html = processHTML(data)
                json = JSONObject(html)
            } catch (e2: Exception) {
                Log.e(TAG, "OnPostRequest -> $html")
                json = JSONObject()
                json.put("status", false)
                json.put("message", html)
                e2.printStackTrace()
            }
        }
        callback(dialog, json)
    }

}

fun WebView.callJavaScriptFunction(methodName: String, vararg params: Any) {
    post {
        val sb = StringBuilder()
        sb.append("javascript:try{")
        sb.append(methodName)
        sb.append("(")
        var separator = ""
        for (param in params) {
            sb.append(separator)
            separator = ","
            if (param is String) {
                sb.append("'")
            }
            sb.append(param.toString().replace("'", "\\'"))
            if (param is String) {
                sb.append("'")
            }
        }
        sb.append(")}catch(error){Android.onError(error.message);}")
        //loadUrl(sb.toString())
        evaluateJavascript(sb.toString(), null)
    }
}

fun WebView.callJavaScriptOnDeceptiveSite(callback: (() -> Unit)? = null) {
    post {
        val sb = StringBuilder("javascript:try{\n" +
                "        var message = document.getElementById('main-message');\n" +
                "        if (message.innerText.toLowerCase().includes('deceptive site ahead')) {\n" +
                "            document.querySelector('#proceed-link').click();\n" +
                "            Android.onDeceptiveSiteBypass(true);\n" +
                "        } else {\n" +
                "            throw Error('not a deceptive site ahead');\n" +
                "        }\n" +
                "    }catch(error){\n" +
                "        console.log(error.message);\n" +
                "        Android.onDeceptiveSiteBypass(false);\n" +
                "    }")
        evaluateJavascript(sb.toString()) {
            println(it)
            callback?.invoke()
        }
    }
}

/**
 * Example ->
 *
 * showWebViewLoadingDialog("https://example.com/api/services/endPoint", Gson().toJson(model)) { dialog, json ->
 * }
 * */
fun Context.showWebViewLoadingDialog(webUrl: String, json: String, callback: ((Dialog?, JSONObject) -> Unit)) {
    if (webUrl.isEmpty()) return
    val dialog = Dialog(this, R.style.TransparentDialogTheme)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_webview_loading)

    val webView = dialog.findViewById<WebView>(R.id.webView)
    dialog.show()

    webView.webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            request.grant(request.resources)
        }
    }
    webView.settings.javaScriptEnabled = true
    webView.settings.safeBrowsingEnabled = false
    webView.addJavascriptInterface(MyJavaScriptInterface(dialog, callback), "Android")

    webView.loadUrl(domainBaseUrl() + API_REQUEST_PAGE)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            view.callJavaScriptFunction("postRequest", webUrl, json)
        }
    }
}

fun Context.showWebViewInterceptingDialog(webUrl: String, callback: ((Dialog?, String) -> Unit)) {
    if (webUrl.isEmpty()) return
    val dialog = Dialog(this, R.style.TransparentDialogTheme)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_webview_loading)

    val webView = dialog.findViewById<WebView>(R.id.webView)
    dialog.show()

    webView.webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            request.grant(request.resources)
        }
    }
    webView.settings.javaScriptEnabled = true
    webView.settings.safeBrowsingEnabled = false
    webView.loadUrl(webUrl)
    webView.webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return getWebResourceResponse(request.url.toString())
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            view.evaluateJavascript("document.documentElement.outerHTML") {
                val html = it.replace("\\u003C", "<")
                val renderContent = TextUtil.parseHtml(html)
                try {
                    val strJson = JsonParser.parseString(renderContent).asString
                    callback(dialog, strJson)
                } catch (e: Exception) {
                    callback(dialog, renderContent)
                }
                /*val doc = Jsoup.connect(url).get()
                val result = doc.body().text()*/
                dialog.dismiss()
            }
        }
    }
}

private fun getWebResourceResponse(url: String, json: String = "{}"): WebResourceResponse? {
    return try {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), json)
        val request: Request = Request.Builder()
            .url(url.trim())
            //.addHeader("Authorization", "YOU_AUTH_KEY")
            //.addHeader("api-key", "YOUR_API_KEY")
            //.addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build()
        val response: Response = OkHttpClient().newCall(request).execute()
        WebResourceResponse(getMimeType(url), "UTF-8", response.body().byteStream())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getMimeType(url: String): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        if (extension == "js") {
            return "text/javascript"
        } else if (extension == "woff") {
            return "application/font-woff"
        } else if (extension == "woff2") {
            return "application/font-woff2"
        } else if (extension == "ttf") {
            return "application/x-font-ttf"
        } else if (extension == "eot") {
            return "application/vnd.ms-fontobject"
        } else if (extension == "svg") {
            return "image/svg+xml"
        }
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

/**
 * Example -> https://stackoverflow.com/questions/7488872/page-content-is-loaded-with-javascript-and-jsoup-doesnt-see-it
 * */
fun readHtmlFromServer(url: String) {
    val webClient2 = WebClient()
    val myPage: HtmlPage = webClient2.getPage(URL(url))
    val doc = Jsoup.parse(myPage.asXml())
    for (row in doc.select("table#data > tbody > tr")) for (col in row.select("td"))
        System.out.println(col.ownText())

    webClient2.close()
}

fun postHtmlFromServer(url: String): Document {
    //JSoup load Login Page and get Session Details
    val res: Connection.Response = Jsoup.connect(url).method(Connection.Method.GET).execute()

    //val sessionId: String = res.cookie("findSESSION")
    val csrf: String = res.cookie("findCSRF")

    val cookie = HttpCookie("findCSRF", csrf)
    cookie.domain = "domain.url"
    cookie.path = "/path"

    val webClient = WebClient()
    webClient.addCookie(cookie.toString(), URL("https://url"), "https://referrer")

    // Add other cookies/ Session ...
    webClient.options.isJavaScriptEnabled = true
    webClient.options.isCssEnabled = false
    webClient.options.isThrowExceptionOnFailingStatusCode = false
    webClient.cookieManager.isCookiesEnabled = true
    webClient.ajaxController = NicelyResynchronizingAjaxController()
    if (url.startsWith("https://")) {
        webClient.options.isUseInsecureSSL = true
    }

    // Wait time
    webClient.waitForBackgroundJavaScript(15000)
    webClient.options.isThrowExceptionOnScriptError = false

    val requestSettings = WebRequest(URL(url), HttpMethod.POST)

    requestSettings.requestBody = "user=234&pass=sdsdc&CSRFToken=$csrf"
    val page: HtmlPage = webClient.getPage(requestSettings)

    // Wait
    synchronized(page) {
        try {
            Thread.sleep(15000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // Parse logged in page as needed
    return Jsoup.parse(page.asXml())
}