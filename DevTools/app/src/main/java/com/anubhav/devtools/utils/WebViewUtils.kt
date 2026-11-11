package com.anubhav.devtools.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun WebView.setDesktopMode(enabled: Boolean) {
    var newUserAgent: String? = settings.userAgentString
    if (enabled) {
        try {
            val ua: String = settings.userAgentString
            val androidOSString: String = settings.userAgentString.substring(
                ua.indexOf("("), ua.indexOf(")") + 1)
            newUserAgent = settings.userAgentString.replace(androidOSString, "(X11; Linux x86_64)")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        newUserAgent = null
    }
    settings.apply {
        useWideViewPort = enabled
        loadWithOverviewMode = enabled
        userAgentString = newUserAgent
    }
    tag = enabled // val enable = (binding.webView.tag as? Boolean)?.not() ?: true
}

/**
 * Function to capture a specific HTML element by its class name using JavaScript.
 */
fun WebView.captureElementByClass(className: String) {
    // JavaScript to get the bounding rectangle of the first element by class name
    val js = """
        (function() {
            var element = document.getElementsByClassName('$className')[0];
            if (element) {
                var rect = element.getBoundingClientRect();
                return rect.top + ',' + rect.left + ',' + rect.width + ',' + rect.height + ',' + window.devicePixelRatio;
            } else {
                return null;
            }
        })();
    """.trimIndent()

    evaluateJavascript(js) { result ->
        if (result != "null") {
            val rectData = result.replace("\"", "").split(",")
            val top = rectData[0].toFloat() * rectData[4].toFloat() // Adjusted top
            val left = rectData[1].toFloat() * rectData[4].toFloat() // Adjusted left
            val width = rectData[2].toFloat() * rectData[4].toFloat() // Adjusted width
            val height = rectData[3].toFloat() * rectData[4].toFloat() // Adjusted height

            // Check if the element is scrollable
            if (height > this.height) {
                GlobalScope.launch(Dispatchers.Main) {
                    val bitmap = captureScrollableElement(left.toInt(), top.toInt(), width.toInt(), height.toInt())
                    bitmap.saveAsFile(context)
                }
            } else {
                val bitmap = captureVisibleContent(left.toInt(), top.toInt(), width.toInt(), height.toInt())
                bitmap.saveAsFile(context)
            }
        } else {
            Toast.makeText(context, "Element not found", Toast.LENGTH_SHORT).show()
        }
    }
}


/**
 * Function to capture a scrollable part of the WebView based on the coordinates and stitch images together.
 */
private suspend fun WebView.captureScrollableElement(x: Int, y: Int, width: Int, height: Int): Bitmap {
    val viewportHeight = this.height // Height of the visible part (WebView height)
    val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val finalCanvas = Canvas(finalBitmap)

    var currentYOffset = 0

    // Scroll and capture each visible part of the element
    while (currentYOffset < height) {
        val captureHeight = Math.min(viewportHeight, height - currentYOffset)
        val capturedBitmap = captureVisibleContent(x, y + currentYOffset, width, captureHeight)

        finalCanvas.drawBitmap(capturedBitmap, 0f, currentYOffset.toFloat(), null)
        currentYOffset += captureHeight

        if (currentYOffset < height) {
            withContext(Dispatchers.Main) {
                scrollBy(0, captureHeight)
            }
        }
    }
    return finalBitmap
}

/**
 * Function to capture the visible part of the WebView based on coordinates.
 */
private fun WebView.captureVisibleContent(x: Int, y: Int, width: Int, height: Int): Bitmap {
    // Create a blank bitmap and draw the current WebView content into it
    val webViewBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(webViewBitmap)
    this.draw(canvas)

    // Clamp the x, y, width, and height to avoid out-of-bound issues
    val clampedX = Math.max(0, x)
    val clampedY = Math.max(0, y)
    val clampedWidth = Math.min(width, webViewBitmap.width - clampedX)
    val clampedHeight = Math.min(height, webViewBitmap.height - clampedY)

    // Ensure valid dimensions before cropping
    if (clampedWidth > 0 && clampedHeight > 0 && clampedX < webViewBitmap.width && clampedY < webViewBitmap.height) {
        return Bitmap.createBitmap(webViewBitmap, clampedX, clampedY, clampedWidth, clampedHeight)
    } else {
        // Invalid crop dimensions: x=0, y=1847, width=1080, height=-321
        Log.e("WebViewCapture", "Invalid crop dimensions: x=$clampedX, y=$clampedY, width=$clampedWidth, height=$clampedHeight")
        //return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        throw IllegalArgumentException("Invalid crop dimensions: x=$clampedX, y=$clampedY, width=$clampedWidth, height=$clampedHeight")
    }
}