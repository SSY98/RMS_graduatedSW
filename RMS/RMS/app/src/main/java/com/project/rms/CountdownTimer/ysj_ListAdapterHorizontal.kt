package com.project.rms.CountdownTimer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.R
import kotlinx.android.synthetic.main.ysj_list_item_hori.view.*

class ysj_ListAdapterHorizontal(var list: ArrayList<ysj_TimerModel>):
    RecyclerView.Adapter<ysj_ListAdapterHorizontal.ViewHolder>(){

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ysj_list_item_hori, parent, false)
    ) {
        val timeranme: TextView = itemView.findViewById(R.id.timer_name)
        val timertime: TextView = itemView.findViewById(R.id.timer_time)
    }

    class ListAdapter(val layout: View): RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val alltimer = list[position]
        val alltimername = alltimer.name
        val alltimertime = alltimer.time

        holder.timeranme.text = alltimername
        holder.timertime.text = alltimertime

    }

    override fun getItemCount(): Int {
        return list.size
    }
}