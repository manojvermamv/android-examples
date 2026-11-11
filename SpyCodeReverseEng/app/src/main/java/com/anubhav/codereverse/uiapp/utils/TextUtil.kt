package com.anubhav.codereverse.uiapp.utils

import android.os.Build
import android.text.Html
import android.text.format.DateUtils
import org.apache.commons.text.CaseUtils
import java.util.*

internal object TextUtil {

    @JvmStatic
    fun removeFirstChar(str: String): String {
        if (str.isEmpty()) return ""
        return str.substring(1, str.length)
    }

    @JvmStatic
    fun removeLastChar(str: String): String {
        if (str.isEmpty()) return ""
        return str.substring(0, str.length - 1)
    }

    @JvmStatic
    fun removeFirstAndLastChar(str: String): String {
        val arrOfStr = str.split(" ".toRegex()).toTypedArray()
        var result = ""
        for (s in arrOfStr) {
            result += s.substring(1, s.length - 1) + " "
        }
        return result
    }

    @JvmStatic
    fun String.toCamelCase() = split(" ")
        .joinToString("") {
            it.lowercase().replaceFirstChar { char -> char.uppercase() }
        }.replaceFirstChar { char -> char.lowercase() }

    @JvmStatic
    fun toLowerCamelCase(s: String): String {
        return CaseUtils.toCamelCase(s, false, ' ')
    }

    @JvmStatic
    fun toUpperCamelCase(s: String): String {
        return CaseUtils.toCamelCase(s, true, ' ')
    }

    @JvmStatic
    fun getRoomID(userID1: String, userID2: String): String {
        return convertTwoUserIDs(userID1, userID2)
    }

    @JvmStatic
    fun convertUserID(combinedID: String, myUserID: String): String {
        return if (combinedID.contains(myUserID)) {
            combinedID.replace(myUserID, "")
        } else {
            combinedID
        }
    }

    // Always returns the same combined id when comparing the two users id's
    @JvmStatic
    fun convertTwoUserIDs(userID1: String, userID2: String): String {
        return if (userID1 < userID2) {
            userID2 + userID1
        } else {
            userID1 + userID2
        }
    }

    @JvmStatic
    fun parseHtml(html: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(html).toString().trim()
        }
    }

}


fun formatRelativeTimestamp(start: Date, end: Date): CharSequence =
    DateUtils.getRelativeTimeSpanString(start.time, end.time, DateUtils.SECOND_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE).replace(Regex("\\. ago$"), "")

fun isEmailValid(email: CharSequence): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isTextValid(text: String?, minLength: Int = 1): Boolean {
    if (text.isNullOrBlank() || text.length < minLength) {
        return false
    }
    return true
}