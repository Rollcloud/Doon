package com.rollcloud.doon.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.rollcloud.doon.R
import com.rollcloud.doon.data.room.ActionWithTask
import com.rollcloud.doon.data.room.AppDatabase
import com.rollcloud.doon.ui.adapters.ActionsAdapter
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_main.toolbar

class HistoryActivity : AppCompatActivity() {
  private val modelList = arrayListOf<ActionWithTask>()
  private var adapter = ActionsAdapter(modelList)

  private val db by lazy { AppDatabase.getDatabase(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    /* Creates UI. */
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history)

    setSupportActionBar(toolbar)
    completed_RV.apply {
      layoutManager = LinearLayoutManager(this@HistoryActivity)
      adapter = this@HistoryActivity.adapter
    }

    db
      .actionDao()
      .getActions()
      .observe(
        this,
        Observer {
          if (!it.isNullOrEmpty()) {
            modelList.clear()
            modelList.addAll(it)
            adapter.notifyDataSetChanged()
          } else {
            modelList.clear()
            adapter.notifyDataSetChanged()
          }
        }
      )
  }

  fun displayPerformed(newText: String = "") {
    db
      .actionDao()
      .getActions()
      .observe(
        this,
        Observer {
          if (it.isNotEmpty()) {
            modelList.clear()
            modelList.addAll(
              it.filter { taskAction -> taskAction.task.name.contains(newText, true) }
            )
            adapter.notifyDataSetChanged()
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
}
