package com.project.rms.Foodlist.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ssh_ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    var name: String,
    var category: String,
    var date: String,
    var count: String
)
