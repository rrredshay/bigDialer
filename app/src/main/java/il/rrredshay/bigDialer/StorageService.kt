package il.rrredshay.bigDialer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class StorageService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contacts", Context.MODE_PRIVATE)

    init {
        migrateIfNeeded()
    }

    private fun migrateIfNeeded() {
        if (!prefs.contains("migration_done") && prefs.contains("btn_1_name")) {
            val editor = prefs.edit()
            // Migrate all possible buttons (let's assume up to 100 for safety)
            for (i in 1..100) {
                if (prefs.contains("btn_${i}_name")) {
                    editor.putString("set_1_btn_${i}_name", prefs.getString("btn_${i}_name", ""))
                    editor.putString("set_1_btn_${i}_number", prefs.getString("btn_${i}_number", ""))
                    editor.putString("set_1_btn_${i}_photo", prefs.getString("btn_${i}_photo", null))
                    editor.putString("set_1_btn_${i}_label", prefs.getString("btn_${i}_label", null))
                    if (prefs.contains("btn_${i}_bg_color")) {
                        editor.putInt("set_1_btn_${i}_bg_color", prefs.getInt("btn_${i}_bg_color", 0))
                    }
                    if (prefs.contains("btn_${i}_text_color")) {
                        editor.putInt("set_1_btn_${i}_text_color", prefs.getInt("btn_${i}_text_color", 0))
                    }
                    if (prefs.contains("btn_${i}_image_alpha_int")) {
                        editor.putInt("set_1_btn_${i}_image_alpha_int", prefs.getInt("btn_${i}_image_alpha_int", 50))
                    }
                }
            }
            editor.putBoolean("migration_done", true)
            editor.apply()
        }
    }

    // Set Management
    fun getCurrentSetIndex(): Int = prefs.getInt("current_set_index", 1)
    fun setCurrentSetIndex(index: Int) {
        prefs.edit().putInt("current_set_index", index).apply()
    }

    fun saveSetMetadata(index: Int, name: String, bgColor: Int, textColor: Int) {
        prefs.edit().apply {
            putString("set_${index}_metadata_name", name)
            putInt("set_${index}_metadata_bg", bgColor)
            putInt("set_${index}_metadata_text", textColor)
            apply()
        }
    }

    fun getSetName(index: Int): String {
        val defaultName = if (index == 1) "מועדפים" else "סט $index"
        return prefs.getString("set_${index}_metadata_name", defaultName) ?: defaultName
    }

    fun getSetBgColor(index: Int): Int {
        return prefs.getInt("set_${index}_metadata_bg", Color.parseColor("#000080"))
    }

    fun getSetTextColor(index: Int): Int {
        return prefs.getInt("set_${index}_metadata_text", Color.WHITE)
    }

    private fun k(index: Int): String = "set_${getCurrentSetIndex()}_btn_${index}"

    fun saveContact(index: Int, name: String, number: String, photoUri: String? = null, label: String? = null) {
        prefs.edit().apply {
            putString("${k(index)}_name", name)
            putString("${k(index)}_number", number)
            putString("${k(index)}_photo", photoUri)
            putString("${k(index)}_label", label)
            apply()
        }
    }

    fun deleteContact(index: Int) {
        prefs.edit().apply {
            remove("${k(index)}_name")
            remove("${k(index)}_number")
            remove("${k(index)}_photo")
            remove("${k(index)}_label")
            remove("${k(index)}_bg_color")
            remove("${k(index)}_text_color")
            remove("${k(index)}_image_alpha_int")
            apply()
        }
    }

    fun loadContact(index: Int): Contact? {
        val name = prefs.getString("${k(index)}_name", null)
        val number = prefs.getString("${k(index)}_number", null)
        val photoUri = prefs.getString("${k(index)}_photo", null)
        val label = prefs.getString("${k(index)}_label", null)
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
        prefs.edit().putInt("${k(index)}_bg_color", color).apply()
    }

    fun getButtonBgColor(index: Int): Int {
        return prefs.getInt("${k(index)}_bg_color", getGlobalBgColor())
    }

    fun setButtonTextColor(index: Int, color: Int) {
        prefs.edit().putInt("${k(index)}_text_color", color).apply()
    }

    fun getButtonTextColor(index: Int): Int {
        return prefs.getInt("${k(index)}_text_color", getGlobalTextColor())
    }

    // Image Alpha
    fun setButtonImageAlphaInt(index: Int, alphaPercent: Int) {
        prefs.edit().putInt("${k(index)}_image_alpha_int", alphaPercent).apply()
    }

    fun getButtonImageAlphaInt(index: Int): Int {
        return prefs.getInt("${k(index)}_image_alpha_int", 50)
    }
}