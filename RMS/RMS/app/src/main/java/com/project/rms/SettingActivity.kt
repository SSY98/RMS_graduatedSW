package com.project.rms

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
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