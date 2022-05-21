package com.project.rms.Receipt

import androidx.room.*

@Dao
interface ssh_ReceiptDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(receipt: ssh_ReceiptEntity)

    @Update
    fun update(receipt: ssh_ReceiptEntity)

    @Delete
    fun delete(receipt: ssh_ReceiptEntity)

    @Query("SELECT * FROM receipt ORDER BY date")
    fun getAll(): MutableList<ssh_ReceiptEntity>

    @Query("DELETE FROM receipt ")
    fun deleteAll()
}