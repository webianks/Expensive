package com.webianks.expensive.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.webianks.expensive.R
import kotlin.math.ceil


fun hideKeyboard(view: View) {
    try {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (ignored: Exception) {
    }
}

fun getSkeletonRowCount(context: Context): Int {
    val pxHeight = getDeviceHeight(context)
    val skeletonRowHeight = context.resources.getDimension(R.dimen.height_skeleton_expense_layout)
    return ceil(pxHeight / skeletonRowHeight.toDouble()).toInt()
}

fun getDeviceHeight(context: Context): Int {
    val resources: Resources = context.resources
    val metrics: DisplayMetrics = resources.displayMetrics
    return metrics.heightPixels
}