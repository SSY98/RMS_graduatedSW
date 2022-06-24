package com.project.rms.Image_recognition

import androidx.room.*
import com.project.rms.Receipt.ssh_ReceiptEntity

@Dao
interface ssh_ImageDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: ssh_ImageEntity)

    @Update
    fun update(image: ssh_ImageEntity)

    @Delete
    fun delete(image: ssh_ImageEntity)

    @Query("SELECT * FROM image")
    fun getAll(): MutableList<ssh_ImageEntity>
}