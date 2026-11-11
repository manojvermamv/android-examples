package com.anubhav.codereverse.uiapp.modules

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.anubhav.codereverse.uiapp.utils.Log
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.engine.CropFileEngine
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.SandboxTransformUtils
import com.yalantis.ucrop.UCrop
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class SandboxFileEngine : UriToFileTransformEngine {
    override fun onUriToFileAsyncTransform(context: Context, srcPath: String, mineType: String, call: OnKeyValueResultCallbackListener?) {
        if (call != null) {
            val sandboxPath = SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
            call.onCallback(srcPath, sandboxPath)
        }
    }
}

class ImageCropEngine : CropFileEngine {

    override fun onStartCrop(fragment: Fragment, srcUri: Uri?, destinationUri: Uri?, dataSource: ArrayList<String>, requestCode: Int) {
        if (srcUri == null || destinationUri == null) return
        UCrop.of(srcUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .start(fragment.requireActivity(), fragment, requestCode)
    }

}

class ImageFileCompressEngine : CompressFileEngine {

    override fun onStartCompress(context: Context, source: ArrayList<Uri>, call: OnKeyValueResultCallbackListener) {
        Luban.with(context).load(source).ignoreBy(100)
            .setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                DateUtils.getCreateFileName("CMP_").toString() + postfix
            }.setCompressListener(object : OnCompressListener {

                override fun onStart() {}

                override fun onSuccess(source: String, compressFile: File) {
                    val file = compressFile.absoluteFile
                    val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                    } else {
                        Uri.fromFile(file)
                    }
                    call.onCallback(source, uri.toString())
                }

                override fun onError(source: String, e: Throwable) {
                    call.onCallback(source, null)
                    Log.getErrors(this, e.toString())
                }

            }).launch()
    }

}


fun Context.initPictureSelector(selectionMode: Int = 1, enableCompress: Boolean = false, listener: ((Uri) -> Unit)) {
    val pictureSelector = PictureSelector.create(this)
        .openGallery(SelectMimeType.ofImage())
        .setImageEngine(GlideEngine.createGlideEngine())
        .setSelectionMode(selectionMode)
        .setSandboxFileEngine(SandboxFileEngine()) // for android 10 or above
        //.setCropEngine(ImageCropEngine())

    if (enableCompress) {
        //pictureSelector.setCompressEngine(ImageFileCompressEngine())
    }
    pictureSelector.forResult(object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>) {
            val finalUri = result[0].path.toUri()
            if (enableCompress) {
                startCompressImage(finalUri, listener)
            } else {
                listener.invoke(finalUri)
            }
            return
        }

        override fun onCancel() { }
    })
}


fun Context.startCompressImage(source: Uri, listener: ((Uri) -> Unit)) {
    Luban.with(this).load(source).ignoreBy(100)
        .setRenameListener { filePath ->
            val indexOf = filePath.lastIndexOf(".")
            val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
            DateUtils.getCreateFileName("CMP_").toString() + postfix
        }.setCompressListener(object : OnCompressListener {

            override fun onStart() {}

            override fun onSuccess(source: String, compressFile: File) {
                val file = compressFile.absoluteFile
                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val context = this@startCompressImage
                    FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                } else {
                    Uri.fromFile(file)
                }
                listener.invoke(uri)
            }

            override fun onError(source: String, e: Throwable) {
                e.printStackTrace()
                Log.getErrors(this, e.toString())
            }

        }).launch()
}