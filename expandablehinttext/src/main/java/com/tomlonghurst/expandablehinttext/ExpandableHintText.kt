package com.tomlonghurst.expandablehinttext

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.eht_layout.view.*


class ExpandableHintText : FrameLayout {
    private lateinit var inputMethodManager: InputMethodManager

    lateinit var editText: ExpandableEditText
        private set

    private var hintText: String = ""

    private var labelTranslationY = -1
    private var labelTranslationX = -1

    var isExpanded = false
        private set

    private var animationDuration = -1
    private var textColor = Int.MIN_VALUE
    private var textSize: Float = -1f
    private var imageDrawableId = -1
    private var cardCollapsedHeight = -1
    private var imageColour = Color.BLACK
    private var floatingLabelColor = Color.WHITE
    private var textBoxColor: Int = Color.WHITE
    private var presetText: String? = null
    private var inputType: Int = -1
    private var maxLines: Int = -1
    private val labelPadding by lazy {
        if (imageDrawableId == -1) {
            0
        } else {
            getDp(45)
        }
    }
    private var customIsEnabled: Boolean = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getAttributes(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttributes(context, attrs)
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.eht_layout, this)
        addEditText()
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun getDp(int: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (int * scale + 0.5f).toInt()
    }


    private fun toggle() {
        if (isEnabled) {
            if (isExpanded) {
                reduce()
            } else {
                expand()
            }
        }
    }

    fun reduce() {
        if (isExpanded) {

            if (editText.text.toString().isBlank()) {

                editText.apply {
                    post {
                        ViewCompat.animate(this)
                            .alpha(0f).duration = animationDuration.toLong()
                    }
                }

                label.apply {
                    post {
                        ViewCompat.animate(this)
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .translationX(0f).duration = animationDuration.toLong()

                        animateColours(this, this.currentTextColor, adjustAlpha(textColor, 0.7f))
                    }
                }

                isExpanded = false
            }
        }
    }

    private fun animateColours(view: TextView, startColour: Int, endColour: Int) {
        view.setTextColor(startColour)

        val property = object : Property<TextView, Int>(Int::class.javaPrimitiveType, "textColor") {
            override fun get(textView: TextView): Int {
                return textView.currentTextColor
            }

            override fun set(textView: TextView, value: Int?) {
                textView.setTextColor(value!!)
            }
        }

        val animator = ObjectAnimator.ofInt(view, property, endColour)
        animator.duration = animationDuration.toLong()
        animator.setEvaluator(ArgbEvaluator())
        animator.interpolator = DecelerateInterpolator(2f)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                view.setTextColor(endColour)
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    fun expand() {
        if (!isExpanded) {

            editText.post {
                ViewCompat.animate(editText)
                    .alpha(1f).duration = animationDuration.toLong()
            }

            val miniatureScale = 0.7f

            label.apply {
                post {
                    ViewCompat.animate(this)
                        .scaleX(miniatureScale)
                        .scaleY(miniatureScale)
                        .translationY((-labelTranslationY).toFloat())
                        .translationX(-labelTranslationX.plus(labelPadding).times(miniatureScale)).duration =
                            animationDuration.toLong()

                    animateColours(this, this.currentTextColor, floatingLabelColor)
                }
            }

            isExpanded = true
        }
    }

    private fun editEditText() {
        if (isEnabled && isExpanded) {
            editText.post {
                editText.requestFocus()
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun addEditText() {
        editText = ExpandableEditText(context).apply {
            setOnBackPressListener {
                editText.clearFocus()
                card?.clearFocus()
            }

            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus && editText.text.toString().isBlank()) {
                    reduce()
                }
                if (hasFocus) {
                    expand()
                }
            }

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.toString()?.isNotBlank() == true) {
                        expand()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })

            post {
                setPaddingRelative(getDp(10), paddingTop, paddingEnd, paddingBottom)
            }
        }
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        var styledAttrs: TypedArray? = null
        try {
            styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ExpandableHintText)

            animationDuration = styledAttrs.getInteger(R.styleable.ExpandableHintText_animationDuration, 400)
            textColor = styledAttrs.getColor(R.styleable.ExpandableHintText_android_textColor, Int.MIN_VALUE)
            floatingLabelColor = styledAttrs.getColor(R.styleable.ExpandableHintText_floatingLabelColor, Color.BLACK)
            imageDrawableId = styledAttrs.getResourceId(R.styleable.ExpandableHintText_image, -1)
            imageColour = styledAttrs.getColor(R.styleable.ExpandableHintText_imageColor, Color.GRAY)
            cardCollapsedHeight = styledAttrs.getDimensionPixelOffset(
                R.styleable.ExpandableHintText_cardCollapsedHeight,
                context.resources.getDimensionPixelOffset(R.dimen.cardHeight_initial)
            )
            customIsEnabled = styledAttrs.getBoolean(R.styleable.ExpandableHintText_android_enabled, true)
            hintText = styledAttrs.getString(R.styleable.ExpandableHintText_android_hint) ?: ""
            textBoxColor = styledAttrs.getColor(R.styleable.ExpandableHintText_textBoxColor, Color.WHITE)
            presetText = styledAttrs.getString(R.styleable.ExpandableHintText_android_text)
            inputType = styledAttrs.getInt(R.styleable.ExpandableHintText_android_inputType, Int.MIN_VALUE)
            maxLines = styledAttrs.getInt(R.styleable.ExpandableHintText_android_maxLines, -1)
            textSize = styledAttrs.getFloat(R.styleable.ExpandableHintText_android_textSize, 16f)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            styledAttrs?.recycle()
        }

    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        super.setOnClickListener(l)
        editText.setOnClickListener(l)
        card.setOnClickListener(l)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        card.addView(editText)

