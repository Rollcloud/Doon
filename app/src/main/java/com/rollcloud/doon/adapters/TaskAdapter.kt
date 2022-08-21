package com.rollcloud.doon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.Action
import com.rollcloud.doon.MILLIS_PER_DAY
import com.rollcloud.doon.R
import com.rollcloud.doon.TaskWithActions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.zipWithNext
import kotlinx.android.synthetic.main.item_task.view.*

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
    fun bind(actionsTask: TaskWithActions) {
      /* Sets values in UI. */
      with(itemView) {
        val nextDue =
          if (actionsTask.actions.isEmpty()) actionsTask.task.startDate
          else actionsTask.actions.last().timestamp + actionsTask.task.frequency

        txtShowName.text = actionsTask.task.name
        txtShowFrequency.text = buildString {
          append((actionsTask.task.frequency / MILLIS_PER_DAY))
          append(" days")
        }
        updateColorTag(nextDue)
        updateDueDate(nextDue)
        updateDueDelta(nextDue)
        updateScore(actionsTask.task.frequency, actionsTask.actions)
      }
    }

    fun calculateScore(frequency: Long, timestampDeltas: List<Long>): Float {
      /*
       * A scoring algorithm for timeliness of task performance.
       * Must provide an output in decimal days.
       */
      // Average interval of last 5 actions - frequency
      if (timestampDeltas.isEmpty()) return 0F
      val scoringDeltas: List<Long>
      val n = 5
      scoringDeltas = if (timestampDeltas.size < n) timestampDeltas else timestampDeltas.takeLast(n)
      val numberDeltas = scoringDeltas.size
      val meanInterval = (scoringDeltas.last() - scoringDeltas.first()).toFloat() / numberDeltas
      val score = meanInterval - frequency
      return score / MILLIS_PER_DAY
    }

    private fun updateScore(frequency: Long, actions: List<Action>) {
      val timestamps = actions.map { it.timestamp }
      val deltas = timestamps.zipWithNext { a, b -> b - a }
      val score = calculateScore(frequency, deltas)
      itemView.txtShowScore.text = buildString {
        append(String.format("%.1f", score))
        append(" days")
      }
    }

    private fun updateColorTag(nextDue: Long) {
      val colors = itemView.resources.getIntArray(R.array.random_color)
      val colorQueued = colors[9]
      val colorDue = colors[13]
      val colorOverdue = colors[0]

      val now = System.currentTimeMillis()

      var taskColor = colorQueued
      if (nextDue - now < MILLIS_PER_DAY * 2) {
        taskColor = colorDue
      }
      if (nextDue - now < -MILLIS_PER_DAY) {
        taskColor = colorOverdue
      }

      itemView.viewColorTag.setBackgroundColor(taskColor)
    }

    private fun updateDueDate(nextDue: Long) {
      // Mon, 5 Jan 2020
      val myFormat = "EEE, d MMM yyyy"
      val sdf = SimpleDateFormat(myFormat)
      itemView.txtShowDue.text = sdf.format(Date(nextDue))
    }

    private fun updateDueDelta(nextDue: Long) {
      // 2 days
      val now = System.currentTimeMillis()
      val delta = nextDue - now
      itemView.txtShowDelta.text = buildString {
        append((delta / MILLIS_PER_DAY))
        append(" days")
      }
    }
  }
}