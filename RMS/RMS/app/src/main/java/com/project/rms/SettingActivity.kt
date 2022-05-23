package com.project.rms

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingActivity :  AppCompatActivity(){

    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContentView(R.layout.setting)

        val voiceset= findViewById<Switch>(R.id.voiceset)
        val voicename= findViewById<EditText>(R.id.voicename)
        val vn_btn= findViewById<Button>(R.id.VN_btn)
        val explain= findViewById<TextView>(R.id.voice_explain)

        val explaintext = "1. 설정한 이름으로 음성인식 비서를 호출해주세요.\n2. 실행시킬 기능 + (실행,등록)을 명령해주세요.\n기능 : 타이머, 레시피, 유튜브, 웹, 식재료 등록\n(타이머 예시) 타이머 1분 실행시켜줘\n" +
                "(식재료 등록 예시) 양파 두개 등록시켜줘"
        explain.setText(explaintext)
        voicename.setText(App.prefs.Voicename)
        vn_btn.setOnClickListener{
            App.prefs.Voicename = voicename.getText().toString()
        }
        voiceset.setChecked(App.prefs.Voiceoption)
        voiceset.setOnCheckedChangeListener{CompoundButton, onSwitch ->
            //  스위치가 켜지면
            if (onSwitch){
                Toast.makeText(applicationContext, "switch on", Toast.LENGTH_SHORT).show()
                App.prefs.Voiceoption = true
                App.prefs.Voicepause=true
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
            //  스위치가 꺼지면
            else{
                Toast.makeText(applicationContext, "switch off", Toast.LENGTH_SHORT).show()
                App.prefs.Voiceoption = false
                App.prefs.Voicepause=false
                //음소거된 볼륨을 되돌려놓음
                val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val muteValue = AudioManager.ADJUST_UNMUTE
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
            }
        }
    }
}