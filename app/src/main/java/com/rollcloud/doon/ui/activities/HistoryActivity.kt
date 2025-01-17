package com.rollcloud.doon.ui.activities

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.rollcloud.doon.ActionDecorator
import com.rollcloud.doon.Constants
import com.rollcloud.doon.R
import com.rollcloud.doon.TodayDecorator
import com.rollcloud.doon.data.room.*
import com.rollcloud.doon.ui.adapters.ActionsAdapter
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_history.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_task_new.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class HistoryActivity : AppCompatActivity() {

  private val modelList = arrayListOf<ActionWithTask>()
  private var adapter = ActionsAdapter(modelList)

  private val db by lazy { AppDatabase.getDatabase(this) }

  private var taskId = -1L

  private val clock: Clock = Clock.System
  private val localTimeZone = TimeZone.currentSystemDefault()
  private val today = clock.todayIn(localTimeZone)

  private fun epochSecondsToCalendarDay(timestamp: Long, tz: TimeZone): CalendarDay {
    val localDate = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC).toLocalDate()
    return CalendarDay.from(localDate.year, localDate.monthValue, localDate.dayOfMonth)
  }

  private fun getActionsFromDB(taskId: Long): LiveData<List<ActionWithTask>> {
    return if (taskId > -1) db.actionDao().getActionsByTask(taskId)
    else {
      db.actionDao().getActions()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    /* Creates UI. */
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history)

    taskId = intent.getLongExtra(Constants.EXTRA_TASK_ID, -1L)

    setSupportActionBar(toolbar)
    completed_RV.apply {
      layoutManager = LinearLayoutManager(this@HistoryActivity)
      adapter = this@HistoryActivity.adapter
    }

    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
    val primaryColor = typedValue.data

    calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
    var actionDates: List<CalendarDay> = emptyList()

    getActionsFromDB(taskId)
      .observe(
        this@HistoryActivity,
        Observer { list ->
          if (!list.isNullOrEmpty()) {
            actionDates =
              list.map { actionWithTask ->
                epochSecondsToCalendarDay(actionWithTask.action.timestamp, localTimeZone)
              }
            list.forEach {
              calendarView.addDecorators(
                ActionDecorator(it.task.getColour(this@HistoryActivity), actionDates.toHashSet())
              )
            }
            calendarView.addDecorators(TodayDecorator(primaryColor))
            modelList.clear()
            modelList.addAll(list)
            adapter.notifyDataSetChanged()
          } else {
            modelList.clear()
            adapter.notifyDataSetChanged()
          }
        }
      )
  }
}
