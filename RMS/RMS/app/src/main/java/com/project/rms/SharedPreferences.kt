package com.project.rms

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences (context: Context) {
    private val prefsFilename = "prefs"
    // private val 프리퍼런스 변수이름 = "다른액티비티에서 사용할 이름"
    private val recipe_name = "Recipe_Name"
    private val recipe_seq = "Recipe_Seq"
    private val food_name = "FoodName"
    private val food_category = "FoodCategory"
    private val food_date = "FoodDate"
    private val food_count = "FoodCount"

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, Context.MODE_PRIVATE)

    // get() 저장된 값을 반환, 기본 값은 "", set(value)은 value 값을 대체한 후 저장
    var Recipe_Seq: String?
        get() = prefs.getString(recipe_seq, "")
        set(value) = prefs.edit().putString(recipe_seq, value).apply()

    var Recipe_Name: String?
        get() = prefs.getString(recipe_name, "")
        set(value) = prefs.edit().putString(recipe_name, value).apply()

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
}