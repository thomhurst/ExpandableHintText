package com.tomlonghurst.expandablehinttext

import android.content.Context

internal object ViewHelper {
    internal fun getDp(context: Context, int: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (int * scale + 0.5f).toInt()
    }
}