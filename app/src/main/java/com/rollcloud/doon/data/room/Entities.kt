package com.rollcloud.doon.data.room

import android.content.Context
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.rollcloud.doon.R
import com.rollcloud.doon.ui.adapters.clock
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.*

private val localTimeZone = TimeZone.currentSystemDefault()

@Entity
data class Task(
  var name: String,
  var startDate: Long,
  var frequency: Long,
  var isActive: Int = 0,
  @PrimaryKey(autoGenerate = true) var id: Long = 0
) {

  fun getColour(context: Context): Int {
    val colors = context.resources.getIntArray(R.array.colors_400)
    val taskHash = "${id}${name}".hashCode().absoluteValue
    return colors[taskHash % colors.size]
  }
}

@Entity(
  foreignKeys =
    [
      ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["task_id"],
        onDelete = CASCADE
      )
    ]
)
data class Action(
  var task_id: Long,
  var timestamp: Long,
  @PrimaryKey(autoGenerate = true) var id: Long = 0
)

data class TaskWithActions(
  @Embedded val task: Task,
  @Relation(parentColumn = "id", entityColumn = "task_id") val actions: List<Action>
) {

  /**
   * Calculates the average actual frequency delta over the last `length` actions. Returns the
   * result in decimal days, where more frequent performance is negative.
   */
  fun movingAverageFrequency(length: Int): Float? {
    val timestamps = actions.map { it.timestamp }
    val timestampDeltas = timestamps.zipWithNext { a, b -> b - a }
    val frequency: Duration = task.frequency.toDuration(DurationUnit.MILLISECONDS)

    if (timestampDeltas.isEmpty()) return null

    val scoringDeltas =
      if (timestampDeltas.size < length) timestampDeltas else timestampDeltas.takeLast(length)
    val numberDeltas = scoringDeltas.size
    val meanInterval: Duration =
      scoringDeltas.sum().toDuration(DurationUnit.MILLISECONDS) / numberDeltas
    val score = meanInterval - frequency
    return score.inWholeHours / 24F
  }

  fun getNextDue(): Instant {
    return Instant.fromEpochMilliseconds(
      if (actions.isEmpty()) task.startDate else actions.last().timestamp + task.frequency
    )
  }

  /**
   * Returns the number of days till the task is due. Overdue tasks will return a negative number.
   */
  fun getDaysTillDue(): Int {
    val dueDate: LocalDate = getNextDue().toLocalDateTime(localTimeZone).date
    val today: LocalDate = clock.todayIn(localTimeZone)
    return today.daysUntil(dueDate)
  }
}

data class ActionWithTask(
  @Relation(parentColumn = "task_id", entityColumn = "id") val task: Task,
  @Embedded val action: Action
)
