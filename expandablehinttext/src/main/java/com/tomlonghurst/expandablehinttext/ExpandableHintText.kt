package com.tomlonghurst.expandablehinttext

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.tomlonghurst.expandablehinttext.databinding.EhtLayoutBinding
import com.tomlonghurst.expandablehinttext.extensions.beGone
import com.tomlonghurst.expandablehinttext.extensions.beVisible
import com.tomlonghurst.expandablehinttext.extensions.postOnMainThread
import com.tomlonghurst.expandablehinttext.extensions.remove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A view used for user input
 */
class ExpandableHintText : FrameLayout {

    private lateinit var viewBinding: EhtLayoutBinding
    private var inputMethodManager: InputMethodManager? = null
    private lateinit var editText: ExpandableEditText

    /**
     * Get the Edit Text when it is rendered and ready
     * @sample ExpandableHintText.useEditText { editText -> editText.height = 500 }
     */
    fun useEditText(action: (editText: EditText) -> Unit) {
        editText.postOnMainThread {
            action.invoke(editText)
        }
    }

    /**
     * The hint that tells the user what they should input
     */
    var hintText: String? = ""
        set(value) {
            field = value
            viewBinding.label.postOnMainThread {
                editText.postOnMainThread {
                    viewBinding.label.text = hintText ?: ""
                    editText.hint = ""
                }
            }
        }

    private var labelTranslationY = -1
    private var labelTranslationX = -1

    /**
     * Boolean for is the field expanded
     * i.e. Is the hint floating as a label above the edit text?
     */
    var isExpanded = false
        private set

    /**
     * Animation time in milliseconds
     */
    var animationDurationMs = -1

    /**
     * Color for the text
     * @sample ExpandableHintText.textColor = Color.WHITE
     */
    @ColorInt
    var textColor = Int.MIN_VALUE
        set(value) {
            field = value

            viewBinding.label.postOnMainThread {
                editText.postOnMainThread {
                    if (textColor != Int.MIN_VALUE) {
                        viewBinding.label.setTextColor(adjustAlpha(textColor, DEFAULT_HINT_OPACITY))
                        editText.setTextColor(textColor)
                    } else {
                        viewBinding.label.currentTextColor.let { currentTextColor ->
                            this.textColor = currentTextColor
                            editText.setTextColor(currentTextColor)
                        }
                    }
                }
            }
        }

    /**
     * Size for the text
     * @sample ExpandableHintText.textSize = 16f
     */
    var textSize: Float = -1f
        set(value) {
            field = value

            viewBinding.label.postOnMainThread {
                editText.postOnMainThread {
                    editText.textSize = textSize
                    viewBinding.label.textSize = textSize
                }
            }
        }

    /**
     * Image for the text field
     * @sample ExpandableHintText.imageDrawableId = R.drawable.logo
     */
    @DrawableRes
    var imageDrawableId = -1
        set(value) {
            field = value

            viewBinding.image.postOnMainThread {
                if (imageDrawableId != -1) {
                    viewBinding.image.apply {
                        setImageDrawable(ContextCompat.getDrawable(context, imageDrawableId))
                        setColorFilter(imageColour)
                        viewBinding.image.beVisible()
                    }

                    viewBinding.label.apply {
                        postOnMainThread {
                            setPaddingRelative(
                                paddingStart + labelPadding,
                                paddingTop,
                                paddingEnd,
                                paddingBottom
                            )
                        }
                    }
                } else {
                    viewBinding.image.beGone()
                }
            }
        }

    private var cardCollapsedHeight = -1

    /**
     * Color for the image
     * @sample ExpandableHintText.imageColour = Color.WHITE
     */
    @ColorInt
    var imageColour = Color.BLACK
        set(value) {
            field = value
            viewBinding.image.postOnMainThread {
                viewBinding.image.setColorFilter(imageColour)
            }
        }

    /**
     * Color for the text hint label when floating
     * @sample ExpandableHintText.floatingLabelColor = Color.WHITE
     */
    @ColorInt
    var floatingLabelColor = Color.WHITE

    /**
     * Color for the text field background
     * @sample ExpandableHintText.textBoxColor = Color.WHITE
     */
    @ColorInt
    var textBoxColor: Int = Color.WHITE
        set(value) {
            field = value

            viewBinding.card.postOnMainThread {
                viewBinding.card.background.setColorFilter(textBoxColor, PorterDuff.Mode.SRC_IN)
            }
        }

    /**
     * Set or Get the text of the input field
     * @sample ExpandableHintText.text = "Blah"
     */
    var text: String?
        set(value) {
            editText.postOnMainThread {
                editText.setText(value)
                if(editText.isSelected && editText.text?.isNotEmpty() == true) {
                    editText.setSelection(editText.text!!.length - 1)
                }
                invalidate()
            }
        }
        get() = editText.text.toString()

