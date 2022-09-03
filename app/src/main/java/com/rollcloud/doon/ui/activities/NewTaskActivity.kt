package com.rollcloud.doon.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.rollcloud.doon.Constants.DATE_FORMAT
import com.rollcloud.doon.Constants.EXTRA_TASK_ID
import com.rollcloud.doon.R
import com.rollcloud.doon.data.room.AppDatabase
import com.rollcloud.doon.data.room.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import kotlinx.android.synthetic.main.activity_task_new.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewTaskActivity : AppCompatActivity(), View.OnClickListener {

  lateinit var myCalendar: Calendar
  lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

  private val db by lazy { AppDatabase.getDatabase(this) }

  private val sdf = SimpleDateFormat(DATE_FORMAT)

  private val now = System.currentTimeMillis()
  private var startDate: Long = now

  private var taskId = -1L
  private var isNew = true

  override fun onCreate(savedInstanceState: Bundle?) {
    /* Creates UI. */
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_new)

    taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
    isNew = taskId == -1L

    dateEdt.setOnClickListener(this)
    saveBtn.setOnClickListener(this)

    setDefaults()
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.dateEdt -> {
        setListener()
      }
      R.id.saveBtn -> {
        saveTodo()
      }
    }
  }

  private fun setDefaults() {
    if (isNew) {
      toolbarAddTask.title = getString(R.string.new_task)
      saveBtn.text = getString(R.string.save_new_task)
      dateEdt.setText(sdf.format(now))
      nameInputLayout.editText?.requestFocus()
    } else {
      toolbarAddTask.title = getString(R.string.edit_task)
      saveBtn.text = getString(R.string.save_edit_task)

      GlobalScope.launch(Dispatchers.Main) {
        val task =
          withContext(Dispatchers.IO) {
            return@withContext db.taskDao().getTask(taskId)
          }
        nameInputLayout.editText?.setText(task.name)
        dateEdt.isEnabled = false
        dateEdt.setText(sdf.format(task.startDate))
        frequencyInputLayout.editText?.setText(
          task.frequency.toDuration(MILLISECONDS).inWholeDays.toString()
        )
      }
    }
  }

  private fun saveTodo() {
    /* Retrieves values from UI. */
    val title = nameInputLayout.editText?.text.toString().trim()
    val frequencyDays = frequencyInputLayout.editText?.text.toString().toInt()
    val frequency: Long = frequencyDays.toDuration(DAYS).toLong(MILLISECONDS)

    GlobalScope.launch(Dispatchers.Main) {
      withContext(Dispatchers.IO) {
        if (isNew) {
          val task = Task(title, startDate, frequency, 1)
          return@withContext db.taskDao().insertTask(task)
        } else {
          val task = Task(title, startDate, frequency, 1, taskId)
          return@withContext db.taskDao().updateTask(task)
        }
      }
      finish()
    }
  }

  private fun setListener() {
    myCalendar = Calendar.getInstance()

    dateSetListener =
      DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, month)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDate()
      }

    val datePickerDialog =
      DatePickerDialog(
        this,
        dateSetListener,
        myCalendar.get(Calendar.YEAR),
        myCalendar.get(Calendar.MONTH),
        myCalendar.get(Calendar.DAY_OF_MONTH)
      )
    datePickerDialog.show()
  }

  private fun updateDate() {
    // Mon, 5 Jan 2020
    startDate = myCalendar.time.time
    dateEdt.setText(sdf.format(startDate))
  }
}
