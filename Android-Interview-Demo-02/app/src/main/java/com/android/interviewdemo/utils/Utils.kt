package com.android.interviewdemo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Window
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal object Utils {
}

fun Activity.registerOnBackPressedDispatcher(enabled: Boolean = true, onBackPressed: () -> Unit) {
    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
            onBackPressed()
        }
    } else {
        (this as ComponentActivity).onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }
}

fun ActivityResultLauncher<Intent>.launchFileChooser(
    type: String,
    title: String = "Choose file",
    multipleAllowed: Boolean = false
) {
    // show file chooser
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = type
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multipleAllowed)
    launch(Intent.createChooser(intent, title))
}

/**
 * Register activities result
 * */

fun <I, O> Activity.registerForActivityResult(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
) = (this as ComponentActivity).registerForActivityResult(contract, callback)


fun Fragment.registerForActivityResult(callback: ((Int, Intent?) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode, result.data)
    }
}

fun AppCompatActivity.registerForActivityResult(callback: ((Int, Intent?) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode, result.data)
    }
}

fun Fragment.registerForActivityResultOnSuccess(callback: ((Intent) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result?.data != null) {
            callback.invoke(result.data!!)
        }
    }
}

fun AppCompatActivity.registerForActivityResultOnSuccess(callback: ((Intent) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result?.data != null) {
            callback.invoke(result.data!!)
        }
    }
}

/**
 * Register permissions result
 * */
fun Fragment.registerForPermissionResult(callback: ((Boolean) -> Unit)): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isGranted?.let { callback.invoke(it) }
    }
}

fun AppCompatActivity.registerForPermissionResult(callback: ((Boolean) -> Unit)): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isGranted?.let { callback.invoke(it) }
    }
}


/**
 * Changes the foreground color of the status bars to light or dark so that the items on the bar
 * can be read clearly.
 *
 * @param window Window that hosts the status bars
 * @param isLight `true` to make the foreground color light
 */
fun setLightStatusBar(window: Window, isLight: Boolean) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = isLight
}

/**
 * Changes the foreground color of the navigation bars to light or dark so that the items on the
 * bar can be read clearly.
 *
 * @param window Window that hosts the status bars
 * @param isLight `true` to make the foreground color light.
 */
fun setLightNavigationBar(window: Window, isLight: Boolean) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightNavigationBars = isLight
}



/**
 * Helper extensions
 * */

fun Activity.requestPermission(permission: String, resultLauncher: ActivityResultLauncher<String>, onPermissionGranted: () -> Unit = {}) {
    when {
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> { onPermissionGranted() }
        shouldShowRequestPermissionRationale(permission) -> {
            // permission denied permanently - showPermissionRationaleDialog()
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("This app requires certain permissions to function properly. Please grant all permissions.")
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Yes") { _, _ ->
                    // Request the permission after the user accepts the rationale
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        resultLauncher.launch(permission)
                    } else {
                        goToSettings()
                    }
                }.show()
        }
        else -> resultLauncher.launch(permission)
    }
}

fun Context.goToSettings() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}