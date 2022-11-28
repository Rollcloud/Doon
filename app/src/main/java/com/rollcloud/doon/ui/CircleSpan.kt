package com.prolificinteractive.materialcalendarview.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/** Span to draw a circle behind a section of text */
class CircleSpan : LineBackgroundSpan {
  private val radius: Float
  private val color: Int

  /**
   * Create a span to draw a circle using default radius and color
   *
   * @see .CircleSpan
   * @see .DEFAULT_RADIUS
   */
  constructor() {
    radius = CircleSpan.Companion.DEFAULT_RADIUS
    color = 0
  }

  /**
   * Create a span to draw a circle using a specified color
   *
   * @param color color of the circle
   * @see .CircleSpan
   * @see .DEFAULT_RADIUS
   */
  constructor(color: Int) {
    radius = CircleSpan.Companion.DEFAULT_RADIUS
    this.color = color
  }

  /**
   * Create a span to draw a circle using a specified radius
   *
   * @param radius radius for the circle
   * @see .CircleSpan
   */
  constructor(radius: Float) {
    this.radius = radius
    color = 0
  }

  /**
   * Create a span to draw a circle using a specified radius and color
   *
   * @param radius radius for the circle
   * @param color color of the circle
   */
  constructor(radius: Float, color: Int) {
    this.radius = radius
    this.color = color
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
    canvas.drawCircle(((left + right) / 2).toFloat(), ((top + bottom) / 2).toFloat(), radius, paint)
    paint.color = oldColor
  }

  companion object {
    /** Default radius used */
    const val DEFAULT_RADIUS = 30f
  }
}
