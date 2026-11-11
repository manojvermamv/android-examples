package com.android.interviewdemo.utils

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable.ShaderFactory
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.android.interviewdemo.R
import com.android.interviewdemo.model.models.Team
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine


fun ImageView.loadLogo(team: Team) {
    if (team.code == "GT") {
        Picasso.get().load(R.drawable.img_gujarat_titans).error(R.drawable.img_not_available).into(this)
    } else {
        Picasso.get().load(team.logoUrl).error(R.drawable.img_not_available).into(this)
    }
}

fun ImageView.loadImage(url: String? = null) {
    Picasso.get().load(url).error(R.drawable.img_not_available).into(this)
}

fun ImageView.loadImage(uri: Uri) {
    Picasso.get().load(uri).error(R.drawable.img_not_available).into(this)
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
Usage:
lifecycle.coroutineScope.launch {
    binding.tvAmount.text = "Dumb text"
    binding.tvAmount.awaitLayoutChange()
    binding.tvAmount.setGradientTextColor(R.color.yellow, R.color.green)
}
 * */
private fun toFloatArray(vararg colorRes: Int): ArrayList<Float> {
    val floatArray = ArrayList<Float>(colorRes.size)
    for (i in colorRes.indices) {
        floatArray.add(i, i.toFloat() / (colorRes.size - 1))
    }
    return floatArray
}

private fun View.getGradientOf45Angle(floatArray: ArrayList<Float>, vararg colorRes: Int): LinearGradient {
    return LinearGradient(
        0f,
        0f,
        this.height.toFloat(), // End X coordinate (same as height for 45 degrees)
        0f,
        colorRes.map { ContextCompat.getColor(context, it) }.toIntArray(),
        floatArray.toFloatArray(),
        Shader.TileMode.CLAMP
    )
}

suspend fun View.awaitLayoutChange() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            view?.removeOnLayoutChangeListener(this)
            cont.resumeWith(Result.success(Unit))
        }
    }
    addOnLayoutChangeListener(listener)
    cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
}

fun View.getGradientColor(vararg colorRes: Int): PaintDrawable {
    val roundCorners = FloatArray(8) { 12f } // similarCornerRadius
    val floatArray = toFloatArray(*colorRes)
    return PaintDrawable().apply {
        shape = RoundRectShape(roundCorners, null, null).apply {  }
        shaderFactory = object : ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                return getGradientOf45Angle(floatArray, *colorRes)
            }
        }
    }
}

fun View.setGradientBackground(vararg colorRes: Int) {
    this.background = getGradientColor(*colorRes)
}

fun TextView.setGradientTextColor(vararg colorRes: Int) {
    val floatArray = toFloatArray(*colorRes)
    this.paint.shader = getGradientOf45Angle(floatArray, *colorRes)
}


fun TextView.setGradientTextColor(lifecycle: Lifecycle, vararg colorRes: Int) {
    lifecycle.coroutineScope.launch {
        val floatArray = toFloatArray(*colorRes)
        paint.shader = getGradientOf45Angle(floatArray, *colorRes)
    }
}

fun TextView.setTextColorByRes(@ColorRes id: Int) {
    paint.shader = null
    setTextColor(ContextCompat.getColor(context, id))
}

fun View.setBackgroundTintByRes(@ColorRes id: Int) {
    val color = ResourcesCompat.getColor(context.resources, id, null)
    backgroundTintList = ColorStateList.valueOf(color)
}


fun ImageView.setGradient(vararg colorRes: Int) {
    val originalBitmap = (drawable as BitmapDrawable).bitmap ?: return

    val width = originalBitmap.width
    val height = originalBitmap.height
    val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(updatedBitmap)

    val paint = Paint().apply {
        shader = getGradientOf45Angle(toFloatArray(*colorRes), *colorRes)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    canvas.drawBitmap(originalBitmap, 0f, 0f, null)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    canvas.density = originalBitmap.density

    setImageDrawable(BitmapDrawable(resources, updatedBitmap))
}
