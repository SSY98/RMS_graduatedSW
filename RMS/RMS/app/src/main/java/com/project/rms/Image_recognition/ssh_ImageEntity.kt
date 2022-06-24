package com.project.rms.Image_recognition

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image")
data class ssh_ImageEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    var name: String,
    var category: String,
    var date: Int
)