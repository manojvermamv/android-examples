package com.anubhav.codereverse.uiapp.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.VectorDrawable
import android.text.*
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.anubhav.commonutility.views.SingleClickListener
import com.anubhav.customprogresshud.CustomProgressHUD
import com.anubhav.codereverse.R
import com.anubhav.commonutility.HelperMethod
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*

/**
 *  with Context
 * */
fun Context.getCustomProgress(): CustomProgressHUD {
    val color = ContextCompat.getColor(this, R.color.overlay_light_80)
    return CustomProgressHUD.getInstance(this)
        .setBackgroundColor(color)
        .setImageTint(Color.BLACK)
        .setDimAmount(0.2f)
        .setCancellable(false)
}

fun Context.getCustomProgressText(): CustomProgressHUD {
    val color = ContextCompat.getColor(this, R.color.overlay_light_80)
    return CustomProgressHUD.getInstance(this)
        .setBackgroundColor(color)
        .setImageTint(Color.BLACK)
        .setDimAmount(0.2f)
        .setCancellable(false)
}

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    text?.let { Toast.makeText(this, it, duration).show() }
}

fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_LONG): Snackbar {
    return Snackbar.make(this, message, duration)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.colorAccent))
        .setTextColor(ContextCompat.getColor(context, android.R.color.white))
        .setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
}

/**
 *  with Activity
 * */
fun Activity.showKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Activity.hideKeyboard() {
    var view = this.currentFocus
    if (view == null) {
        view = findViewById(android.R.id.content)
    }
    val inputManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view!!.windowToken, 0)
}

fun Activity.setViewHeight(view: View) {
    val params = view.layoutParams
    params.height = HelperMethod.getStatusBarHeight(this)
    view.layoutParams = params
}

fun Context.showConfirmDialog(@StringRes title: Int, @StringRes msg: Int, onConfirmClick: (() -> Unit)? = null) {
    showConfirmDialog(getString(title), getString(msg), onConfirmClick)
}

fun Context.showConfirmDialog(title: String, msg: String, onConfirmClick: (() -> Unit)? = null) {
    val dialog = Dialog(this, R.style.TransparentDialogTheme)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.dialog_confirm)

    val textTitle = dialog.findViewById<View>(R.id.dialog_title) as TextView
    textTitle.text = title

    val textDesc = dialog.findViewById<View>(R.id.dialog_desc) as TextView
    textDesc.text = msg

    val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
    btnConfirm.setOnClickListener {
        dialog.dismiss()
        onConfirmClick?.invoke()
    }

    val imgClose = dialog.findViewById<View>(R.id.img_close) as ImageView
    imgClose.setOnClickListener { dialog.dismiss() }
    dialog.show()
}

fun Context.showRateUsDialog(onConfirmClick: ((Int) -> Unit)) {
//    var selectedRating = 3f
//
//    val dialog = Dialog(this, R.style.TransparentDialogTheme)
//    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//    dialog.setCancelable(true)
//    dialog.setContentView(R.layout.dialog_rateus)
//
//    val textTitle = dialog.findViewById<View>(R.id.dialog_title) as TextView
//    textTitle.setText(R.string.rate_us_title)
//
//    val mRatingBar = dialog.findViewById(R.id.ratingBar) as RatingBarSvg
//    mRatingBar.rating = selectedRating
//    mRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
//        selectedRating = rating
//    }
//
//    val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
//    btnConfirm.setOnClickListener {
//        dialog.dismiss()
//        onConfirmClick.invoke(selectedRating.toInt())
//    }
//
//    val imgClose = dialog.findViewById<View>(R.id.img_close) as ImageView
//    imgClose.setOnClickListener { dialog.dismiss() }
//    dialog.show()
}

fun Context.showConfirmPopup(@StringRes title: Int, @StringRes msg: Int, onConfirmClick: (() -> Unit)? = null) {
    showConfirmPopup(getString(title), getString(msg), onConfirmClick)
}

fun Context.showConfirmPopup(title: String, msg: String, onConfirmClick: (() -> Unit)? = null) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.dialog_confirm, null, false)
    val pwDialog = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

    val textTitle = view.findViewById<View>(R.id.dialog_title) as TextView
    textTitle.text = title

    val textDesc = view.findViewById<View>(R.id.dialog_desc) as TextView
    textDesc.text = msg

    val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
    btnConfirm.setOnClickListener {
        pwDialog.dismiss()
        onConfirmClick?.invoke()
    }

    val imgClose = view.findViewById<View>(R.id.img_close) as ImageView
    imgClose.setOnClickListener { pwDialog.dismiss() }

    val rootView = ((this as Activity).window.decorView.findViewById(android.R.id.content) as View)
    pwDialog.showAtLocation(rootView, Gravity.CENTER, 0, 0);
}

/**
 *  with ImageView
 * */
fun ImageView.loadImage(storageReference: StorageReference, @DrawableRes fallback: Int = R.drawable.img_load_placeholder) =
    ifNotDestroyed {
        Glide.with(this).load(storageReference).placeholder(fallback).fallback(fallback).into(this)
    }

fun ImageView.loadImage(image: String?, @DrawableRes fallback: Int = 0) =
    ifNotDestroyed {
        if (fallback == 0) {
            Glide.with(this).load(image).centerCrop().into(this)
        } else {
            Glide.with(this).load(image).centerCrop().fallback(fallback).placeholder(fallback)
                .into(this)
        }
    }

