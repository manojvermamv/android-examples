package com.anubhav.devtools.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileUtils {
}

fun generateFileName(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date())
    return "Web_Capture_$formattedDateTime.png"
}

fun Bitmap.saveAsFile(context: Context) {
    val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DevTools")
    val file = File(folder, generateFileName())
    try {
        if (!folder.exists()) {
            folder.mkdirs()
        }
        FileOutputStream(file).use { fileOutputStream ->
            compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            Toast.makeText(context, "Screenshot saved: $file", Toast.LENGTH_LONG).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
    }
}