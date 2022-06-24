package com.project.rms.Image_recognition

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.rms.Receipt.ssh_ReceiptDAO
import com.project.rms.Receipt.ssh_ReceiptDatabase
import com.project.rms.Receipt.ssh_ReceiptEntity

@Database(entities = arrayOf(ssh_ImageEntity::class), version = 1)
abstract class ssh_ImageDatabase:  RoomDatabase() {
    abstract fun ImageDAO() : ssh_ImageDAO

    companion object{
        var INSTANCE : ssh_ImageDatabase? = null

        fun getInstance(context: Context) : ssh_ImageDatabase?{
            if(INSTANCE == null){
                synchronized(ssh_ImageDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ssh_ImageDatabase::class.java,"image.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}