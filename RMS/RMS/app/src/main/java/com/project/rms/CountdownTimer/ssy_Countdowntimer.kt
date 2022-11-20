package com.project.rms.CountdownTimer

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mut_jaeryo.circletimer.CircleTimer
import com.project.rms.App
import com.project.rms.CountdownTimer.Database.ssh_TimerDatabase
import com.project.rms.CountdownTimer.Database.ssh_TimerEntity
import com.project.rms.Memo.ssh_MemoAdapter
import com.project.rms.R
import com.project.rms.databinding.SsyActivityTimerBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ssy_activity_timer.*
import kotlinx.coroutines.*

class ssy_Countdowntimer : AppCompatActivity(), ysj_TimerDialogInterface{
    private var soundPool = SoundPool.Builder().build()
    private var my_sound = 0
    var loaded = false
    var timerList = arrayListOf<ysj_TimerModel>()

    lateinit var db : ssh_TimerDatabase // 타이머 db_ssh
    var TimerList = mutableListOf<ssh_TimerEntity>() // 타이머 목록_ssh


    // ViewBinding
    lateinit var binding : SsyActivityTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssy_activity_timer)

        db = ssh_TimerDatabase.getInstance(this)!! // 타이머 db_ssh

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

        val start = findViewById<ImageButton>(R.id.start)
        val stop = findViewById<ImageButton>(R.id.stop)
        val reset = findViewById<ImageButton>(R.id.reset)
        val addtimer = findViewById<ImageButton>(R.id.addtimer)// + 버튼

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

        //추가 '+' 버튼 누르면 팝업창 출력_ysj
        addtimer.setOnClickListener{
            App.prefs.Timername = ""
            App.prefs.Timertime = ""

            val TimerDialog = ysj_TimerDialog(this,this)
            TimerDialog.show()
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


        //리사이클러뷰
        var manager02 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        var adapter02 = ysj_ListAdapterHorizontal(timerList)

        var RecyclerView02 = recyclerHorizon.apply {
            adapter = adapter02
            layoutManager = manager02
        }

        /* 타이머 추가 코드_ssh
        val timerdata = ssh_TimerEntity(null, "타이머 이름 데이터", "타이머 시간 데이터")
        // edittext_memo.setText("")
        insertTimer(timerdata)*/

    }

    // '+' 팝업창에서 확인 버튼 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가된다._ysj
    override fun onAddButtonClicked() {
        var timername = App.prefs.Timername.toString()
        val timertime = App.prefs.Timertime.toString()

        val timer = ssh_TimerEntity(null, timername, timertime)
        insertTimer(timer)

        App.prefs.Timername = ""
        App.prefs.Timertime = ""
    }

    // '+' 팝업창에서 취소 버튼을 누르면 SharedPreference 변수에 저장된 내용 초기화 (edittext 초기화)_ysj
    override fun onCancelButtonClicked() {
        App.prefs.Timername = ""
        App.prefs.Timertime = ""
    }


    // 데이터베이스에 타이머 추가
    fun insertTimer(timer : ssh_TimerEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.timerDAO().insert(timer)
            }.await()
            getAllTimer()
        }
    }

    // 데이터베이스에 있는 타이머를 불러옴_ssh
    fun getAllTimer(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                TimerList = db.timerDAO().getAll()
                Log.d("timer","$TimerList")
            }.await()
            CoroutineScope(Dispatchers.Main).launch {
            }
        }
    }

    // 데이터베이스에 있는 타이머를 삭제_ssh
    fun deleteTimer(timer: ssh_TimerEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.timerDAO().delete(timer)
            }.await()
            getAllTimer()
        }
    }
}