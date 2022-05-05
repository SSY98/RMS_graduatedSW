package com.project.rms

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingActivity :  AppCompatActivity(){

    var pro: ProgressDialog? = null

    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContentView(R.layout.setting)

        val customdialog= findViewById<Button>(R.id.btn2)
        val voiceset= findViewById<Button>(R.id.voiceset)
        val voicename= findViewById<EditText>(R.id.voicename)
        val vn_btn= findViewById<Button>(R.id.VN_btn)

        voicename.setText(App.prefs.Voicename)
        vn_btn.setOnClickListener{
            App.prefs.Voicename = voicename.getText().toString()
        }

        voiceset.setOnClickListener {
            if(App.prefs.Voiceoption==false){
                App.prefs.Voiceoption=true
                App.prefs.Voicepause=true
                Log.d("옵션", "true")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                App.prefs.Voiceoption=false
                App.prefs.Voicepause=false
                Log.d("옵션", "false")
                //음소거된 볼륨을 되돌려놓음
                val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val muteValue = AudioManager.ADJUST_UNMUTE
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
                //음소거된 볼륨을 되돌려놓음
            }
        }

        // 커스텀 다이얼로그
        customdialog.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("커스텀 다이얼로그")
            builder.setIcon(R.mipmap.ic_launcher)

            var v1 = layoutInflater.inflate(R.layout.dialog, null)
            builder.setView(v1)

            // p0에 해당 AlertDialog가 들어온다. findViewById를 통해 view를 가져와서 사용
            var listener = DialogInterface.OnClickListener { p0, p1 ->
                var alert = p0 as AlertDialog
                var edit1: EditText? = alert.findViewById<EditText>(R.id.editText)
                var edit2: EditText? = alert.findViewById<EditText>(R.id.editText2)

                //tv1.text = "${edit1?.text}"
                //tv1.append("${edit2?.text}")
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)

            builder.show()
        }
    }


}