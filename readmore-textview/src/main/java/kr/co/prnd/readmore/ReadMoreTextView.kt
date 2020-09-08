package kr.co.prnd.readmore

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible

class ReadMoreTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var readMoreMaxLine = DEFAULT_MAX_LINE
    private var readMoreText = context.getString(R.string.read_more)
    private var readMoreColor = ContextCompat.getColor(context, R.color.read_more)

    var state: State = State.COLLAPSED
        private set(value) {
            field = value
            text = when (value) {
                State.EXPANDED -> {
                    maxLines = Int.MAX_VALUE
                    originalText
                }
                State.COLLAPSED -> collapseText
            }
            changeListener?.onStateChange(value)
        }

    val isExpanded
        get() = state == State.EXPANDED

    val isCollapsed
        get() = state == State.COLLAPSED

    var changeListener: ChangeListener? = null

    private var originalText: CharSequence = ""
    private var collapseText: CharSequence = ""

    init {
        setupAttributes(context, attrs, defStyleAttr)
        setupListener()
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView, defStyleAttr, 0)

        readMoreMaxLine =
            typedArray.getInt(R.styleable.ReadMoreTextView_readMoreMaxLine, readMoreMaxLine)
        readMoreText =
            typedArray.getString(R.styleable.ReadMoreTextView_readMoreText) ?: readMoreText
        readMoreColor =
            typedArray.getColor(R.styleable.ReadMoreTextView_readMoreColor, readMoreColor)
        typedArray.recycle()
    }

    private fun setupListener() {
        super.setOnClickListener { toggle() }
    }

    fun toggle() {
        when (state) {
            State.EXPANDED -> collapse()
            State.COLLAPSED -> expand()
        }
    }

    fun collapse() {
        if (isCollapsed || collapseText.isEmpty()) {
            return
        }
        state = State.COLLAPSED
    }

    fun expand() {
        if (isExpanded || originalText.isEmpty()) {
            return
        }
        state = State.EXPANDED
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        throw UnsupportedOperationException("You can not use OnClickListener in ReadMoreTextView")
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        doOnLayout {
            post { setupReadMore() }
        }
    }

    private fun setupReadMore() {
        if (needSkipSetupReadMore()) {
            return
        }
        originalText = text
        maxLines = readMoreMaxLine
        
        val adjustCutCount = getAdjustCutCount(readMoreMaxLine, readMoreText)
        val maxTextIndex = layout.getLineVisibleEnd(readMoreMaxLine - 1)
        val originalSubText = originalText.substring(0, maxTextIndex - 1 - adjustCutCount)

        collapseText = buildSpannedString {
            append(originalSubText)
            color(readMoreColor) { append(readMoreText) }
        }

        text = collapseText

    }

    private fun needSkipSetupReadMore(): Boolean =
        isInvisible || lineCount <= readMoreMaxLine || isExpanded || text == null || text == collapseText

    private fun getAdjustCutCount(maxLine: Int, readMoreText: String): Int {

        val lastLineStartIndex = layout.getLineVisibleEnd(maxLine - 2) + 1
        val lastLineEndIndex = layout.getLineVisibleEnd(maxLine - 1)
        val lastLineText = text.substring(lastLineStartIndex, lastLineEndIndex)

        val bounds = Rect()
        paint.getTextBounds(lastLineText, 0, lastLineText.length, bounds)

        var adjustCutCount = -1
        do {
            adjustCutCount++
            val subText = lastLineText.substring(0, lastLineText.length - adjustCutCount)
            val replacedText = subText + readMoreText
            paint.getTextBounds(replacedText, 0, replacedText.length, bounds)
            val replacedTextWidth = bounds.width()
        } while (replacedTextWidth > width)

        return adjustCutCount
    }

    enum class State {
        EXPANDED, COLLAPSED
    }

    interface ChangeListener {
        fun onStateChange(state: State)
    }

    companion object {
        private const val DEFAULT_MAX_LINE = 4
    }

}
