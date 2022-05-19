package com.project.rms.Receipt

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.R

class ssh_ReceiptAdapter(var list : MutableList<ssh_ReceiptEntity>, var ssh_onReceiptDeleteListener: ssh_onReceiptDeleteListener) : RecyclerView.Adapter<ssh_ReceiptAdapter.ViewHolder>(){
    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_receipt, parent, false)
    ) {
        val receiptname: TextView = itemView.findViewById(R.id.receipt_name)
        val receiptcategory: TextView = itemView.findViewById(R.id.receipt_category)
        val receiptdate: TextView = itemView.findViewById(R.id.receipt_date)
        val receiptcount: TextView = itemView.findViewById(R.id.receipt_count)
        val receiptdelete: Button = itemView.findViewById(R.id.receipt_delete_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //position = 순서
        val receipt = list[position]
        val receipt_name = receipt.name
        val receipt_category = receipt.category
        val receipt_date = receipt.date
        val receipt_count = receipt.count

        holder.receiptname.text = receipt_name
        holder.receiptcategory.text = receipt_category
        holder.receiptdate.text = receipt_date
        holder.receiptcount.text = receipt_count


        // 삭제 버튼을 클릭하면 영수증 DB에서 해당 식재료 삭제
        holder.receiptdelete.setOnClickListener {
            ssh_onReceiptDeleteListener.onReceiptDeleteListener(receipt) //  OnMemoUpdateListener 실행 (Listener는 수정 화면 dialog 창을 띄움)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}