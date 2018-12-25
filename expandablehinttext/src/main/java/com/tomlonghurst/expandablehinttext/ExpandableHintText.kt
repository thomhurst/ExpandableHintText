package com.tomlonghurst.expandablehinttext

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.text.method.KeyListener
import android.util.AttributeSet
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView


class ExpandableHintText : FrameLayout {
    private lateinit var inputMethodManager: InputMethodManager

    var label: TextView? = null
        private set
    lateinit var card: View
        private set
    lateinit var image: ImageView
        private set
    lateinit var editText: ExpandableEditText
        private set
    lateinit var editTextLayout: ViewGroup
        private set
    private var hintText: String? = null

    private var labelTranslationY = -1
    private var labelTranslationX = -1
    var isExpanded = false
        private set

    private var animationDuration = -1
    private var textColor = -1
    private var imageDrawableId = -1
    private var cardCollapsedHeight = -1
    private var customHasFocus = true
    private var backgroundColor = -1
    private var imageColour = -1
    private var floatingLabelColor = -1
    private var textBoxColor: Int = -1
    private var presetText: String? = null
    private val labelPadding by lazy {
        if (imageDrawableId == -1) {
            0
        } else {
            getDp(45)
        }
    }

    private lateinit var oldKeyListener: KeyListener
    private var oldOnClickListener: OnClickListener? = null

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

                label?.apply {
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
            override fun get(`object`: TextView): Int {
                return `object`.currentTextColor
            }

            override fun set(`object`: TextView, value: Int?) {
                `object`.setTextColor(value!!)
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
                editEditText()
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

            label?.apply {
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
                inputMethodManager.showSoftInput(editText, 0)
            }
        }
    }

    override fun setBackgroundColor(color: Int) {
        this.backgroundColor = color
    }

    fun getBackgroundColor(): Int {
        return this.backgroundColor
    }

    private fun setHasFocus(hasFocus: Boolean) {
        this.customHasFocus = hasFocus

        if (hasFocus) {
            expand()

        } else {
            reduce()
        }
    }

    private fun addEditText(): ExpandableEditText {
        return ExpandableEditText(context).apply {
            setOnBackPressListener {
                editText.clearFocus()
                card.clearFocus()
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

            (context as? Activity)?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        var styledAttrs: TypedArray? = null
        try {
            styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ExpandableHintText)

            animationDuration = styledAttrs.getInteger(R.styleable.ExpandableHintText_animationDuration, 400)
            textColor = styledAttrs.getColor(R.styleable.ExpandableHintText_textColor, Int.MIN_VALUE)
            floatingLabelColor = styledAttrs.getColor(R.styleable.ExpandableHintText_floatingLabelColor, Color.BLACK)
            imageDrawableId = styledAttrs.getResourceId(R.styleable.ExpandableHintText_image, -1)
            imageColour = styledAttrs.getColor(R.styleable.ExpandableHintText_imageColor, Color.GRAY)
            cardCollapsedHeight = styledAttrs.getDimensionPixelOffset(
                R.styleable.ExpandableHintText_cardCollapsedHeight,
                context.resources.getDimensionPixelOffset(R.dimen.cardHeight_initial)
            )
            customHasFocus = styledAttrs.getBoolean(R.styleable.ExpandableHintText_hasFocus, false)
            isEnabled = styledAttrs.getBoolean(R.styleable.ExpandableHintText_enabled, true)
            hintText = styledAttrs.getString(R.styleable.ExpandableHintText_hint)
            textBoxColor = styledAttrs.getColor(R.styleable.ExpandableHintText_textBoxColor, Color.WHITE)
            presetText = styledAttrs.getString(R.styleable.ExpandableHintText_text)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            styledAttrs?.recycle()
        }

    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        super.setOnClickListener(l)
        oldOnClickListener = l
        editText.setOnClickListener(l)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addView(LayoutInflater.from(context).inflate(R.layout.eht_layout, this, false))

        editText = addEditText()
        editTextLayout = findViewById(R.id.editTextLayout)
        card = findViewById(R.id.card)
        image = findViewById(R.id.image)
        label = findViewById(R.id.label)

        editTextLayout.addView(editText)

        setHasFocus(customHasFocus)

        label?.pivotX = 0f
        label?.pivotY = 0f

        if (isEnabled) {
            label?.setOnClickListener { toggle() }
        } else {
            label?.isClickable = false
            label?.isFocusable = false
        }

        label?.bringToFront()
        if (isEnabled) {
            editText.isFocusableInTouchMode = true
            editText.isFocusable = true
            editText.bringToFront()
        }

        hintText?.let { updateHint(it) }

        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.alpha = 0f

        if (editText.text.toString().isNotBlank()) {
            expand()
        }

        labelTranslationY = (label?.layoutParams as? FrameLayout.LayoutParams)?.topMargin ?: 0
        labelTranslationX = label?.paddingLeft ?: 0
        customizeFromAttributes()

        this.setOnClickListener { v -> toggle() }

        editText.post { editText.clearFocus() }

        oldKeyListener = editText.keyListener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (::editText.isInitialized) {
            editText.isEnabled = isEnabled
        }

        if (isEnabled) {
            editText.keyListener = oldKeyListener
            label?.setOnClickListener { v -> toggle() }
        } else {
            editText.keyListener = null
            label?.setOnClickListener(null)
        }
    }

    private fun updateHint(hint: String?) {
        label?.text = hint ?: ""
        editText.hint = ""
        hintText = hint ?: ""
    }

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun customizeFromAttributes() {
        if (textColor != Int.MIN_VALUE) {
            label?.setTextColor(adjustAlpha(textColor, 0.7f))
            editText.setTextColor(textColor)
        } else {
            (label?.currentTextColor ?: Color.WHITE).let {
                textColor = it
                editText.setTextColor(it)
            }
        }

        if (imageDrawableId != -1) {
            image.setImageDrawable(ContextCompat.getDrawable(context, imageDrawableId))
            image.setColorFilter(imageColour)

            label?.setPaddingRelative(
                label!!.paddingStart + labelPadding,
                label!!.paddingTop,
                label!!.paddingEnd,
                label!!.paddingBottom
            )
        } else {
            image.visibility = View.GONE
        }

        card.background.setColorFilter(textBoxColor, PorterDuff.Mode.SRC_IN)

        setCursorColor(textColor)

        editText.setText(presetText)
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
