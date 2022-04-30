package com.project.rms.Memo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.project.rms.App
import com.project.rms.R

class ssh_MemoUpdateDialog(context: Context, Interface: ssh_MemoUpdateDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var MemoUpdateDialogInterface: ssh_MemoUpdateDialogInterface = Interface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_update_memo)

        var update_memo = findViewById<EditText>(R.id.update_memo)
        var update_memo_btn = findViewById<Button>(R.id.update_memo_btn)
        var cancel_memo_btn = findViewById<Button>(R.id.cancel_memo_btn)

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val currentMemo = App.prefs.MemoContents

        // 메모를 화면에 출력
        update_memo.setText(currentMemo)

        // 수정 버튼 클릭 시 onUpdateButtonClicked 호출 후 종료
        update_memo_btn.setOnClickListener {
            App.prefs.MemoContents = update_memo.text.toString()
            MemoUpdateDialogInterface.onMemoUpdateButtonClicked()
            dismiss()
        }

        // 취소 버튼 클릭 시 onCancelButtonClicked 호출 후 종료
        cancel_memo_btn.setOnClickListener {
            Log.d("memo", "hi2")
            MemoUpdateDialogInterface.onMemoCancelButtonClicked()
            dismiss()
        }
    }
}