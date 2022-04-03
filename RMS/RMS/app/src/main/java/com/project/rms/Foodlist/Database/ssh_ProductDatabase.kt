package com.project.rms.Foodlist.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ssh_ProductEntity::class), version = 1)
abstract class ssh_ProductDatabase : RoomDatabase() {
    abstract fun productDAO() : ssh_ProductDAO

    companion object{
        var INSTANCE : ssh_ProductDatabase? = null

        fun getInstance(context : Context) : ssh_ProductDatabase?{
            if(INSTANCE == null){
                synchronized(ssh_ProductDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ssh_ProductDatabase::class.java,"product.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}