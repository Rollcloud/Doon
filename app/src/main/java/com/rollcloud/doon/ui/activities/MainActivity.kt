package com.rollcloud.doon.ui.activities

import android.content.Intent
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rollcloud.doon.Constants.EXTRA_TASK_ID
import com.rollcloud.doon.R
import com.rollcloud.doon.data.room.Action
import com.rollcloud.doon.data.room.AppDatabase
import com.rollcloud.doon.data.room.TaskWithActions
import com.rollcloud.doon.ui.adapters.TaskAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.lang.System.currentTimeMillis
import kotlin.math.absoluteValue


class MainActivity : AppCompatActivity() {

  val clock: Clock = Clock.System

  private val actionsTasks = arrayListOf<TaskWithActions>()
  var adapter = TaskAdapter(actionsTasks)

  private var dingPlayer: MediaPlayer? = null
  val db by lazy { AppDatabase.getDatabase(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    installSplashScreen()
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    tasks_RV.apply {
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = this@MainActivity.adapter
    }

    initSwipe()

    db
      .actionDao()
      .loadTasksAndActions()
      .observe(
        this,
        Observer {
          if (!it.isNullOrEmpty()) {
            actionsTasks.clear()
            actionsTasks.addAll(it)
            adapter.notifyDataSetChanged()
            updateScore()
          } else {
            actionsTasks.clear()
            adapter.notifyDataSetChanged()
            updateScore()
          }
        }
      )
  }

  private fun initSwipe() {
    dingPlayer = MediaPlayer.create(this@MainActivity, R.raw.ding)

    val simpleItemTouchCallback =
      object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
          recyclerView: RecyclerView,
          viewHolder: RecyclerView.ViewHolder,
          target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
          val position = viewHolder.adapterPosition

          if (direction == ItemTouchHelper.RIGHT) {
            dingPlayer?.start()
            GlobalScope.launch(Dispatchers.IO) {
              val taskId = adapter.getItemId(position)
              val performedAt = currentTimeMillis()
              val performedAction = Action(taskId, performedAt)
              db.actionDao().insertAction(performedAction)
            }
          }
        }

        override fun onChildDraw(
          canvas: Canvas,
          recyclerView: RecyclerView,
          viewHolder: RecyclerView.ViewHolder,
          dX: Float,
          dY: Float,
          actionState: Int,
          isCurrentlyActive: Boolean
        ) {
          if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            val paint = Paint()
            val icon: Bitmap

            if (dX > 0) {

              icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_check_white_png)

              paint.color = resources.getColor(R.color.completion_green, null)

              canvas.drawRect(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                itemView.left.toFloat() + dX,
                itemView.bottom.toFloat(),
                paint
              )

              canvas.drawBitmap(
                icon,
                itemView.left.toFloat(),
                itemView.top.toFloat() +
                  (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                paint
              )
            }
            viewHolder.itemView.translationX = dX
          } else {
            super.onChildDraw(
              canvas,
              recyclerView,
              viewHolder,
              dX,
              dY,
              actionState,
              isCurrentlyActive
            )
          }
        }
      }

    val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
    itemTouchHelper.attachToRecyclerView(tasks_RV)
  }

  fun updateScore() {
    val now: Instant = clock.now()
    val taskScores =
      actionsTasks.map {
        it.getDaysTillDue().coerceAtMost(0) - (it.getLastDueDelta()?:0).coerceAtMost(0)
      }
    val totalScore = taskScores.sum()
    //    showScore.text = totalScore.toHoursAndDays(signed = true)
    showScore.text = "${totalScore.absoluteValue} task⋅days"
    showScore.setTextColor(
      resources.getColor(
        if (totalScore >= 0) R.color.completion_green else R.color.deletion_red,
        null
      )
    )
    scoreDescription.text =
      resources.getString(
        if (totalScore >= 0) R.string.score_label_pos else R.string.score_label_neg
      )
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    val item = menu.findItem(R.id.search)
    val searchView = item.actionView as SearchView
    item.setOnActionExpandListener(
      object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
          displayTask()
          return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
          displayTask()
          return true
        }
      }
    )
    searchView.setOnQueryTextListener(
      object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
          return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
          if (!newText.isNullOrEmpty()) {
            displayTask(newText)
          }
          return true
        }
      }
    )

    return super.onCreateOptionsMenu(menu)
  }

  fun displayTask(newText: String = "") {
    db
      .actionDao()
      .loadTasksAndActions()
      .observe(
        this,
        Observer {
          if (it.isNotEmpty()) {
            actionsTasks.clear()
            actionsTasks.addAll(
              it.filter { actionsTask -> actionsTask.task.name.contains(newText, true) }
            )
            adapter.notifyDataSetChanged()
            updateScore()
          }
        }
      )
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.history -> {
        startActivity(Intent(this, HistoryActivity::class.java))
      }
    }
    return super.onOptionsItemSelected(item)
  }

  fun openNewTask(view: View) {
    startActivity(Intent(this, NewTaskActivity::class.java))
  }

  fun openEditTask(view: View) {
    val position = tasks_RV.getChildAdapterPosition(view)
    val taskId = adapter.getItemId(position)
    val intent = Intent(this, NewTaskActivity::class.java).apply { putExtra(EXTRA_TASK_ID, taskId) }
    startActivity(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    dingPlayer?.release()
  }
}
