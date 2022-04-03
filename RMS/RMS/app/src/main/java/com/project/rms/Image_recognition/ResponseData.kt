package com.project.rms.Image_recognition

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("class_name")
    var class_name: String? = null
    )
