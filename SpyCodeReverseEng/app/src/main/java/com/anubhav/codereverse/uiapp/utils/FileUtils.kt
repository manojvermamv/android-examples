package com.anubhav.codereverse.uiapp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.storage.StorageReference
import java.io.*
import java.net.URL
import java.util.*

internal object FileUtils {

    private fun sdk29AndUp(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    private fun getFileProperties(url: String): Pair<String, Uri> {
        return when {
            //Images
            url.contains(FileType.JPG.ext) || url.contains(FileType.JPEG.ext) ||
                    url.contains(FileType.PNG.ext) || url.contains(FileType.WEBP.ext) ||
                    url.contains(FileType.TIFF.ext) || url.contains(FileType.TIF.ext) ||
                    url.contains(FileType.GIF.ext) -> {
                val collection: Uri = if (sdk29AndUp()) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                Pair(FileType.JPG.mimeType, collection)
            }
            //Video
            url.contains(FileType.MP4.ext) || url.contains(FileType.AVI.ext) -> {
                val collection: Uri = if (sdk29AndUp()) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                Pair(FileType.MP4.mimeType, collection)
            }
            //Audio
            url.contains(FileType.MP3.ext) -> {
                val collection: Uri = if (sdk29AndUp()) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                Pair(FileType.MP3.mimeType, collection)
            }
            //Files
            url.isNotEmpty() -> {
                val collection: Uri = if (sdk29AndUp()) {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.lowercase(Locale.getDefault())) ?: ""
                Pair(mimeType, collection)
            }
            else -> Pair("", "".toUri())
        }
    }

    private fun writeToFileSystem(
        context: Context,
        fileWriteModel: FileWriteModel,
        callback: ((OutputStream, Uri) -> Unit)
    ) {
        val (url, fileName, collection, mime) = fileWriteModel
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
        }
        //Append DATA Column for SDK<29 required for Audio and Pdfs, also this prevents DISPLAY_NAME and TITLE overwrite
        if (!sdk29AndUp()) {
            when (mime) {
                FileType.MP3.mimeType -> {
                    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    contentValues.put(MediaStore.Audio.AudioColumns.DATA, "${directory}/${fileName}")
                }
                FileType.MP4.mimeType -> {
                    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    contentValues.put(MediaStore.Video.VideoColumns.DATA, "${directory}/${fileName}")
                }
                FileType.PDF.mimeType -> {
                    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    contentValues.put(MediaStore.Files.FileColumns.DATA, "${directory}/${fileName}")
                }
            }
        }

        val resolver: ContentResolver = context.contentResolver
        val uriResolve: Uri? = resolver.insert(collection, contentValues)

        try {
            if (uriResolve == null || uriResolve.path == null) {
                Toast.makeText(context, "Failed to create uri resolver!", Toast.LENGTH_SHORT).show()
                throw IOException("Failed to create new MediaStore record.")
            }
            val stream = resolver.openOutputStream(uriResolve) ?: throw IOException("Output stream null")
            callback.invoke(stream, uriResolve)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     *  save file from direct url or firebase file uri
     * */
    fun StorageReference.getFirebaseFileName(url: String, fileName: String): String {
        return when {
            url.contains("image") -> {
                "${child(url).name}.png"
            }
            url.contains("video") -> {
                "${child(url).name}.mp4"
            }
            url.contains("audio") -> {
                "${child(url).name}.mp3"
            }
            url.contains("file") -> {
                "${child(url).name}.${fileName.substringAfter(".")}"
            }
            else -> {
                fileName
            }
        }
    }

    fun Context.downloadFiles(url: String, fileName: String = "", callback: (Uri, Boolean) -> Unit) {
        var filename = fileName;
        if (filename.isEmpty()) {
            filename = Utils.getFileNameFromUrl(url);
        }
        val (mime, collection) = getFileProperties(url)
        val fileWriteModel = FileWriteModel(url, filename, collection, mime)
        writeToFileSystem(this, fileWriteModel) { outputStream, contentUri ->
            outputStream.saveFileFromUrl(url, contentUri, callback)
        }
    }

    fun Context.downloadFireBaseFiles(fileRef: StorageReference, fileName: String = "", callback: (Uri, Boolean) -> Unit) {
        var filename = fileName;
        if (filename.isEmpty()) {
            filename = Utils.getFileNameFromUrl(fileRef.path);
        }
        val (mime, collection) = getFileProperties(filename)
        val fileWriteModel = FileWriteModel(fileRef.path, filename, collection, mime)
        writeToFileSystem(this, fileWriteModel) { outputStream, contentUri ->
            outputStream.saveFileFromUrl(fileRef, contentUri, callback)
        }
    }

    fun Context.checkFileAlreadyExist(url: String): String {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val sharedStorageFileList = mutableListOf<SharedStorageFile>()
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT)

        contentResolver?.query(
            collection,
            projection,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                sharedStorageFileList.add(SharedStorageFile(id, displayName))
            }
        }
        try {
            val fileUrlArray = url.split("/")
            val storedFileName = fileUrlArray[fileUrlArray.size - 1]
            val matchingFiles = sharedStorageFileList.filter { it.name.contains(storedFileName) }
            if (matchingFiles.isNotEmpty()) return matchingFiles[0].name
        } catch (e: Exception) {
            Log.d(this, "No matching file found! " + e.message)
        }
        return ""
    }

    fun OutputStream.saveFileFromUrl(
        url: String,
        contentUri: Uri,
        callback: (Uri, Boolean) -> Unit
    ) {
        try {
            val bis = BufferedInputStream(URL(url).openStream())
            this.use { fos ->
                val data = ByteArray(1024)
                var count: Int
                while (bis.read(data, 0, 1024).also { count = it } != -1) {
                    fos.write(data, 0, count)
                }
                bis.close()
                fos.flush()
                fos.close()
                callback.invoke(contentUri, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(contentUri, false)
        }
    }

    fun OutputStream.saveFileFromUrl(
        fileRef: StorageReference,
        contentUri: Uri,
        callback: (Uri, Boolean) -> Unit
    ) {
        var inputStream: ByteArrayInputStream
        fileRef.getBytes((1024 * 1024).toLong())
            .addOnSuccessListener { bytes: ByteArray? ->
                try {
                    var bytesRead: Int
                    inputStream = ByteArrayInputStream(bytes)
                    while (inputStream.read(bytes).also { bytesRead = it } > 0) {
                        write(bytes, 0, bytesRead)
                    }
                    inputStream.close()
                    flush()
                    close()
                    callback.invoke(contentUri, true)
                } catch (e: IOException) {
                    close()
                    e.printStackTrace()
                    callback.invoke(contentUri, false)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                callback.invoke(contentUri, false)
            }
    }

}

data class SharedStorageFile(
    var id: Long,
    val name: String,
)

data class FileWriteModel(
    var url: String,
    val fileName: String,
    val collection: Uri,
    val mime: String
)

enum class FileType(val ext: String, val mimeType: String) {
    JPG("jpg", "image/*"),
    JPEG("jpeg", "image/*"),
    PNG("png", "image/*"),
    WEBP("webp", "image/*"),
    TIFF("tiff", "image/*"),
    TIF("tif", "image/*"),
    GIF("gif", "image/*"),
    MP4("mp4", "video/*"),
    AVI("avi", "video/*"),
    MP3("mp3", "audio/*"),
    PDF("pdf", "application/pdf"),
}