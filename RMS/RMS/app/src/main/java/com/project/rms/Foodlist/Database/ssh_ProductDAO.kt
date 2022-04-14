package com.project.rms.Foodlist.Database

import androidx.room.*

@Dao
interface ssh_ProductDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: ssh_ProductEntity)

    @Update
    fun update(product: ssh_ProductEntity)

    @Delete
    fun delete(product: ssh_ProductEntity)

    @Query("SELECT * FROM products ORDER BY date")
    fun getAll(): MutableList<ssh_ProductEntity>
}