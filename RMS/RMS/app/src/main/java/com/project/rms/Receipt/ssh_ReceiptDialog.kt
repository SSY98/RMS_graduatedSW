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
import com.project.rms.Memo.ssh_MemoAdapter
import com.project.rms.Memo.ssh_MemoEntity
import com.project.rms.Memo.ssh_MemoUpdateDialogInterface
import com.project.rms.R
import com.project.rms.Recipe.ssy_Parts_Litem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ssh_ReceiptDialog(context: Context, Interface: ssh_ReceiptDialogInterface) : Dialog(context), ssh_onReceiptDeleteListener {
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
        getAllReceipt()

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

    // 데이터베이스에 영수증 식재료를 삭제_ssh
    fun deleteReceipt(receipt : ssh_ReceiptEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db3.ReceiptDAO().delete(receipt)
            }.await()
            getAllReceipt()
        }
    }

    // 데이터베이스에 있는 영수증 식재료를 불러옴_ssh
    fun getAllReceipt(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                ReceiptList = db3.ReceiptDAO().getAll()
                Log.d("Receipt","$ReceiptList")
            }.await()
            CoroutineScope(Dispatchers.Main).launch {
                setReceiptRecyclerView(ReceiptList)
            }
        }
    }

    // recyclerview로 데이터베이스에 있는 영수증 식재료 출력_ssh
    fun setReceiptRecyclerView(ReceiptList : MutableList<ssh_ReceiptEntity>) {
        val recyclerView = findViewById<RecyclerView>(R.id.ReceiptRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ssh_ReceiptAdapter(ReceiptList, this)//수정
        recyclerView.adapter = adapter//++
    }

    // 영수증 인식 팝업창에서 삭제 버튼 클릭 시 해당 되는 식재료를 DB 및 목록에서 삭제
    override fun onReceiptDeleteListener(receipt: ssh_ReceiptEntity) {
        deleteReceipt(receipt)
        ReceiptDialogInterface.onReceiptDialogDeleteListener()
    }
}