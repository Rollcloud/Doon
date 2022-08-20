package com.rollcloud.doon

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
  @Insert(onConflict = OnConflictStrategy.ABORT) fun insertTask(task: Task)

  //  @Update fun updateTask(task: Task)

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

  @Query("Select * from `action`") fun getActions(): LiveData<List<ActionWithTask>>

  @Query("SELECT * FROM Task") fun loadTasksAndActions(): LiveData<List<TaskWithActions>>
}
