package com.project.rms.Receipt

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Memo.ssh_MemoUpdateDialogInterface
import com.project.rms.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ssh_ReceiptDialog(context: Context, Interface: ssh_ReceiptDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var ReceiptDialogInterface: ssh_ReceiptDialogInterface = Interface

    lateinit var db3 : ssh_ReceiptDatabase // 영수증 db_ssh
    var ReceiptList = mutableListOf<ssh_ReceiptEntity>() // 영수증 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_receipt)

        db3 = ssh_ReceiptDatabase.getInstance(context)!! // 영수증 db_ssh

        var add_receipt = findViewById<Button>(R.id.receipt_add_btn)
        var cancel_receipt = findViewById<Button>(R.id.receipt_cancel_btn)
        var plus_receipt = findViewById<Button>(R.id.receipt_plus_btn)

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 영수증 db를 불러옴
        CoroutineScope(Dispatchers.IO).launch {
            async{
                ReceiptList = db3.ReceiptDAO().getAll()
                Log.d("Receipt","$ReceiptList")
            }.await()
            CoroutineScope(Dispatchers.Main).launch {
                val recyclerView = findViewById<RecyclerView>(R.id.ReceiptRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(context)
                val adapter = ssh_ReceiptAdapter(ReceiptList)//수정
                recyclerView.adapter = adapter//++
            }
        }
        // 확인 버튼 클릭 시 onReceiptAddButtonClicked 호출 후 종료
        add_receipt.setOnClickListener {
            ReceiptDialogInterface.onReceiptAddButtonClicked()
            dismiss()
        }

        // 취소 버튼 클릭 시 onReceiptCancelButtonClicked 호출 후 종료
        cancel_receipt.setOnClickListener {
            ReceiptDialogInterface.onReceiptCancelButtonClicked()
            dismiss()
        }

        // 추가 버튼 클릭 시 onReceiptPluslButtonClicked 호출 후 종료
        plus_receipt.setOnClickListener {
            ReceiptDialogInterface.onReceiptPlusButtonClicked()
            dismiss()
        }
    }
}