package com.android.interviewdemo.model.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import com.android.interviewdemo.model.models.ContactItem

class ContentDataSource(private val contentResolver: ContentResolver) {

    fun fetchContacts(): List<ContactItem> {
        val result = mutableListOf<ContactItem>()
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        cursor?.let {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                result.add(ContactItem(cursor.getString(0), cursor.getString(1).toContactImageUri()))
                cursor.moveToNext()
            }
            cursor.close()
        }
        result.sortWith { a, b -> a.name.compareTo(b.name) }
        return result.toList()
    }

}

fun String.toContactImageUri() = Uri.withAppendedPath(
    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, this.toLong()),
    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
).toString()