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

    // 유통기한 마감이 임박한 상위 3개 식재료 검색
    @Query("SELECT category FROM products ORDER BY date LIMIT 3")
    fun date(): MutableList<String>
}