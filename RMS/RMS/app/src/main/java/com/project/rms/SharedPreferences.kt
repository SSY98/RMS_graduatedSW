package com.project.rms

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences (context: Context) {
    private val prefsFilename = "prefs"
    // private val 프리퍼런스 변수이름 = "다른액티비티에서 사용할 이름"
    private val recipe_name = "Recipe_Name" //레시피에 사용
    private val recipe_seq = "Recipe_Seq" //레시피에 사용
    private val food_id = "FoodID" // 식재료 ID
    private val food_name = "FoodName" // 식재료 이름
    private val food_category = "FoodCategory" // 식재료 목록
    private val food_date = "FoodDate" // 식재료 유통기한
    private val food_year = "FoodYear" // 식재료 유통기한
    private val food_month = "FoodMonth" // 식재료 유통기한
    private val food_day = "FoodDay" // 식재료 유통기한
    private val food_count = "FoodCount" // 식재료 개수

    private val memo_id = "MemoID" // 메모 ID
    private val memo_contents = "MemoContents" // 메모 내용

    private val dialog_button = "Dbtn" // 메모 내용

    private val receipt_name = "ReceiptName" // 영수증 식재료 이름
    private val receipt_category = "ReceiptCategory" // 영수증 식재료 종류
    private val receipt_date = "ReceiptDate" // 영수증 식재료 유통기한
    private val receipt_count = "ReceiptCount" // 영수증 식재료 개수

    //타이머에 사용
    private val timer_name = "Timername"//타이머 이름
    private val timer_min = "Timermin"//타이머 분
    private val timer_sec = "Timersec"//타이머 초
    private val timer_sumtime = "Timersumtime"//타이머 총합

    //음성인식에 사용
    private val voice_name = "Voicename" //음성인식 호출이름
    private val voice_option = "Voiceoption" //음성인식 on/off setting
    private val voice_ans = "Voiceanswer" //음성인식의 대답형태
    private val voice_pause = "Voicepause" //음성인식 잠깐 멈출때사용(ex) 타이머
    private val voice_request = "Voicereq" //음성인식으로 실행시킨 액티비티에서 사용
    private val voice_time = "Voicetime" //음성인식 tts시간

    private val web_site = "WebSite" //뉴스피드, 웹서핑, 유튜브에 사용
    private val timer_s = "TimerSecond" //타이머에 사용

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, Context.MODE_PRIVATE)

    // get() 저장된 값을 반환, 기본 값은 "", set(value)은 value 값을 대체한 후 저장
    var Recipe_Seq: String?
        get() = prefs.getString(recipe_seq, "")
        set(value) = prefs.edit().putString(recipe_seq, value).apply()

    var Recipe_Name: String?
        get() = prefs.getString(recipe_name, "")
        set(value) = prefs.edit().putString(recipe_name, value).apply()

    var FoodID: String?
        get() = prefs.getString(food_id, "")
        set(value) = prefs.edit().putString(food_id, value).apply()

    var FoodName: String?
        get() = prefs.getString(food_name, "")
        set(value) = prefs.edit().putString(food_name, value).apply()

    var FoodCategory: String?
        get() = prefs.getString(food_category, "")
        set(value) = prefs.edit().putString(food_category, value).apply()

    var FoodDate: String?
        get() = prefs.getString(food_date, "")
        set(value) = prefs.edit().putString(food_date, value).apply()

    var FoodYear: String?
        get() = prefs.getString(food_year, "")
        set(value) = prefs.edit().putString(food_year, value).apply()

    var FoodMonth: String?
        get() = prefs.getString(food_month, "")
        set(value) = prefs.edit().putString(food_month, value).apply()

    var FoodDay: String?
        get() = prefs.getString(food_day, "")
        set(value) = prefs.edit().putString(food_day, value).apply()

    var FoodCount: String?
        get() = prefs.getString(food_count, "1")
        set(value) = prefs.edit().putString(food_count, value).apply()

    var MemoID: String?
        get() = prefs.getString(memo_id, "")
        set(value) = prefs.edit().putString(memo_id, value).apply()

    var MemoContents: String?
        get() = prefs.getString(memo_contents, "")
        set(value) = prefs.edit().putString(memo_contents, value).apply()

    var Dbtn: Boolean
        get() = prefs.getBoolean(dialog_button, true)
        set(value) = prefs.edit().putBoolean(dialog_button, value).apply()

    var ReceiptName: String?
        get() = prefs.getString(receipt_name, "")
        set(value) = prefs.edit().putString(receipt_name, value).apply()

    var ReceiptCategory: String?
        get() = prefs.getString(receipt_category, "")
        set(value) = prefs.edit().putString(receipt_category, value).apply()

    var ReceiptDate: String?
        get() = prefs.getString(receipt_date, "")
        set(value) = prefs.edit().putString(receipt_date, value).apply()

    var ReceiptCount: String?
        get() = prefs.getString(receipt_count, "")
        set(value) = prefs.edit().putString(receipt_count, value).apply()

    var Voicename: String
        get() = prefs.getString(voice_name, "냉장고").toString()
        set(value) = prefs.edit().putString(voice_name, value).apply()

    var Voiceoption: Boolean
        get() = prefs.getBoolean(voice_option, false)
        set(value) = prefs.edit().putBoolean(voice_option, value).apply()

    var Voiceanswer: Boolean
        get() = prefs.getBoolean(voice_ans, false)
        set(value) = prefs.edit().putBoolean(voice_ans, value).apply()

    var Voicepause: Boolean
        get() = prefs.getBoolean(voice_pause, true)
        set(value) = prefs.edit().putBoolean(voice_pause, value).apply()

    var Voicereq: Boolean
        get() = prefs.getBoolean(voice_request, false)
        set(value) = prefs.edit().putBoolean(voice_request, value).apply()

    var Voicetime: Long
        get() = prefs.getLong(voice_time, 0)
        set(value) = prefs.edit().putLong(voice_time, value).apply()

    var WebSite: String?
        get() = prefs.getString(web_site, "")
        set(value) = prefs.edit().putString(web_site, value).apply()

    var TimerSecond: Int
        get() = prefs.getInt(timer_s, 60)
        set(value) = prefs.edit().putInt(timer_s, value).apply()

    var Timername: String?
        get() = prefs.getString(timer_name, "")
        set(value) = prefs.edit().putString(timer_name, value).apply()

    var Timermin: Int
        get() = prefs.getInt(timer_min, 0)
        set(value) = prefs.edit().putInt(timer_min, value).apply()

    var Timersec: Int
        get() = prefs.getInt(timer_sec, 0)
        set(value) = prefs.edit().putInt(timer_sec, value).apply()

    var Timersumtime: Int
        get() = prefs.getInt(timer_sumtime, 0)
        set(value) = prefs.edit().putInt(timer_sumtime, value).apply()

}