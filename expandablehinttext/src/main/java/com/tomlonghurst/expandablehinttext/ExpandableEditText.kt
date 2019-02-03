package com.tomlonghurst.expandablehinttext

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.EditText

/**
 * Edit Text for Expandable Hint Text
 */
class ExpandableEditText : EditText {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private var onBackPressed: Runnable? = null

    /**
     * Custom event for clicking back when on this view is active
     * @sample ExpandableEditText.setOnBackPressListener { Runnable { println("Back Pressed") } }
     */
    fun setOnBackPressListener(onBackPressed: Runnable?) {
        this.onBackPressed = onBackPressed
    }

    private var clickable: Boolean = true

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        this.clickable = clickable
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (clickable) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            this.clearFocus()
            onBackPressed?.run()
        }
        return super.onKeyPreIme(keyCode, event)
    }
}