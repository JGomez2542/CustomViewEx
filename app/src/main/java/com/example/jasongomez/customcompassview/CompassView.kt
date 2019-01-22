package com.example.jasongomez.customcompassview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent

class CompassView : View {

    private var markerPaint: Paint
    private var textPaint: Paint
    private var circlePaint: Paint
    private var northString: String
    private var eastString: String
    private var southString: String
    private var westString: String
    private var textHeight: Int = 0

    //We create a custom setter to set the displayed bearing. Call invalidate() to make sure the
    //view is repainted when the bearing changes
    var mBearing: Float = 0f
        set(value) {
            field = value
            invalidate()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
        }


    //This constructor is invoked when the view is created programmatically
    constructor(context: Context) : this(context, null)

    //This constructor is invoked when the view is inflated using the xml file
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    //This constructor is invoked when the view is inflated using the xml file and a style is specified
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        isFocusable = true
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CompassView, defStyleAttr, 0)
        if (typedArray.hasValue(R.styleable.CompassView_bearing)) {
            mBearing = typedArray.getFloat(R.styleable.CompassView_bearing, 0f)
        }
        typedArray.recycle()

        //After setting our custom attribute, we initialize all our instance variables here
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = ContextCompat.getColor(context, R.color.background_color)
        circlePaint.strokeWidth = 1f
        circlePaint.style = Paint.Style.FILL_AND_STROKE

        northString = resources.getString(R.string.cardinal_north)
        westString = resources.getString(R.string.cardinal_west)
        eastString = resources.getString(R.string.cardinal_east)
        southString = resources.getString(R.string.cardinal_south)

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = ContextCompat.getColor(context, R.color.text_color)

        textHeight = textPaint.measureText("yY").toInt()

        markerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        markerPaint.color = ContextCompat.getColor(context, R.color.marker_color)
    }

    /**
     * This function is invoked by the parent layout when laying out the child view. The parent layout determines
     * the size and position of the child view and passes the width and height specs to the child view.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //The compass is a circle that fills as much space as possible.
        //Set the measured dimensions by figuring out the shortest boundary,
        //height or width.
        val measuredWidth = measure(widthMeasureSpec)
        val measuredHeight = measure(heightMeasureSpec)

        val d = Math.min(measuredHeight, measuredWidth)
        setMeasuredDimension(d, d)
    }

    override fun onDraw(canvas: Canvas?) {
        val mMeasuredWidth = measuredWidth
        val mMeasuredHeight = measuredHeight

        val px = mMeasuredWidth / 2
        val py = mMeasuredHeight / 2

        val radius = Math.min(px, py)

        //Draw the background
        canvas?.drawCircle(px.toFloat(), py.toFloat(), radius.toFloat(), circlePaint)

        //Rotate our perspective so that the 'top' is
        //facing the current bearing
        canvas?.save()
        canvas?.rotate(-mBearing, px.toFloat(), py.toFloat())

        val textWidth = textPaint.measureText("W").toInt()
        val cardinalX = px - textWidth / 2
        val cardinalY = py - radius + textHeight

        //Draw the marker every 15 degrees and text every 45.
        for (i in 0 until 24) {
            //Draw marker
            canvas?.drawLine(
                px.toFloat(),
                (py - radius).toFloat(),
                px.toFloat(),
                (py - radius + 10).toFloat(),
                markerPaint
            )
            canvas?.save()
            canvas?.translate(0f, textHeight.toFloat())

            // Draw the cardinal points
            if (i % 6 == 0) {
                var dirString = ""
                when (i) {
                    0 -> {
                        dirString = northString
                        val arrowY = 2 * textHeight
                        canvas?.drawLine(
                            px.toFloat(), arrowY.toFloat(), (px - 5).toFloat(),
                            3 * textHeight.toFloat(), markerPaint
                        )
                        canvas?.drawLine(
                            px.toFloat(), arrowY.toFloat(), (px + 5).toFloat(),
                            3 * textHeight.toFloat(), markerPaint
                        )
                    }

                    6 -> dirString = eastString
                    12 -> dirString = southString
                    18 -> dirString = westString
                }
                canvas?.drawText(dirString, cardinalX.toFloat(), cardinalY.toFloat(), textPaint)
            } else if (i % 3 == 0) {

                //Draw the text every alternate 45 deg
                val angle = (i*15).toString()
                val angleTextWidth = textPaint.measureText(angle)

                val angleTextX = (px-angleTextWidth/2)
                val angleTextY = py-radius+textHeight
                canvas?.drawText(angle, angleTextX, angleTextY.toFloat(), textPaint)
            }
            canvas?.restore()
            canvas?.rotate(15f, px.toFloat(), py.toFloat())
        }
        canvas?.restore()
    }

    /**
     * Function used to extract view dimensions.
     */
    private fun measure(measureSpec: Int): Int {
        var result: Int

        //Decode the measurement specifications.
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        //Return a default size of 200 if no bounds are specified.
        //Otherwise as you want to fill the available space always
        //return the full available bounds.
        result = if (specMode == MeasureSpec.UNSPECIFIED) 200 else specSize
        return result
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        super.dispatchPopulateAccessibilityEvent(event)
        return if (isShown) {
            val bearingStr = mBearing.toString()
            event?.text?.add(bearingStr)
            true
        } else {
            false
        }
    }
}