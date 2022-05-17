package com.project.rms.Receipt

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ssh_ReceiptEntity::class), version = 1)
abstract class ssh_ReceiptDatabase : RoomDatabase() {
    abstract fun ReceiptDAO() : ssh_ReceiptDAO

    companion object{
        var INSTANCE : ssh_ReceiptDatabase? = null

        fun getInstance(context: Context) : ssh_ReceiptDatabase?{
            if(INSTANCE == null){
                synchronized(ssh_ReceiptDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ssh_ReceiptDatabase::class.java,"receipt.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}