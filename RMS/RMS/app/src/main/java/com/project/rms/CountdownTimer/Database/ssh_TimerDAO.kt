package com.project.rms.CountdownTimer.Database

import androidx.room.*

@Dao
interface ssh_TimerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(timer: ssh_TimerEntity)

    @Delete
    fun delete(timer: ssh_TimerEntity)

    @Query("SELECT * FROM timer")
    fun getAll(): MutableList<ssh_TimerEntity>
}