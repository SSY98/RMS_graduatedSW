package com.project.rms.CountdownTimer.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer")
data class ssh_TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    var name: String,
    var time: String
)
