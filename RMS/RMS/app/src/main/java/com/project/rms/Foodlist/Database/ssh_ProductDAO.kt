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

    // 유통기한 마감이 임박한 상위 3개 식재료 검색 (이미지 인식한 과일, 채소류는 식재료명을, 그 외 직접 입력, 바코드 인식한 식재료는 분류명이 리스트에 들어가게함)
    @Query("SELECT CASE WHEN category = '과일' THEN name WHEN category = '채소' THEN name ELSE category END FROM products ORDER BY date")
    fun date(): MutableList<String>
}