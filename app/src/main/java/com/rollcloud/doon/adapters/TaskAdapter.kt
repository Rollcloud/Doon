package com.rollcloud.doon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.Action
import com.rollcloud.doon.R
import com.rollcloud.doon.TaskWithActions
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.zipWithNext
import kotlin.time.Duration
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

class TaskAdapter(private val modelList: List<TaskWithActions>) :
  RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

  companion object {
    fun calculateScore(frequency: Duration, timestampDeltas: List<Long>): Float? {
      /*
       * A scoring algorithm for timeliness of task performance.
       * Must provide an output in decimal days.
       */
      // Average interval of last 5 actions - frequency
      if (timestampDeltas.isEmpty())
        return null
      val scoringDeltas: List<Long>
      val n = 5
      scoringDeltas = if (timestampDeltas.size < n) timestampDeltas else timestampDeltas.takeLast(n)
      val numberDeltas = scoringDeltas.size
      val meanInterval: Duration = scoringDeltas.sum().toDuration(MILLISECONDS) / numberDeltas
      val score = meanInterval - frequency
      return score.inWholeHours / 24F
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
    return TaskViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
    )
  }

  override fun getItemCount() = modelList.size

  override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
    holder.bind(modelList[position])
  }

  override fun getItemId(position: Int): Long {
    return modelList[position].task.id
  }

  class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val localTimeZone = TimeZone.currentSystemDefault()
    private val locale: Locale = Locale.getDefault()

    fun bind(actionsTask: TaskWithActions) {
      /* Sets values in UI. */
      with(itemView) {
        val frequency: Duration = actionsTask.task.frequency.toDuration(MILLISECONDS)
        val nextDue: Instant =
          Instant.fromEpochMilliseconds(
            if (actionsTask.actions.isEmpty()) actionsTask.task.startDate
            else actionsTask.actions.last().timestamp + actionsTask.task.frequency
          )
        val queuedThreshold: Duration = (frequency / 3)

        txtShowName.text = actionsTask.task.name
        txtShowFrequency.text = "${frequency.inWholeDays} days"

        updateColorTag(nextDue, queuedThreshold)
        updateDueDate(nextDue)
        updateDueDelta(nextDue)
        updateScore(frequency, actionsTask.actions)
      }
    }

    private fun updateScore(frequency: Duration, actions: List<Action>) {
      val timestamps = actions.map { it.timestamp }
      val deltas = timestamps.zipWithNext { a, b -> b - a }
      val score = calculateScore(frequency, deltas)
      if (score == null) return // don't show score if null
      itemView.txtShowScore.text = "${String.format("%.1f", score)} days"
    }

    private fun updateColorTag(nextDue: Instant, queuedThreshold: Duration) {
      val colors = itemView.resources.getIntArray(R.array.random_color)
      val colorQueued = colors[9]
      val colorDue = colors[13]
      val colorOverdue = colors[0]

      val clock: Clock = Clock.System
      val now: Instant = clock.now()
      val timeUntil: Duration = nextDue - now

      val taskColor: Int =
        when {
          timeUntil < (-1).toDuration(DAYS) -> colorOverdue
          timeUntil < queuedThreshold -> colorDue
          else -> colorQueued
        }

      itemView.viewColorTag.setBackgroundColor(taskColor)
    }

    private fun updateDueDate(nextDue: Instant) {
      val d: LocalDateTime = nextDue.toLocalDateTime(localTimeZone)
      val dow: String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
      val month: String = d.month.getDisplayName(TextStyle.SHORT, locale)
      val nextDueFormatted = "$dow, ${d.dayOfMonth} $month ${d.year}"
      itemView.txtShowDue.text = nextDueFormatted
    }

    private fun updateDueDelta(nextDue: Instant) {
      val dueDate: LocalDate = nextDue.toLocalDateTime(localTimeZone).date
      val clock: Clock = Clock.System
      val today: LocalDate = clock.todayIn(localTimeZone)
      val daysUntil: Int = (dueDate - today).days

      when {
        daysUntil > 1 -> itemView.txtShowDelta.text = "In $daysUntil days"
        daysUntil == 1 ->
          itemView.txtShowDelta.text = itemView.context.getString(R.string.one_day_ahead)
        daysUntil == 0 -> itemView.txtShowDelta.text = itemView.context.getString(R.string.today)
        daysUntil == -1 ->
          itemView.txtShowDelta.text = itemView.context.getString(R.string.one_day_behind)
        else -> itemView.txtShowDelta.text = "${-daysUntil} days ago"
      }
    }
  }
}
