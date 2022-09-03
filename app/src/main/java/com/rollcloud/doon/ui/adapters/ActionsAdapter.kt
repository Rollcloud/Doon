package com.rollcloud.doon.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.R
import com.rollcloud.doon.data.room.ActionWithTask
import com.rollcloud.doon.data.room.Task
import java.time.format.TextStyle
import java.util.*
import kotlin.math.absoluteValue
import kotlinx.android.synthetic.main.item_action.view.*
import kotlinx.android.synthetic.main.item_action.view.txtShowName
import kotlinx.android.synthetic.main.item_action.view.viewColorTag
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

class ActionsAdapter(private val taskActions: List<ActionWithTask>) :
  RecyclerView.Adapter<ActionsAdapter.ActionsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionsViewHolder {
    return ActionsViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false)
    )
  }

  override fun getItemCount() = taskActions.size

  override fun onBindViewHolder(holder: ActionsViewHolder, position: Int) {
    holder.bind(taskActions[position])
  }

  override fun getItemId(position: Int): Long {
    return taskActions[position].action.id
  }

  class ActionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val localTimeZone = TimeZone.currentSystemDefault()
    private val locale: Locale = Locale.getDefault()

    fun bind(taskAction: ActionWithTask) {
      /* Sets values in UI. */
      with(itemView) {
        var timestamp: Instant = Instant.fromEpochMilliseconds(taskAction.action.timestamp)
        txtShowName.text = taskAction.task.name
        updateColorTag(taskAction.task)
        updateTime(timestamp)
        updateDate(timestamp)
      }
    }

    private fun updateColorTag(task: Task) {
      val colors = itemView.resources.getIntArray(R.array.colors_400)
      val taskHash = "${task.id}${task.name}".hashCode().absoluteValue
      val randomColor = colors[taskHash % colors.size]
      itemView.viewColorTag.setBackgroundColor(randomColor)
    }

    private fun updateTime(timestamp: Instant) {
      val t: LocalTime = timestamp.toLocalDateTime(localTimeZone).time
      val timeFormatted = t.toString()
      itemView.txtShowTime.text = timeFormatted
    }

    private fun updateDate(timestamp: Instant) {
      val d: LocalDate = timestamp.toLocalDateTime(localTimeZone).date
      val dow: String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
      val month: String = d.month.getDisplayName(TextStyle.SHORT, locale)
      val nextDueFormatted = "$dow, ${d.dayOfMonth} $month ${d.year}"
      itemView.txtShowDate.text = nextDueFormatted
    }
  }
}
