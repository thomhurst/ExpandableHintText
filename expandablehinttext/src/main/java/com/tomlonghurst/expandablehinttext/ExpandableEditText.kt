package com.tomlonghurst.expandablehinttext

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

class ExpandableEditText : EditText {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private var onBackPressed: Runnable? = null

    fun setOnBackPressListener(onBackPressed: Runnable?) {
        this.onBackPressed = onBackPressed
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            this.clearFocus()
            onBackPressed?.run()
        }
        return super.onKeyPreIme(keyCode, event)
    }
}