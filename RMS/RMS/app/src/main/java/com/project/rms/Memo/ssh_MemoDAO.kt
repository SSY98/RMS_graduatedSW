package com.project.rms.Memo

import androidx.room.*

@Dao
interface ssh_MemoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: ssh_MemoEntity)

    @Update
    fun update(memo: ssh_MemoEntity)

    @Query("SELECT * FROM memo")
    fun getAll() : MutableList<ssh_MemoEntity>

    @Delete
    fun delete(memo: ssh_MemoEntity)
}