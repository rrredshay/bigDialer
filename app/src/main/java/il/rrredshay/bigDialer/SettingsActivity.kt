package il.rrredshay.bigDialer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var storageService: StorageService
    private var selectedBgColor: Int = 0
    private var selectedTextColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storageService = StorageService(this)
        selectedBgColor = storageService.getGlobalBgColor()
        selectedTextColor = storageService.getGlobalTextColor()

        val etRows = findViewById<EditText>(R.id.etRows)
        val etCols = findViewById<EditText>(R.id.etCols)
        val btnPickBg = findViewById<Button>(R.id.btnPickGlobalBg)
        val btnPickText = findViewById<Button>(R.id.btnPickGlobalText)
        val btnSave = findViewById<Button>(R.id.btnSave)

        etRows.setText(storageService.getGridRows().toString())
        etCols.setText(storageService.getGridCols().toString())
        
        updateButtonColors(btnPickBg, btnPickText)

        btnPickBg.setOnClickListener {
            ColorPickerDialogHelper.show(this, "Select Default Background") { color ->
                selectedBgColor = color
                updateButtonColors(btnPickBg, btnPickText)
            }
        }

        btnPickText.setOnClickListener {
            ColorPickerDialogHelper.show(this, "Select Default Text Color") { color ->
                selectedTextColor = color
                updateButtonColors(btnPickBg, btnPickText)
            }
        }

        btnSave.setOnClickListener {
            val rows = etRows.text.toString().toIntOrNull() ?: 5
            val cols = etCols.text.toString().toIntOrNull() ?: 3

            storageService.setGridSize(rows, cols)
            storageService.setGlobalBgColor(selectedBgColor)
            storageService.setGlobalTextColor(selectedTextColor)

            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.btnPrivacyPolicy).setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://rrredshay.github.io/BigDialer/privacy-policy.html")
            startActivity(intent)
        }
    }

    private fun updateButtonColors(btnBg: Button, btnText: Button) {
        btnBg.setBackgroundColor(selectedBgColor)
        btnBg.setTextColor(android.graphics.Color.WHITE) // Ensure button text is white for contrast if it's the "background picker"
        
        btnText.setBackgroundColor(selectedBgColor)
        btnText.setTextColor(selectedTextColor) // This is the preview for the text color
    }
}