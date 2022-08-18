package com.rollcloud.doon

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoModel(
    var title:String,
    var date:Long,
    var time:Long,
    var isFinished : Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
)