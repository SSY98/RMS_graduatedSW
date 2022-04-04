package com.project.rms.Barcode

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.zxing.integration.android.IntentIntegrator
import com.project.rms.App
import com.project.rms.MainActivity
import com.project.rms.R
import com.project.rms.Recipe.ssy_RecipeActivity

class ssh_BarcodeDialog(context: Context, Interface: ssh_BarcodeDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var BarcodeDialogInterface: ssh_BarcodeDialogInterface = Interface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_barcode)

        var food_name = findViewById<EditText>(R.id.food_name_edt)
        var food_category = findViewById<EditText>(R.id.food_category_edt)
        var food_date = findViewById<EditText>(R.id.food_date_edt)
        var food_count = findViewById<EditText>(R.id.food_count_edt)
        var food_add = findViewById<Button>(R.id.food_add_btn)
        var food_cancel = findViewById<Button>(R.id.food_cancel_btn)
        var food_plus = findViewById<Button>(R.id.food_plus_btn)

        var FoodName = App.prefs.FoodName
        var FoodCategory = App.prefs.FoodCategory
        var FoodDate = App.prefs.FoodDate
        var FoodCount = App.prefs.FoodCount

        // 바코드 인식으로 가져온 식재료 정보를 화면에 출력함
        food_name.setText(FoodName)
        food_category.setText(FoodCategory)
        food_date.setText(FoodDate)
        food_count.setText(FoodCount)

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 추가 버튼 클릭 시 onAddButtonClicked 호출 후 종료
        food_add.setOnClickListener {
            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = food_date.text.toString()
            App.prefs.FoodCount = food_count.text.toString()
            BarcodeDialogInterface.onAddButtonClicked()
            dismiss()}

        // 취소 버튼 클릭 시 onCancelButtonClicked 호출 후 종료
        food_cancel.setOnClickListener {
            BarcodeDialogInterface.onCancelButtonClicked()
            dismiss()}

        food_plus.setOnClickListener {
            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = food_date.text.toString()
            App.prefs.FoodCount = food_count.text.toString()
            BarcodeDialogInterface.onPlusButtonClicked()
            dismiss()
            }
    }
}