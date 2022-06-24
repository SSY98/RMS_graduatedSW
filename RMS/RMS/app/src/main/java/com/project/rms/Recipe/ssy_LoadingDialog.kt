package com.project.rms.Recipe

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.project.rms.R

class ssy_LoadingDialog  constructor(context: Context) : Dialog(context){
    init {
        setCanceledOnTouchOutside(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.ssy_loading_dialog)
    }
}