package com.tomlonghurst.expandablehinttext.extensions

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewTreeObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    GlobalScope.launch(Dispatchers.Main) {
        visibility = View.INVISIBLE
    }
}

fun View.beVisible() {
    GlobalScope.launch(Dispatchers.Main) {
        visibility = View.VISIBLE
    }
}

fun View.beGone() {
    GlobalScope.launch(Dispatchers.Main) {
        visibility = View.GONE
    }
}

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
