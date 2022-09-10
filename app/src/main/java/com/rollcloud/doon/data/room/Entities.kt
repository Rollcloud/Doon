package com.rollcloud.doon.data.room

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.rollcloud.doon.ui.adapters.clock
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

private val localTimeZone = TimeZone.currentSystemDefault()

@Entity
data class Task(
  var name: String,
  var startDate: Long,
  var frequency: Long,
  var isActive: Int = 0,
  @PrimaryKey(autoGenerate = true) var id: Long = 0
)

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
  fun movingAverageFrequency(): Float? {
    /*
     * A scoring algorithm for timeliness of task performance.
     * Must provide an output in decimal days.
     * Returns frequency subtracted from the average interval of last 5 actions.
     */
    val n = 5
    val timestamps = actions.map { it.timestamp }
    val timestampDeltas = timestamps.zipWithNext { a, b -> b - a }
    val frequency: Duration = task.frequency.toDuration(DurationUnit.MILLISECONDS)

    if (timestampDeltas.isEmpty()) return null

    val scoringDeltas =
      if (timestampDeltas.size < n) timestampDeltas else timestampDeltas.takeLast(n)
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
    val daysUntil: Int = (dueDate - today).days

    return daysUntil
  }
}

data class ActionWithTask(
  @Relation(parentColumn = "task_id", entityColumn = "id") val task: Task,
  @Embedded val action: Action
)
