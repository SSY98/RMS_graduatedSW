package com.project.rms.Barcode

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
import com.google.zxing.integration.android.IntentIntegrator
import com.project.rms.App
import com.project.rms.MainActivity
import com.project.rms.R
import com.project.rms.Recipe.ssy_RecipeActivity
import kotlinx.android.synthetic.main.ssh_dialog_barcode.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ssh_BarcodeDialog(context: Context, Interface: ssh_BarcodeDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var BarcodeDialogInterface: ssh_BarcodeDialogInterface = Interface

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_barcode)

        var food_name = findViewById<EditText>(R.id.food_name_edt)
        var food_category = findViewById<EditText>(R.id.food_category_edt)
        var food_date = findViewById<DatePicker>(R.id.Datepicker)
        var food_count = findViewById<EditText>(R.id.food_count_edt)
        var food_add = findViewById<Button>(R.id.food_add_btn)
        var food_cancel = findViewById<Button>(R.id.food_cancel_btn)
        var food_plus = findViewById<Button>(R.id.food_plus_btn)

        var FoodName = App.prefs.FoodName
        var FoodCategory = App.prefs.FoodCategory
        var FoodDate = App.prefs.FoodDate
        var FoodCount = App.prefs.FoodCount

        food_plus.setEnabled(App.prefs.Dbtn)

        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodCount = ""

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val listener =
            DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                val strDate = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
                val date: LocalDate = LocalDate.parse(strDate, formatter)
                App.prefs.FoodDate = date.toString()
            }

        // 바코드 인식으로 가져온 식재료 정보를 화면에 출력함
        food_name.setText(FoodName)
        food_category.setText(FoodCategory)
        if (App.prefs.FoodYear == "") {
            var cal = Calendar.getInstance() // 오늘 날짜
            food_date.init(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH)), cal.get(Calendar.DATE), listener)
        }
        else {
            food_date.init(App.prefs.FoodYear!!.toInt(), (App.prefs.FoodMonth!!.toInt())-1, App.prefs.FoodDay!!.toInt(), listener)
        }
        food_count.setText(FoodCount)

        // 확인 버튼 클릭 시 각각의 edittext에 입력된 식재료 정보를 SharedPreference 변수에 저장하고 onAddButtonClicked 호출 후 종료
        food_add.setOnClickListener {
            var year2 = Datepicker.getYear()
            var month2 = (Datepicker.getMonth())+1
            var day2 = Datepicker.getDayOfMonth()
            val strDate2 = year2.toString()+"-"+month2+"-"+day2
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
            val date: LocalDate = LocalDate.parse(strDate2, formatter)

            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = date.toString()
            App.prefs.FoodCount = food_count.text.toString()

            BarcodeDialogInterface.onAddButtonClicked()
            dismiss()}

        // 취소 버튼 클릭 시 onCancelButtonClicked 호출 후 종료
        food_cancel.setOnClickListener {
            BarcodeDialogInterface.onCancelButtonClicked()
            dismiss()}

        // 추가 버튼 클릭 시 edittext에 입력된 식재료 정보를 SharedPreference 변수에 저장하고 onPlustButtonClicked 호출 후 종료
        food_plus.setOnClickListener {
            var year2 = food_date.getYear()
            var month2 = (food_date.getMonth())+1
            var day2 = food_date.getDayOfMonth()
            val strDate2 = year2.toString()+"-"+month2+"-"+day2
            val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
            val date: LocalDate = LocalDate.parse(strDate2, formatter)

            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = date.toString()
            App.prefs.FoodCount = food_count.text.toString()
            BarcodeDialogInterface.onPlusButtonClicked()
            dismiss()
            }
    }
}