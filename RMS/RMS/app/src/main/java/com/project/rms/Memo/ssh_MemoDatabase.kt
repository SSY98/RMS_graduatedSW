package com.project.rms.Memo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ssh_MemoEntity::class), version = 1)
abstract class ssh_MemoDatabase : RoomDatabase() {
    abstract fun memoDAO() : ssh_MemoDAO

    companion object{
        var INSTANCE : ssh_MemoDatabase? = null

        fun getInstance(context : Context) : ssh_MemoDatabase?{
            if(INSTANCE == null){
                synchronized(ssh_MemoDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ssh_MemoDatabase::class.java,"memo.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}