package com.rollcloud.doon.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.ActionWithTask
import com.rollcloud.doon.R
import com.rollcloud.doon.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.item_task_completed.view.*
import kotlinx.android.synthetic.main.item_task_completed.view.txtShowName
import kotlinx.android.synthetic.main.item_task_completed.view.viewColorTag

class ActionsAdapter(private val taskActions: List<ActionWithTask>) :
  RecyclerView.Adapter<ActionsAdapter.ActionsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionsViewHolder {
    return ActionsViewHolder(
      LayoutInflater.from(parent.context).inflate(R.layout.item_task_completed, parent, false)
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
    fun bind(taskAction: ActionWithTask) {
      /* Sets values in UI. */
      with(itemView) {
        txtShowName.text = taskAction.task.name
        updateColorTag(taskAction.task)
        updateTime(taskAction.action.timestamp)
        updateDate(taskAction.action.timestamp)
      }
    }

    private fun updateColorTag(task: Task) {
      val colors = itemView.resources.getIntArray(R.array.random_color)
      val taskHash = "${task.id}${task.name}".hashCode()
      val randomColor = colors[taskHash % colors.size]
      itemView.viewColorTag.setBackgroundColor(randomColor)
    }

    private fun updateTime(time: Long) {
      // Mon, 5 Jan 2020
      val myFormat = "h:mm a"
      val sdf = SimpleDateFormat(myFormat)
      itemView.txtShowTime.text = sdf.format(Date(time))
    }

    private fun updateDate(time: Long) {
      // Mon, 5 Jan 2020
      val myFormat = "EEE, d MMM yyyy, h:mm:ss a"
      val sdf = SimpleDateFormat(myFormat)
      itemView.txtShowDate.text = sdf.format(Date(time))
    }
  }
}
