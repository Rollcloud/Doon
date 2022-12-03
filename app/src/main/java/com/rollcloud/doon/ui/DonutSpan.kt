package com.prolificinteractive.materialcalendarview.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/** Span to draw a circle behind a section of text */
class DonutSpan : LineBackgroundSpan {
  private val radius: Float
  private val borderRadius: Float
  private val color: Int

  /**
   * Create a span to draw a circle using default radius and color
   *
   * @see .DonutSpan
   * @see .DEFAULT_RADIUS
   */
  constructor() {
    radius = DonutSpan.Companion.DEFAULT_RADIUS
    borderRadius = DonutSpan.Companion.DEFAULT_BORDER_RADIUS
    color = 0
  }

  /**
   * Create a span to draw a circle using a specified color
   *
   * @param color color of the circle
   * @see .DonutSpan
   * @see .DEFAULT_RADIUS
   */
  constructor(color: Int) {
    radius = DonutSpan.Companion.DEFAULT_RADIUS
    borderRadius = DonutSpan.Companion.DEFAULT_BORDER_RADIUS
    this.color = color
  }

  /**
   * Create a span to draw a circle using a specified radius
   *
   * @param radius radius for the circle
   * @see .DonutSpan
   */
  constructor(radius: Float) {
    this.radius = radius
    borderRadius = DonutSpan.Companion.DEFAULT_BORDER_RADIUS
    color = 0
  }

  /**
   * Create a span to draw a circle using a specified radius and color
   *
   * @param radius radius for the circle
   * @param color color of the circle
   * @param borderRadius radius for the circle border
   */
  constructor(radius: Float, color: Int, borderRadius: Float) {
    this.radius = radius
    this.color = color
    this.borderRadius = borderRadius
  }

  override fun drawBackground(
    canvas: Canvas,
    paint: Paint,
    left: Int,
    right: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    charSequence: CharSequence,
    start: Int,
    end: Int,
    lineNum: Int
  ) {
    val oldColor = paint.color
    if (color != 0) {
      paint.color = color
    }
    val oldStyle = paint.style
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderRadius
    canvas.drawCircle(((left + right) / 2).toFloat(), ((top + bottom) / 2).toFloat(), radius, paint)
    paint.style = oldStyle
    paint.color = oldColor
  }

  companion object {
    /** Default radius used */
    const val DEFAULT_RADIUS = 35f
    const val DEFAULT_BORDER_RADIUS = 5f
  }
}
