package com.project.rms.CountdownTimer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.project.rms.App
import com.project.rms.R

class ysj_TimerDialog(context: Context, Interface:ysj_TimerDialogInterface) : Dialog(context){

    private var TimerDialogInterface : ysj_TimerDialogInterface = Interface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ysj_dialog_addtimer)

        var timer_name = findViewById<EditText>(R.id.timer_name_edt)
        var timer_min = findViewById<EditText>(R.id.timer_min_edt)
        var timer_sec = findViewById<EditText>(R.id.timer_sec_edt)
        var timer_add = findViewById<Button>(R.id.timer_add_btn)
        var timer_cancel = findViewById<Button>(R.id.timer_cancel_btn)

        // 추가 버튼 클릭 시 edittext에 입력된 타이머 정보를 SharedPreference 변수에 저장하고 onAddtButtonClicked 호출 후 종료
        timer_add.setOnClickListener {
            var timermin = timer_min.text.toString()
            var timersec = timer_sec.text.toString()
            val timertime = timermin.toInt() * 60 + timersec.toInt()

            App.prefs.Timername = timer_name.text.toString()
            App.prefs.Timertime = timertime.toString()

            TimerDialogInterface.onAddButtonClicked()
            dismiss()
        }

        // 취소 버튼 클릭 시 onCancelButtonClicked 호출 후 종료
        timer_cancel.setOnClickListener {
            TimerDialogInterface.onCancelButtonClicked()
            dismiss()}
    }
}