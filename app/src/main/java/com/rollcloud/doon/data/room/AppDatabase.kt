package com.rollcloud.doon.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rollcloud.doon.data.room.Action
import com.rollcloud.doon.data.room.ActionDao
import com.rollcloud.doon.data.room.Task
import com.rollcloud.doon.data.room.TaskDao

const val DB_NAME = "doon.db"

@Database(entities = [Task::class, Action::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao
  abstract fun actionDao(): ActionDao

  companion object {
    // Singleton prevents multiple instances of database opening at the
    // same time.
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }
      synchronized(this) {
        val instance =
          Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME).build()
        INSTANCE = instance
        return instance
      }
    }
  }
}
