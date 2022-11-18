package com.project.rms.CountdownTimer.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ssh_TimerEntity::class), version = 1)
abstract class ssh_TimerDatabase : RoomDatabase() {
    abstract fun timerDAO() : ssh_TimerDAO

    companion object{
        var INSTANCE : ssh_TimerDatabase? = null

        fun getInstance(context : Context) : ssh_TimerDatabase?{
            if(INSTANCE == null){
                synchronized(ssh_TimerDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        ssh_TimerDatabase::class.java,"timer.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}