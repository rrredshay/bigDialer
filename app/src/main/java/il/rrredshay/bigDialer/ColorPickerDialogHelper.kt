package il.rrredshay.bigDialer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog

object ColorPickerDialogHelper {

    private val colors = intArrayOf(
        Color.parseColor("#000080"), // Navy
        Color.parseColor("#000000"), // Black
        Color.parseColor("#FFFFFF"), // White
        Color.parseColor("#FF0000"), // Red
        Color.parseColor("#008000"), // Green
        Color.parseColor("#0000FF"), // Blue
        Color.parseColor("#FFFF00"), // Yellow
        Color.parseColor("#FFA500"), // Orange
        Color.parseColor("#800080"), // Purple
        Color.parseColor("#008080"), // Teal
        Color.parseColor("#800000"), // Maroon
        Color.parseColor("#808080"), // Gray
        Color.parseColor("#FFC0CB"), // Pink
        Color.parseColor("#A52A2A"), // Brown
        Color.parseColor("#4B0082"), // Indigo
        Color.parseColor("#00FF00")  // Lime
    )

    fun show(context: Context, title: String, onColorSelected: (Int) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_color_grid, null)
        val grid = view.findViewById<GridLayout>(R.id.colorGrid)

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()

        for (color in colors) {
            val colorView = View(context).apply {
                val size = (50 * context.resources.displayMetrics.density).toInt()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(color)
                setOnClickListener {
                    onColorSelected(color)
                    dialog.dismiss()
                }
            }
            grid.addView(colorView)
        }

        dialog.show()
    }
}