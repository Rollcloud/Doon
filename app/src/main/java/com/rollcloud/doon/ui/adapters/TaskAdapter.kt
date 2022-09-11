package com.rollcloud.doon.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.R
import com.rollcloud.doon.data.room.TaskWithActions
import java.time.format.TextStyle
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

val clock: Clock = Clock.System

class TaskAdapter(private val modelList: List<TaskWithActions>) :
  RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

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
        val nextDue: Instant = actionsTask.getNextDue()
        val queuedThreshold: Duration = (frequency / 3)

        txtShowName.text = actionsTask.task.name
        txtShowFrequency.text = "${frequency.inWholeDays} days"

        updateColorTag(nextDue, queuedThreshold)
        updateDueDate(nextDue)
        updateDueDelta(nextDue)
        updateScore(actionsTask)
      }
    }

    private fun updateScore(actionsTask: TaskWithActions) {
      val length = 3
      val score = actionsTask.movingAverageFrequency(length) ?: return
      itemView.txtShowScore.text = "${"%+.1f".format(score)} days"
    }

    private fun updateColorTag(nextDue: Instant, queuedThreshold: Duration) {
      val colors = itemView.resources.getIntArray(R.array.colors_400)
      val colorQueued = colors[9]
      val colorDue = colors[13]
      val colorOverdue = colors[0]

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
