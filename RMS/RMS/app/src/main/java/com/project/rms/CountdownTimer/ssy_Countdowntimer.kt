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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        timer.setInitPosition(App.prefs.TimerSecond) //타이머의 시간 설정

        val start = findViewById<Button>(R.id.start)
        val stop = findViewById<Button>(R.id.stop)
        val reset = findViewById<Button>(R.id.reset)

        if(App.prefs.Voicereq == true){ //음성인식으로 타이머 실행시켰을때
            timer.start()
            App.prefs.Voicepause = false
            App.prefs.Voicereq = false
        }

        start.setOnClickListener{
            timer.start()
            App.prefs.Voicepause = false //타이머가 돌아가는동안 음성인식 중단
        }

        stop.setOnClickListener{
            timer.stop()
            App.prefs.Voicepause = true //타이머가 끝나면 음성인식 시작
        }
        reset.setOnClickListener{
            timer.reset()
            App.prefs.Voicepause = true //타이머가 끝나면 음성인식 시작
        }

        //타이머가 끝나면 비프음 출력
        timer.setBaseTimerEndedListener(object: CircleTimer.baseTimerEndedListener {
            override fun OnEnded() {
                val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val muteValue = AudioManager.ADJUST_UNMUTE
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
                if (loaded) {
                    soundPool.play(my_sound,  0.6f, 0.6f, 1, 3, 1f);
                }
                GlobalScope.launch(Dispatchers.Main) {
                    delay(20000)
                    App.prefs.Voicepause = true //타이머가 끝나면 음성인식 시작
                }
            }
        })
    }
}