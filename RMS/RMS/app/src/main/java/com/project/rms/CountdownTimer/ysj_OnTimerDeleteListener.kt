package com.project.rms.CountdownTimer

import com.project.rms.CountdownTimer.Database.ssh_TimerEntity

interface ysj_OnTimerDeleteListener {
    fun onTimerDeleteListener(timer : ssh_TimerEntity)
}