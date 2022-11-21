package com.project.rms.CountdownTimer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.CountdownTimer.Database.ssh_TimerEntity
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_item_memo.view.*
import kotlinx.android.synthetic.main.ysj_list_item_hori.view.*
import java.text.SimpleDateFormat

class ysj_ListAdapterHorizontal(var list: MutableList<ssh_TimerEntity>,
                                var ysj_onTimerDeleteListener: ysj_OnTimerDeleteListener):
    RecyclerView.Adapter<ysj_ListAdapterHorizontal.ViewHolder>(){


    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ysj_list_item_hori, parent, false)
    ) {
        val timeranme: TextView = itemView.findViewById(R.id.timer_name)
        val timertime: TextView = itemView.findViewById(R.id.timer_time)

        val root = itemView.TimerRoot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val alltimer = list[position]
        val alltimername = alltimer.name
        val alltimertime = alltimer.time

        val timemin = alltimertime.toInt()/60
        val timesec = alltimertime.toInt()%60

        val sumtime = timemin.toString()+"분 "+timesec.toString()+"초"

        holder.timeranme.text = alltimername
        holder.timertime.text = sumtime


        holder.root.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                val alltimer = list[position]
                ysj_onTimerDeleteListener.onTimerDeleteListener(alltimer)
                return true
            }
        })

        holder.root.setOnClickListener{}

    }

    override fun getItemCount(): Int {
        return list.size
    }
}