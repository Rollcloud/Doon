package com.rollcloud.doon

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task.view.*
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private val modelList: List<TaskWithActions>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.`item_task`, parent, false)
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
            """Sets values in UI."""
            with(itemView) {
                var nextDue = 0L
                if (actionsTask.actions.isEmpty())
                    nextDue = actionsTask.task.startDate
                else
                    nextDue = actionsTask.actions.last().timestamp + actionsTask.task.frequency
                updateColorTag(nextDue)
                updateDueDate(nextDue)
                updateDueDelta(nextDue)

                txtShowTitle.text = actionsTask.task.name
            }
        }

        private fun updateColorTag(nextDue: Long) {
            val colors = itemView.resources.getIntArray(R.array.random_color)
            val colorQueued = colors[9]
            val colorApproaching = colors[13]
            val colorOverdue = colors[0]

            var taskColor = colorQueued
            if (nextDue < 3600 * 1000) {
                taskColor = colorApproaching
            }
            if (nextDue < 0) {
                taskColor = colorOverdue
            }

            itemView.viewColorTag.setBackgroundColor(taskColor)
        }

        private fun updateDueDate(nextDue: Long) {
            //Mon, 5 Jan 2020
            val myFormat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myFormat)
            itemView.txtShowDue.text = sdf.format(Date(nextDue))
        }

        private fun updateDueDelta(nextDue: Long) {
            //Mon, 5 Jan 2020
            val myFormat = "h:mm a"
            val sdf = SimpleDateFormat(myFormat)
            itemView.txtShowDelta.text = sdf.format(Date(nextDue))
        }
    }

}


