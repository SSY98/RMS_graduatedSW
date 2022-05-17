package com.project.rms.Receipt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipt")
data class ssh_ReceiptEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    var name: String,
    var category: String,
    var date: String,
    var count: String
)