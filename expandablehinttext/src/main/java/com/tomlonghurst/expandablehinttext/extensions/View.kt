package com.tomlonghurst.expandablehinttext.extensions

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.coroutines.*

internal suspend fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

internal suspend fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

internal suspend fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

internal suspend fun View.beInvisible() = withContext(Dispatchers.Main) {
    visibility = View.INVISIBLE
}

internal suspend fun View.beVisible() = withContext(Dispatchers.Main) {
    visibility = View.VISIBLE
}

internal suspend fun View.beGone() = withContext(Dispatchers.Main) {
    visibility = View.GONE
}

internal fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback.invoke()
        }
    })
}

internal fun View.postOnMainThread(action: suspend CoroutineScope.() -> Unit) {
    post {
        GlobalScope.launch(Dispatchers.Main) {
            action.invoke(this)
        }
    }
}

internal suspend fun View.remove() = withContext(Dispatchers.Main) {
    (parent as ViewGroup).removeView(this@remove)
}

internal fun View.isVisible() = visibility == View.VISIBLE

internal fun View.isInvisible() = visibility == View.INVISIBLE

internal fun View.isGone() = visibility == View.GONE
