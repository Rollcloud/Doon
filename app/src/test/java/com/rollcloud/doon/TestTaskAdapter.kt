package com.rollcloud.doon

import com.rollcloud.doon.adapters.TaskAdapter
import kotlin.time.Duration
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestTaskAdapter {
  @Test
  fun calculateScoreRegular5() {
    val frequency: Duration = (1).toDuration(DAYS)
    val timestampDeltas: List<Long> =
      listOf(1, 1, 1, 1, 1).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }

  @Test
  fun calculateScoreRegular0() {
    val frequency: Duration = (1).toDuration(DAYS)
    val timestampDeltas: List<Long> = listOf<Long>()

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }

  @Test
  fun calculateScoreRegular1() {
    val frequency: Duration = (1).toDuration(DAYS)
    val timestampDeltas: List<Long> = listOf(1).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }

  @Test
  fun calculateScoreRegular2() {
    val frequency: Duration = (1).toDuration(DAYS)
    val timestampDeltas: List<Long> =
      listOf(1, 1).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }

  @Test
  fun calculateScoreRegular6() {
    val frequency: Duration = (1).toDuration(DAYS)
    val timestampDeltas: List<Long> =
      listOf(0, 1, 1, 1, 1, 1).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }

  @Test
  fun calculateScoreMean() {
    val frequency: Duration = (0).toDuration(DAYS)
    val timestampDeltas: List<Long> =
      listOf(1, 2, 3, 4, 5).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(3F, result)
  }

  @Test
  fun calculateScorePerfectDespite() {
    val frequency: Duration = (3).toDuration(DAYS)
    val timestampDeltas: List<Long> =
      listOf(1, 2, 3, 4, 5).map { a -> a.toDuration(DAYS).toLong(MILLISECONDS) }

    val result = TaskAdapter.calculateScore(frequency, timestampDeltas)

    assertEquals(0F, result)
  }
}
