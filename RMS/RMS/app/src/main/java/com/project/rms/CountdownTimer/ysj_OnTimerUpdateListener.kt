package com.project.rms.CountdownTimer

import com.project.rms.CountdownTimer.Database.ssh_TimerEntity

interface ysj_OnTimerUpdateListener {
    fun onTimerUpdateListener(timer : ssh_TimerEntity)
}