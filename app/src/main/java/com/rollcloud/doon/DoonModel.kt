package com.rollcloud.doon

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

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
)

data class ActionWithTask(
  @Relation(parentColumn = "task_id", entityColumn = "id") val task: Task,
  @Embedded val action: Action
)
