package com.project.rms.Foodlist.UpdateDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.project.rms.App
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_dialog_barcode.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ssh_FoodListUpdateDialog(context: Context, Interface: ssh_FoodListUpdateDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var FoodListUpdateDialogInterface: ssh_FoodListUpdateDialogInterface = Interface

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_update_foodlist)

        var food_name = findViewById<EditText>(R.id.update_food_name)
        var food_category = findViewById<EditText>(R.id.update_food_category)

        var food_count = findViewById<EditText>(R.id.update_food_count)
        var food_update = findViewById<Button>(R.id.foodlist_update_btn)
        var food_cancel = findViewById<Button>(R.id.foodlist_cancel_btn)
        var food_date = findViewById<DatePicker>(R.id.UpdateDatepicker)
        var FoodName = App.prefs.FoodName
        var FoodCategory = App.prefs.FoodCategory
        var FoodDate = App.prefs.FoodDate
        var FoodCount = App.prefs.FoodCount

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 유통기한 날짜를 불러와 -을 기준으로 년, 월, 일을 나누고 각각의 값을 sharedpreference 변수에 저장
        val DateSplit = FoodDate.toString().split("-")
        App.prefs.FoodYear = DateSplit[0]
        App.prefs.FoodMonth = DateSplit[1]
        App.prefs.FoodDay = DateSplit[2]

        val listener =
            DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                val strDate = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
                val date: LocalDate = LocalDate.parse(strDate, formatter)
                App.prefs.FoodDate = date.toString()
            }

        // 식재료 정보를 화면에 출력함
        food_name.setText(FoodName)
        food_category.setText(FoodCategory)
        food_date.init(App.prefs.FoodYear!!.toInt(), (App.prefs.FoodMonth!!.toInt())-1, App.prefs.FoodDay!!.toInt(), listener)
        food_count.setText(FoodCount)

        // 수정 버튼 클릭 시 onUpdateButtonClicked 호출 후 종료
        food_update.setOnClickListener {
            var year2 = food_date.getYear()
            var month2 = (food_date.getMonth())+1
            var day2 = food_date.getDayOfMonth()
            val strDate2 = year2.toString()+"-"+month2+"-"+day2
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
            val date2: LocalDate = LocalDate.parse(strDate2, formatter)

            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = date2.toString()
            App.prefs.FoodCount = food_count.text.toString()
            FoodListUpdateDialogInterface.onUpdateButtonClicked()
            dismiss()
        }

        // 취소 버튼 클릭 시 onCancelButtonClicked 호출 후 종료
        food_cancel.setOnClickListener {
            FoodListUpdateDialogInterface.onCancelButtonClicked()
            dismiss()
        }
    }
}