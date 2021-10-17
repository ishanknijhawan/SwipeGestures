package com.example.listadapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var dragHelper: ItemTouchHelper
    private lateinit var swipeHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val height = (displayMetrics.heightPixels / displayMetrics.density).toInt().dp
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt().dp

        val deleteIcon = resources.getDrawable(R.drawable.ic_outline_delete_24, null)
        val archiveIcon = resources.getDrawable(R.drawable.ic_outline_archive_24, null)
        val rvList = findViewById<RecyclerView>(R.id.rv_list)

        val deleteColor = resources.getColor(android.R.color.holo_red_light)
        val archiveColor = resources.getColor(android.R.color.holo_green_light)

        val list = arrayListOf<String>().apply {
            for (i in 0..100) {
                add("Item $i")
            }
        }
        val adapter = ItemAdapter(this, list)
        rvList.adapter = adapter

        swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                list.removeAt(pos)
                adapter.notifyItemRemoved(pos)

                Snackbar.make(
                    findViewById(R.id.ll_main),
                    if (direction == ItemTouchHelper.RIGHT) "Deleted" else "Archived",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                //1. Background color based upon direction swiped
                when {
                    abs(dX) < width / 3 -> canvas.drawColor(Color.GRAY)
                    dX > width / 3 -> canvas.drawColor(deleteColor)
                    else -> canvas.drawColor(archiveColor)
                }

                //2. Printing the icons
                val textMargin = resources.getDimension(R.dimen.text_margin)
                    .roundToInt()
                deleteIcon.bounds = Rect(
                    textMargin,
                    viewHolder.itemView.top + textMargin + 8.dp,
                    textMargin + deleteIcon.intrinsicWidth,
                    viewHolder.itemView.top + deleteIcon.intrinsicHeight
                            + textMargin + 8.dp
                )
                archiveIcon.bounds = Rect(
                    width - textMargin - archiveIcon.intrinsicWidth,
                    viewHolder.itemView.top + textMargin + 8.dp,
                    width - textMargin,
                    viewHolder.itemView.top + archiveIcon.intrinsicHeight
                            + textMargin + 8.dp
                )

                //3. Drawing icon based upon direction swiped
                if (dX > 0) deleteIcon.draw(canvas) else archiveIcon.draw(canvas)

                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })

        dragHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                viewHolder.itemView.elevation = 16F

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                Collections.swap(list, from, to)
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                viewHolder?.itemView?.elevation = 0F
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        })

        swipeHelper.attachToRecyclerView(rvList)
        dragHelper.attachToRecyclerView(rvList)
    }

    fun startDragging(holder: RecyclerView.ViewHolder) {
        dragHelper.startDrag(holder)
    }

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()
}