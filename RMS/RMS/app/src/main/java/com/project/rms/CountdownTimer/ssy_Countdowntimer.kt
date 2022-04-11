package com.project.rms.CountdownTimer

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mut_jaeryo.circletimer.CircleTimer
import com.project.rms.App
import com.project.rms.R

class ssy_Countdowntimer : AppCompatActivity(){
    private var soundPool = SoundPool.Builder().build()
    private var my_sound = 0
    var loaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssy_activity_timer)

        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        my_sound = soundPool.load(this, R.raw.timer_beep, 1)
        soundPool.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
            override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
                loaded = true
            }
        })

        val timer = findViewById<CircleTimer>(R.id.main_timer)
        timer.setMaximumTime(3600)
        /*
        if(음성인식으로 불러와졌다면(boolean)){
            timer.start()
        }
         */
        timer.setInitPosition(App.prefs.TimerSecond) //쉐어드프리펀스 사용

        val start = findViewById<Button>(R.id.start)
        val stop = findViewById<Button>(R.id.stop)
        val reset = findViewById<Button>(R.id.reset)
        start.setOnClickListener{ timer.start()}
        stop.setOnClickListener{ timer.stop() }
        reset.setOnClickListener{ timer.reset() }

        //타이머가 끝나면 비프음 출력
        timer.setBaseTimerEndedListener(object: CircleTimer.baseTimerEndedListener {
            override fun OnEnded() {
                val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val muteValue = AudioManager.ADJUST_UNMUTE
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
                if (loaded) {
                    soundPool.play(my_sound,  0.6f, 0.6f, 1, 3, 1f);
                }
            }
        })
    }
}