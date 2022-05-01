package com.project.rms.Foodlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.R

class ItemTouchHelperCallback(private val itemMoveListener: OnItemMoveListener, internal var mContext: Context) : ItemTouchHelper.Callback() {

    var leftBG: Int = Color.rgb(255,0,255)
    //var leftLabel: String = ""
    var leftIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_remove_item)


    private lateinit var background: Drawable


    var initiated: Boolean = false
    //Setting Swipe Text
    val paint = Paint()

    fun initSwipeView(): Unit {
        paint.setColor(Color.WHITE)
        paint.setTextSize(48f)
        paint.setTextAlign(Paint.Align.CENTER)
        background = ColorDrawable();
        initiated = true;
    }

    interface OnItemMoveListener {
        fun onItemMoved(fromPosition: Int, toPosition: Int)
        fun onItemSwiped(position: Int)
    }


    //getMovementFlags() : 이벤트의 방향을 설정, 어느 방향으로 움직 일지에 따라 flag를 정의
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (recyclerView.layoutManager is GridLayoutManager) {
            // GridLayout 형식인 경우
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            // 일반 LinearLayout 형식인 경우
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    //onMove() : 어느 위치에서 어느 위치로 변경하는지 이벤트를 받음
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        itemMoveListener.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }


    //onSwiped(): Swipe가 될 때 이벤트
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        itemMoveListener.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean = false

    //swipe delete 애니메이션
    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        if (!initiated) {
            initSwipeView()
        }


        if (dX != 0.0f) {

            if (dX < 0) {
                //left swipe
                val intrinsicHeight = (leftIcon?.getIntrinsicWidth() ?: 0)
                val xMarkTop = itemView.top + ((itemView.bottom - itemView.top) - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight

                colorCanavas(
                    c,
                    leftBG,
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                //drawTextOnCanvas(c, leftLabel, (itemView.right - 200).toFloat(), (xMarkTop + 10).toFloat())
                drawIconOnCanVas(
                    c, leftIcon, itemView.right - 2 * (leftIcon?.getIntrinsicWidth() ?: 0) - 0,
                    xMarkTop + 0,
                    itemView.right - (leftIcon?.getIntrinsicWidth() ?: 0) - 0,
                    xMarkBottom + 0
                )
            }else{
                //right swipe
                val intrinsicHeight = (leftIcon?.getIntrinsicWidth() ?: 0)
                val xMarkTop = itemView.top + ((itemView.bottom - itemView.top) - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight

                colorCanavas(c, leftBG, itemView.left + dX.toInt(), itemView.top, itemView.left, itemView.bottom)
                //drawTextOnCanvas(c, leftLabel, (itemView.left + 200).toFloat(), (xMarkTop + 10).toFloat())
                drawIconOnCanVas(
                    c, leftIcon, itemView.left + (leftIcon?.getIntrinsicWidth() ?: 0) + 0,
                    xMarkTop + 0,
                    itemView.left + 2 * (leftIcon?.getIntrinsicWidth() ?: 0) + 0,
                    xMarkBottom + 0
                )
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    fun colorCanavas(canvas: Canvas, canvasColor: Int, left: Int, top: Int, right: Int, bottom: Int): Unit {

        (background as ColorDrawable).color = canvasColor
        background.setBounds(left, top, right, bottom)
        background.draw(canvas)

    }
    /*fun drawTextOnCanvas(canvas: Canvas, label: String, x: Float, y: Float) {
        canvas.drawText(label, x, y, paint)
    }*/

    fun drawIconOnCanVas(
        canvas: Canvas, icon: Drawable?, left: Int, top: Int, right: Int, bottom: Int
    ): Unit {
        icon?.setBounds(left, top, right, bottom)
        icon?.draw(canvas)

    }
}