fun ImageView.loadImageOrHide(image: String?) =
    if (image != null) {
        visibility = View.VISIBLE
        loadImage(image)
    } else {
        visibility = View.GONE
    }

fun ImageView.loadImageAndBlur(imageUrl: String) {
    ifNotDestroyed {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.bg_blur)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    this@loadImageAndBlur.setImageDrawable(placeholder)
                }

                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
                    val mBitmap = HelperMethod.fastBlur(bitmap, 0.2f, 30)
                    this@loadImageAndBlur.setImageBitmap(mBitmap)
                }
            })
    }
}

fun ImageView.setImageDrawableWithAnimation(drawable: Drawable, duration: Int = 300) {
    val currentDrawable = getDrawable()
    if (currentDrawable == null) {
        setImageDrawable(drawable)
        return
    }

    val transitionDrawable = TransitionDrawable(arrayOf(currentDrawable, drawable))
    setImageDrawable(transitionDrawable)
    transitionDrawable.startTransition(duration)
}

fun ImageView.setTint(@ColorRes resId: Int) {
    val color = ContextCompat.getColor(context, resId)
    if (drawable is VectorDrawable) {
        setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    } else {
        setColorFilter(color)
    }
}

fun View.showSnackBar(text: String) {
    ifNotDestroyed {
        Snackbar.make(this.rootView, text, Snackbar.LENGTH_SHORT).show()
    }
}

fun View.setOnSingleClickListener(block: (View) -> Unit) {
    setOnClickListener(object : SingleClickListener() {
        override fun onClickView(v: View) {
            block(v)
        }
    })
}

fun ViewGroup.toogleTouchable(canTouch: Boolean) {
    descendantFocusability =
        if (canTouch) {
            requestFocus()
            ViewGroup.FOCUS_BLOCK_DESCENDANTS
        } else {
            ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
}

@SuppressLint("UnsafeOptInUsageError")
fun Context.getOrCreateBadge(count: Int, anchor: View, parent: FrameLayout? = null, block: (BadgeDrawable) -> Unit) {
    anchor.afterMeasured {
        val badgeDrawable = BadgeDrawable.create(this@getOrCreateBadge)
        badgeDrawable.badgeGravity = BadgeDrawable.TOP_END
        badgeDrawable.maxCharacterCount = 3
        badgeDrawable.badgeTextColor = Color.WHITE
        badgeDrawable.backgroundColor = Color.RED
        badgeDrawable.verticalOffset = 15
        badgeDrawable.horizontalOffset = 25
        badgeDrawable.number = count
        badgeDrawable.isVisible = (count > 0)
        if (parent != null) {
            BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, parent)
        } else {
            BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor)
        }
        block(badgeDrawable)
    }
}

fun View.delayOnLifecycle(durationInMillis: Long, block: () -> Unit): Job? {
    return findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
        val dispatcher: CoroutineDispatcher = Dispatchers.Main
        lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
            delay(durationInMillis)
            block()
        }
    }
}

// define 'afterMeasured' layout listener:
inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

/**
 * with TextView
 * */
fun TextView.setDrawableStart(context: Context, @DrawableRes resId: Int) {
    var drawable: Drawable? = null
    if (resId != 0) {
        drawable = ContextCompat.getDrawable(context, resId)
    }
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setDrawableEnd(context: Context, @DrawableRes resId: Int) {
    var drawable: Drawable? = null
    if (resId != 0) {
        drawable = ContextCompat.getDrawable(context, resId)
    }
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

/**
 * with EditText
 * */
fun coordinateBtnAndInputs(btn: Button, vararg inputs: EditText): TextWatcher {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.trim().isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.trim().isNotEmpty() }
    return watcher
}

fun coordinateBtnAndInputs(btn: ImageButton, vararg inputs: EditText): TextWatcher {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.trim().isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.trim().isNotEmpty() }
    return watcher
}

@SuppressLint("ClickableViewAccessibility")
fun EditText.setOnRightDrawableClickListener(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

fun EditText.multilineIme(action: Int, maxLine: Int = Integer.MAX_VALUE) {
    imeOptions = action
    inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
    setHorizontallyScrolling(false)
    maxLines = maxLine
}

fun EditText.multilineDone(callback: (() -> Unit)? = null) {
    val action = EditorInfo.IME_ACTION_DONE
    multilineIme(action)
    setOnEditorActionListener { _, actionId, _ ->
        if (action == actionId) {
            callback?.invoke()
            true
        } else {
            false
        }
    }
}

fun EditText.onDone(callback: (() -> Unit)? = null) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    maxLines = 1
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback?.invoke()
            true
        } else {
            false
        }
    }
}

fun EditText.setTextCounter(textView: TextView, maxCounts: Int = 1000) {
    textView.text = String.format("%2d/%2d", this.text.length, maxCounts)
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textView.text = String.format("%2d/%2d", s?.length, maxCounts)
        }
    }
    addTextChangedListener(watcher)
}

fun Editable.toStringOrNull(): String? {
    val str = toString()
    return if (str.isEmpty()) null else str
}

/**
 *  with private
 * */
private fun View.ifNotDestroyed(block: () -> Unit) {
    if (context is Activity) {
        if (!(context as Activity).isDestroyed) block()
    }
}