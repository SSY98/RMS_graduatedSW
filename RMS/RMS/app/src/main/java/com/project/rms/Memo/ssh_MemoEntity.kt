package com.project.rms.Memo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo")
data class ssh_MemoEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Long?,
    var memo: String = "")