        label.pivotX = 0f
        label.pivotY = 0f

        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.alpha = 0f

        if (editText.text.toString().isNotBlank()) {
            expand()
        }

        label.post {
            labelTranslationY = (label.layoutParams as FrameLayout.LayoutParams).topMargin
            labelTranslationX = label.paddingStart
            customizeFromAttributes()
            label.bringToFront()
        }

        editText.post {
            editText.clearFocus()
            editText.bringToFront()
        }

        isEnabled = customIsEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        editText.isEnabled = enabled

        if (enabled) {
            setOnClickListener {
                toggle()
                editEditText()
            }
            editText.isClickable = true
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
        } else {
            setOnClickListener(null)
            editText.isClickable = false
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
        }
    }

    fun updateHint(hint: String?) {
        label.text = hint ?: ""
        editText.hint = ""
        hintText = hint ?: ""
    }

    @ColorInt
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun customizeFromAttributes() {
        if (textColor != Int.MIN_VALUE) {
            label.setTextColor(adjustAlpha(textColor, 0.7f))
            editText.setTextColor(textColor)
        } else {
            label.currentTextColor.let {
                textColor = it
                editText.setTextColor(it)
            }
        }

        if (imageDrawableId != -1) {
            image.setImageDrawable(ContextCompat.getDrawable(context, imageDrawableId))
            image.setColorFilter(imageColour)

            label.apply {
                post {
                    setPaddingRelative(
                        paddingStart + labelPadding,
                        paddingTop,
                        paddingEnd,
                        paddingBottom
                    )
                }

            }
        } else {
            image.visibility = View.GONE
        }

        card.background.setColorFilter(textBoxColor, PorterDuff.Mode.SRC_IN)

        setCursorColor(textColor)

        editText.setText(presetText)

        updateHint(hintText)

        if(inputType != Int.MIN_VALUE) {
            editText.inputType = inputType
        }

        if(maxLines != -1) {
            editText.maxLines = maxLines
        }

        editText.textSize = textSize
        label.textSize = textSize
    }

    private fun setCursorColor(@ColorInt color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(editText)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(editText)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(editText.context, drawableResId)
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)
        } catch (ignored: Exception) {
        }

    }
}
