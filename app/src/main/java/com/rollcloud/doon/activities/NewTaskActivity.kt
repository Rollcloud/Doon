package com.rollcloud.doon.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.rollcloud.doon.AppDatabase
import com.rollcloud.doon.Constants
import com.rollcloud.doon.R
import com.rollcloud.doon.Task
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

  private val sdf = SimpleDateFormat(Constants.dateFormat)

  private val now = System.currentTimeMillis()
  private var startDate: Long = now

  private val bundle = intent.extras
  private val taskId = bundle?.getLong("TaskId")
  private val isNew = taskId == null

  override fun onCreate(savedInstanceState: Bundle?) {
    /* Creates UI. */
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_new)

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
      toolbarAddTask.title = R.string.new_task.toString()
      saveBtn.text = R.string.save_new_task.toString()
      dateEdt.setText(sdf.format(now))
      nameInputLayout.editText?.requestFocus()
    } else {
      toolbarAddTask.title = R.string.edit_task.toString()
      saveBtn.text = R.string.save_edit_task.toString()

      GlobalScope.launch(Dispatchers.Main) {
        val task = withContext(Dispatchers.IO) {
          return@withContext db.taskDao().getTask(taskId!!)
        }

        nameInputLayout.editText?.setText(task.name)
        dateEdt.isEnabled = false
        dateEdt.setText(sdf.format(task.startDate))
        frequencyInputLayout.editText?.setText(task.frequency.toDuration(MILLISECONDS).inWholeDays.toString())

        finish()
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
        return@withContext db.taskDao().insertTask(Task(title, startDate, frequency, 1))
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
