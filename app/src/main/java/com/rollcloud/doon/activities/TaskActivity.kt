package com.rollcloud.doon.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.rollcloud.doon.AppDatabase
import com.rollcloud.doon.MILLIS_PER_DAY
import com.rollcloud.doon.R
import com.rollcloud.doon.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_task_new.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DB_NAME = "doon.db"

class TaskActivity : AppCompatActivity(), View.OnClickListener {

  lateinit var myCalendar: Calendar
  lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

  var startDate = 0L

  val db by lazy { AppDatabase.getDatabase(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    /* Creates UI. */
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_new)

    dateEdt.setOnClickListener(this)
    saveBtn.setOnClickListener(this)
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

  private fun saveTodo() {
    /* Retrieves values from UI. */
    val title = nameInputLayout.editText?.text.toString()
    val frequency = frequencyInputLayout.editText?.text.toString().toLong() * MILLIS_PER_DAY

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

  private fun roundToLower(value: Long, step: Long): Long {
    return Math.floorDiv(value, step) * step
  }

  private fun updateDate() {
    // Mon, 5 Jan 2020
    val myFormat = "EEE, d MMM yyyy"
    val sdf = SimpleDateFormat(myFormat)
    startDate = roundToLower(myCalendar.time.time, MILLIS_PER_DAY)
    dateEdt.setText(sdf.format(startDate))
  }
}
