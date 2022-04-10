package com.project.rms.Foodlist.UpdateDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.project.rms.App
import com.project.rms.R

class ssh_FoodListUpdateDialog(context: Context, Interface: ssh_FoodListUpdateDialogInterface) : Dialog(context) {
    // 인터페이스를 받아옴
    private var FoodListUpdateDialogInterface: ssh_FoodListUpdateDialogInterface = Interface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_dialog_update_foodlist)

        var food_name = findViewById<EditText>(R.id.update_food_name)
        var food_category = findViewById<EditText>(R.id.update_food_category)
        var food_date = findViewById<EditText>(R.id.update_food_date)
        var food_count = findViewById<EditText>(R.id.update_food_count)
        var food_update = findViewById<Button>(R.id.foodlist_update_btn)
        var food_cancel = findViewById<Button>(R.id.foodlist_cancel_btn)

        var FoodName = App.prefs.FoodName
        var FoodCategory = App.prefs.FoodCategory
        var FoodDate = App.prefs.FoodDate
        var FoodCount = App.prefs.FoodCount

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 식재료 정보를 화면에 출력함
        food_name.setText(FoodName)
        food_category.setText(FoodCategory)
        food_date.setText(FoodDate)
        food_count.setText(FoodCount)

        // 수정 버튼 클릭 시 onUpdateButtonClicked 호출 후 종료
        food_update.setOnClickListener {
            Log.d("memo", "hi5")
            App.prefs.FoodName = food_name.text.toString()
            App.prefs.FoodCategory = food_category.text.toString()
            App.prefs.FoodDate = food_date.text.toString()
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