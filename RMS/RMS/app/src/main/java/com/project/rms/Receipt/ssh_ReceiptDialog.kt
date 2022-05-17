package com.project.rms.Receipt

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Memo.ssh_MemoUpdateDialogInterface
import com.project.rms.R

class ssh_ReceiptDialog(context: Context, Interface: ssh_ReceiptDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var ReceiptDialogInterface: ssh_ReceiptDialogInterface = Interface

    lateinit var db3 : ssh_ReceiptDatabase // 영수증 db_ssh
    var ReceiptList = mutableListOf<ssh_ReceiptEntity>() // 영수증 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_update_memo)

        db3 = ssh_ReceiptDatabase.getInstance(context)!! // 영수증 db_ssh

        var add_receipt = findViewById<Button>(R.id.receipt_add_btn)
        var cancel_receipt = findViewById<Button>(R.id.receipt_cancel_btn)
        var plus_receipt = findViewById<Button>(R.id.receipt_plus_btn)

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}