package com.project.rms

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences (context: Context) {
    private val prefsFilename = "prefs"
    // private val 프리퍼런스 변수이름 = "다른액티비티에서 사용할 이름"
    private val recipe_name = "Recipe_Name" //레시피에 사용
    private val recipe_seq = "Recipe_Seq" //레시피에 사용
    private val food_id = "FoodID"
    private val food_name = "FoodName"
    private val food_category = "FoodCategory"
    private val food_date = "FoodDate"
    private val food_count = "FoodCount"

    //음서인식에 사용
    private val voice_option = "Voiceoption" //음성인식 on/off setting
    private val voice_ans = "Voiceanswer" //음성인식의 대답형태
    private val voice_pause = "Voicepause" //음성인식 잠깐 멈출때사용(ex) 타이머
    private val voice_request = "Voicereq" //음성인식으로 실행시킨 액티비티에서 사용

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

    var FoodCount: String?
        get() = prefs.getString(food_count, "1")
        set(value) = prefs.edit().putString(food_count, value).apply()

    var Voiceoption: Boolean
        get() = prefs.getBoolean(voice_option, true)
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

    var WebSite: String?
        get() = prefs.getString(web_site, "")
        set(value) = prefs.edit().putString(web_site, value).apply()

    var TimerSecond: Int
        get() = prefs.getInt(timer_s, 60)
        set(value) = prefs.edit().putInt(timer_s, value).apply()

}