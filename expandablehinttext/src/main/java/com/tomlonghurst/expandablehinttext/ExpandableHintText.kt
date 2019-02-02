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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.tomlonghurst.expandablehinttext.extensions.beGone
import com.tomlonghurst.expandablehinttext.extensions.beVisible
import com.tomlonghurst.expandablehinttext.extensions.onGlobalLayout
import kotlinx.android.synthetic.main.eht_layout.view.*


class ExpandableHintText : FrameLayout {
    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var editText: ExpandableEditText

    fun useEditText(action: (editText: EditText) -> Unit) {
        editText.onGlobalLayout {
            editText.post {
                action.invoke(editText)
            }
        }
    }

    var hintText: String? = ""
        set(value) {
            field = value
            label.post {
                editText.post {
                    label.text = hintText ?: ""
                    editText.hint = ""
                }
            }
        }

    private var labelTranslationY = -1
    private var labelTranslationX = -1

    var isExpanded = false
        private set

    var animationDurationMs = -1

    @ColorInt
    var textColor = Int.MIN_VALUE
        set(value) {
            field = value

            label.post {
                editText.post {
                    if (textColor != Int.MIN_VALUE) {
                        label.setTextColor(adjustAlpha(textColor, 0.7f))
                        editText.setTextColor(textColor)
                    } else {
                        label.currentTextColor.let {
                            this.textColor = it
                            editText.setTextColor(it)
                        }
                    }
                }
            }
        }

    var textSize: Float = -1f
        set(value) {
            field = value

            label.post {
                editText.post {
                    editText.textSize = textSize
                    label.textSize = textSize
                }
            }
        }

    @DrawableRes
    var imageDrawableId = -1
        set(value) {
            field = value

            image.post {
                if (imageDrawableId != -1) {
                    image.apply {
                        setImageDrawable(ContextCompat.getDrawable(context, imageDrawableId))
                        setColorFilter(imageColour)
                        image.beVisible()
                    }

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
                    image.beGone()
                }
            }
        }

    private var cardCollapsedHeight = -1


    @ColorInt
    var imageColour = Color.BLACK
        set(value) {
            field = value
            image.post {
                image.setColorFilter(imageColour)
            }
        }

    @ColorInt
    var floatingLabelColor = Color.WHITE

    @ColorInt
    var textBoxColor: Int = Color.WHITE
        set(value) {
            field = value

            card.post {
                card.background.setColorFilter(textBoxColor, PorterDuff.Mode.SRC_IN)
            }
        }

    var text: String? = null
        set(value) {
            field = value

            editText.post {
                editText.setText(text)
                invalidate()
            }
        }


    var inputType: Int = -1
        set(value) {
            field = value

            if (inputType != Int.MIN_VALUE) {
                editText.post {
                    editText.inputType = inputType
                }
            }
        }

    var maxLines: Int = -1
        set(value) {
            field = value

            if (maxLines != -1) {
                editText.post {
                    editText.maxLines = maxLines
                }
            }
        }

    private val labelPadding get() =
        if (imageDrawableId == -1) {
            0
        } else {
            getDp(45)
        }

    var readOnly: Boolean
        set(value) {
            isEnabled = !value
        }
        get() = !isEnabled

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
        getAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
        getAttributes(context, attrs)
    }

    private fun init() {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        View.inflate(context, R.layout.eht_layout, this)
        addEditText()
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
                            .alpha(0f).duration = animationDurationMs.toLong()
                    }
                }

                label.apply {
                    post {
                        ViewCompat.animate(this)
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .translationX(0f).duration = animationDurationMs.toLong()

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
        animator.duration = animationDurationMs.toLong()
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
                    .alpha(1f).duration = animationDurationMs.toLong()
            }

            val miniatureScale = 0.7f

            label.apply {
                post {
                    ViewCompat.animate(this)
                        .scaleX(miniatureScale)
                        .scaleY(miniatureScale)
                        .translationY((-labelTranslationY).toFloat())
                        .translationX(-labelTranslationX.plus(labelPadding).times(miniatureScale)).duration =
                        animationDurationMs.toLong()

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
            setOnBackPressListener(Runnable {
                editText.clearFocus()
                card?.clearFocus()
            })

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

            animationDurationMs = styledAttrs.getInteger(R.styleable.ExpandableHintText_animationDurationMs, 400)
            textColor = styledAttrs.getColor(R.styleable.ExpandableHintText_android_textColor, Int.MIN_VALUE)
            floatingLabelColor = styledAttrs.getColor(R.styleable.ExpandableHintText_floatingLabelColor, Color.BLACK)
            imageDrawableId = styledAttrs.getResourceId(R.styleable.ExpandableHintText_image, -1)
            imageColour = styledAttrs.getColor(R.styleable.ExpandableHintText_imageColor, Color.GRAY)
            cardCollapsedHeight = styledAttrs.getDimensionPixelOffset(
                R.styleable.ExpandableHintText_cardCollapsedHeight,
                context.resources.getDimensionPixelOffset(R.dimen.cardHeight_initial)
            )
            readOnly = styledAttrs.getBoolean(R.styleable.ExpandableHintText_readOnly, false)
            hintText = styledAttrs.getString(R.styleable.ExpandableHintText_android_hint) ?: ""
            textBoxColor = styledAttrs.getColor(R.styleable.ExpandableHintText_textBoxColor, Color.WHITE)
            text = styledAttrs.getString(R.styleable.ExpandableHintText_android_text)
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
        card.setOnClickListener(l)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        card.post {
            card.addView(editText)
        }

        label.post {
            label.pivotX = 0f
            label.pivotY = 0f
            labelTranslationY = (label.layoutParams as FrameLayout.LayoutParams).topMargin
            labelTranslationX = label.paddingStart
            setCursorColor(textColor)
            label.bringToFront()
        }

        editText.post {
            editText.setBackgroundColor(Color.TRANSPARENT)
            editText.alpha = 0f
            editText.clearFocus()
            editText.bringToFront()

            if (editText.text.toString().isNotBlank()) {
                expand()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        editText.isEnabled = enabled
        editText.isFocusable = enabled
        editText.isClickable = enabled
        editText.isFocusableInTouchMode = enabled

        if (enabled) {
            setOnClickListener {
                toggle()
                editEditText()
            }
        } else {
            setOnClickListener(null)
        }
    }

    @ColorInt
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
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
