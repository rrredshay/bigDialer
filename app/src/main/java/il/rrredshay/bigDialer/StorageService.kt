package il.rrredshay.bigDialer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class StorageService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contacts", Context.MODE_PRIVATE)

    fun saveContact(index: Int, name: String, number: String, photoUri: String? = null, label: String? = null) {
        prefs.edit().apply {
            putString("btn_${index}_name", name)
            putString("btn_${index}_number", number)
            putString("btn_${index}_photo", photoUri)
            putString("btn_${index}_label", label)
            apply()
        }
    }

    fun deleteContact(index: Int) {
        prefs.edit().apply {
            remove("btn_${index}_name")
            remove("btn_${index}_number")
            remove("btn_${index}_photo")
            remove("btn_${index}_label")
            remove("btn_${index}_bg_color")
            remove("btn_${index}_text_color")
            apply()
        }
    }

    fun loadContact(index: Int): Contact? {
        val name = prefs.getString("btn_${index}_name", null)
        val number = prefs.getString("btn_${index}_number", null)
        val photoUri = prefs.getString("btn_${index}_photo", null)
        val label = prefs.getString("btn_${index}_label", null)
        return if (name != null && number != null) Contact(name, number, photoUri, label) else null
    }

    // Grid Settings
    fun setGridSize(rows: Int, cols: Int) {
        prefs.edit().putInt("grid_rows", rows).putInt("grid_cols", cols).apply()
    }

    fun getGridRows(): Int = prefs.getInt("grid_rows", 5)
    fun getGridCols(): Int = prefs.getInt("grid_cols", 3)

    // Global Colors
    fun setGlobalBgColor(color: Int) {
        prefs.edit().putInt("global_bg_color", color).apply()
    }

    fun getGlobalBgColor(): Int = prefs.getInt("global_bg_color", Color.parseColor("#000080"))

    fun setGlobalTextColor(color: Int) {
        prefs.edit().putInt("global_text_color", color).apply()
    }

    fun getGlobalTextColor(): Int = prefs.getInt("global_text_color", Color.WHITE)

    // Individual Button Colors
    fun setButtonBgColor(index: Int, color: Int) {
        prefs.edit().putInt("btn_${index}_bg_color", color).apply()
    }

    fun getButtonBgColor(index: Int): Int {
        return prefs.getInt("btn_${index}_bg_color", getGlobalBgColor())
    }

    fun setButtonTextColor(index: Int, color: Int) {
        prefs.edit().putInt("btn_${index}_text_color", color).apply()
    }

    fun getButtonTextColor(index: Int): Int {
        return prefs.getInt("btn_${index}_text_color", getGlobalTextColor())
    }

    // Image Alpha
    fun setButtonImageAlpha(index: Int, alpha: Float) {
        prefs.edit().putFloat("btn_${index}_image_alpha", alpha).apply()
    }

    fun getButtonImageAlpha(index: Int): Float {
        return prefs.getInt("btn_${index}_image_alpha_int", 50) / 100f
    }
    
    // Compatibility or direct storage
    fun setButtonImageAlphaInt(index: Int, alphaPercent: Int) {
        prefs.edit().putInt("btn_${index}_image_alpha_int", alphaPercent).apply()
    }

    fun getButtonImageAlphaInt(index: Int): Int {
        return prefs.getInt("btn_${index}_image_alpha_int", 50)
    }
}