    /**
     * See the input type for the text
     * @sample ExpandableHintText.inputType = InputType.TYPE_CLASS_NUMBER
     */
    var inputType: Int = -1
        set(value) {
            field = value

            if (inputType != Int.MIN_VALUE) {
                editText.postOnMainThread {
                    editText.inputType = inputType
                }
            }
        }

    /**
     * Max lines to be displayed
     * @sample ExpandableHintText.maxLines = 5
     */
    var maxLines: Int = -1
        set(value) {
            field = value

            if (maxLines != -1) {
                editText.postOnMainThread {
                    editText.maxLines = maxLines
                }
            }
        }

    private val labelPadding get() =
        if (imageDrawableId == -1) {
            0
        } else {
            ViewHelper.getDp(context, FORTY_FIVE_DP)
        }

    /**
     * Boolean for if the input is disabled and read only
     * @sample ExpandableHintText.readOnly = true
     */
    var readOnly: Boolean
        set(value) {
            isEnabled = !value
        }
        get() = !isEnabled

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }



    private fun init(context: Context, attrs: AttributeSet?) {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        View.inflate(context, R.layout.eht_layout, this)
        viewBinding = EhtLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        addEditText()
        attrs?.let {
            getAttributes(context, it)
            GlobalScope.launch(Dispatchers.Main) {
                val drawable = viewBinding.loadAnimation.indeterminateDrawable.mutate()
                drawable.setColorFilter(textBoxColor, PorterDuff.Mode.SRC_IN)
                viewBinding.loadAnimation.indeterminateDrawable = drawable
            }
        }
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

    private fun reduce() {
        if (isExpanded) {

            if (editText.text.toString().isBlank()) {

                editText.apply {
                    postOnMainThread {
                        ViewCompat.animate(this)
                            .alpha(0f).duration = animationDurationMs.toLong()
                    }
                }

                viewBinding.label.apply {
                    postOnMainThread {
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
                value ?: return
                textView.setTextColor(value)
            }
        }

        ObjectAnimator.ofInt(view, property, endColour).apply {
            duration = animationDurationMs.toLong()
            setEvaluator(ArgbEvaluator())
            interpolator = DecelerateInterpolator(2f)
            addListener(object : Animator.AnimatorListener {
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
            start()
        }
    }

    private fun expand() {
        if (!isExpanded) {

            editText.postOnMainThread {
                ViewCompat.animate(editText)
                    .alpha(1f).duration = animationDurationMs.toLong()
            }

            val miniatureScale = 0.7f

            viewBinding.label.apply {
                postOnMainThread {
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
            editText.postOnMainThread {
                editText.requestFocus()
                inputMethodManager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun addEditText() {
        editText = ExpandableEditText(context).apply {
            setOnBackPressListener(Runnable {
                editText.clearFocus()
                viewBinding.card?.clearFocus()
            })

            setOnFocusChangeListener { _, hasFocus ->
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

            postOnMainThread {
                setPaddingRelative(ViewHelper.getDp(context, TEN_DP), paddingTop, paddingEnd, paddingBottom)
            }
        }
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ExpandableHintText)

        animationDurationMs = styledAttrs.getInteger(R.styleable.ExpandableHintText_animationDurationMs, DEFAULT_ANIMATION_MS)
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
        textSize = styledAttrs.getFloat(R.styleable.ExpandableHintText_android_textSize, DEFAULT_TEXT_SIZE)
        styledAttrs.recycle()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        viewBinding.card.setOnClickListener(l)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewBinding.card.postOnMainThread {
            viewBinding.card.addView(editText)
        }

        viewBinding.label.postOnMainThread {
            viewBinding.label.pivotX = 0f
            viewBinding.label.pivotY = 0f
            labelTranslationY = (viewBinding.label.layoutParams as? LayoutParams)?.topMargin ?: 0
            labelTranslationX = viewBinding.label.paddingStart
            setCursorColor(textColor)
            viewBinding.label.bringToFront()
        }

        editText.postOnMainThread {
            editText.setBackgroundColor(Color.TRANSPARENT)
            editText.alpha = 0f
            editText.clearFocus()
            editText.bringToFront()

            if (editText.text.toString().isNotBlank()) {
                expand()
            }

            viewBinding.card.postOnMainThread {
                viewBinding.label.postOnMainThread {
                    postOnMainThread {
                        viewBinding.expandableEditTextFrame.invalidate()

                        viewBinding.loadAnimation.remove()

                        viewBinding.expandableEditTextFrame.beVisible()

                        zoomIn()
                    }
                }
            }
        }
    }

    private fun zoomIn() {
        val zoomInAnimation = AnimationUtils.loadAnimation(
            context,
            R.anim.zoom_in
        )
        viewBinding.expandableEditTextFrame.startAnimation(zoomInAnimation)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        editText.isEnabled = enabled
        editText.isClickable = enabled

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

    @SuppressLint("SoonBlockedPrivateApi")
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
