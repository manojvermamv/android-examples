package com.anubhav.devtools.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.anubhav.devtools.R


fun Context.setPopupMenuList(items: List<String>, anchor: View? = null, @DrawableRes background: Int = R.drawable.back_cardview_10, @LayoutRes layout: Int = android.R.layout.simple_spinner_dropdown_item,
                             verticalOffset: Int = 0, horizontalOffset: Int = 0, callback: ((String) -> Unit)): ListPopupWindow {
    var vOffset = verticalOffset
    if (vOffset == 0) {
        vOffset = anchor?.y?.toInt() ?: dpToPx(5f).toInt()
    }

    var hOffset = horizontalOffset
    if (hOffset == 0) {
        val width = anchor?.width ?: 0
        val x: Int = anchor?.x?.toInt() ?: 0                                    // Get the X coordinate of the anchor view.
        val centerX = Resources.getSystem().displayMetrics.widthPixels / 2      // Calculate the center of the screen.
        hOffset = if (x < centerX) {
            (width - x)             // anchor view is aligned to the start of the X axis.
        } else if (x > centerX) {
            (width - (x/2))         // anchor view is aligned to the end of the X axis.
        } else {
            0                       // anchor view is centered on the X axis.
        }
    }

    val popupWindow = ListPopupWindow(this)
    val adapter = ArrayAdapter(this, layout, items)
    popupWindow.setAdapter(adapter)
    popupWindow.horizontalOffset = hOffset
    popupWindow.verticalOffset = vOffset
    popupWindow.anchorView = anchor // Set the view below/above which ListPopupWindow dropdowns
    popupWindow.isModal = true      // Setting this enables window to be dismissed by click outside ListPopupWindow
    popupWindow.width = measureContentWidth(adapter)
    popupWindow.height = ListPopupWindow.WRAP_CONTENT
    if (background != 0) {
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, background))
    }
    popupWindow.setOnItemClickListener { adapterView, _, position, _ ->
        popupWindow.dismiss()
        callback(adapterView.getItemAtPosition(position) as String)
    }
    return popupWindow
}

fun Context.measureContentWidth(adapter: Adapter): Int {
    val measureParentViewGroup = FrameLayout(this)
    var itemView: View? = null

    var maxWidth = 0
    var itemType = 0

    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    for (index in 0 until adapter.count) {
        val positionType = adapter.getItemViewType(index)
        if (positionType != itemType) {
            itemType = positionType
            itemView = null
        }
        itemView = adapter.getView(index, itemView, measureParentViewGroup)
        itemView.measure(widthMeasureSpec, heightMeasureSpec)
        val itemWidth = itemView.measuredWidth
        if (itemWidth > maxWidth) {
            maxWidth = itemWidth
        }
    }
    return maxWidth
}

fun dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
}