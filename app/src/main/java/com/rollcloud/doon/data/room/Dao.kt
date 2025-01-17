package com.rollcloud.doon.data.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
  @Insert(onConflict = OnConflictStrategy.ABORT) fun insertTask(task: Task)

  @Update fun updateTask(task: Task)

  @Delete fun deleteTask(task: Task)

  @Query("SELECT * FROM Task WHERE id=:uid") fun getTask(uid: Long): Task

  //  @Query("Update Task Set isActive = 0 where id=:uid") fun disableTask(uid: Long)

  //  @Query("Update Task Set isActive = 1 where id=:uid") fun enableTask(uid: Long)
}

@Dao
interface ActionDao {
  @Insert(onConflict = OnConflictStrategy.ABORT) fun insertAction(action: Action)

  //  @Update fun updateAction(action: Action)

  //  @Delete fun deleteAction(action: Action)

  @Query("SELECT * FROM `action` ORDER BY timestamp DESC")
  fun getActions(): LiveData<List<ActionWithTask>>

  @Query("SELECT * FROM `action` WHERE task_id = :task_uid ORDER BY timestamp DESC")
  fun getActionsByTask(task_uid: Long): LiveData<List<ActionWithTask>>


  @Query(
    "SELECT *, " +
      "(SELECT " +
      " CASE" +
      "  WHEN MAX(timestamp) IS NULL" +
      "  THEN Task.startDate" +
      "  ELSE MAX(timestamp) + frequency" +
      " END" +
      " from `action`" +
      " where task.id = `action`.task_id" +
      ") as due " +
      "FROM Task " +
      "ORDER BY due ASC"
  )
  fun loadTasksAndActions(): LiveData<List<TaskWithActions>>
}
