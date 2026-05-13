package il.rrredshay.bigDialer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var lastClickedButtonIndex = -1
    private lateinit var storageService: StorageService
    private lateinit var contactManager: ContactManager
    private lateinit var gridLayout: GridLayout

    private val handler = Handler(Looper.getMainLooper())
    private var longClickRunnable: Runnable? = null

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            contactUri?.let { uri ->
                val contactInfo = contactManager.getContactInfo(uri)
                if (contactInfo != null) {
                    val intent = Intent(this, ContactEditActivity::class.java).apply {
                        putExtra("index", lastClickedButtonIndex)
                        putExtra("name", contactInfo.name)
                        putExtra("number", contactInfo.phoneNumber)
                        putExtra("photoUri", contactInfo.photoUri)
                        putExtra("label", contactInfo.phoneLabel)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (lastClickedButtonIndex != -1) {
                openContactPicker()
            }
        } else {
            Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storageService = StorageService(this)
        contactManager = ContactManager(this)
        gridLayout = findViewById(R.id.gridLayout)

        findViewById<FloatingActionButton>(R.id.fabSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setupGrid()
    }

    private fun setupGrid() {
        gridLayout.removeAllViews()
        val rows = storageService.getGridRows()
        val cols = storageService.getGridCols()
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        val totalButtons = rows * cols
        for (i in 1..totalButtons) {
            val card = createButton(i)
            gridLayout.addView(card)
        }
    }

    private fun createButton(index: Int): MaterialCardView {
        val card = MaterialCardView(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            radius = 8f * resources.displayMetrics.density
            cardElevation = 4f * resources.displayMetrics.density
        }

        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        card.addView(imageView)

        val textLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }
        card.addView(textLayout)

        val nameTv = AppCompatTextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
        }
        textLayout.addView(nameTv)

        val labelTv = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            textSize = 14f
        }
        textLayout.addView(labelTv)

        updateButtonUI(card, nameTv, labelTv, imageView, index)

        card.setOnClickListener {
            onButtonClick(index)
        }

        card.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longClickRunnable = Runnable {
                        onButtonLongClick3s(index)
                    }
                    handler.postDelayed(longClickRunnable!!, 3000)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longClickRunnable?.let { handler.removeCallbacks(it) }
                }
            }
            false
        }

        return card
    }

    private fun updateButtonUI(card: MaterialCardView, nameTv: TextView, labelTv: TextView, iv: ImageView, index: Int) {
        val contact = storageService.loadContact(index)
        val bgColor = storageService.getButtonBgColor(index)
        val textColor = storageService.getButtonTextColor(index)
        val imageAlpha = storageService.getButtonImageAlphaInt(index) / 100f

        card.setCardBackgroundColor(bgColor)
        nameTv.setTextColor(textColor)
        labelTv.setTextColor(textColor)

        if (contact != null) {
            nameTv.text = contact.name
            if (contact.phoneLabel != null) {
                labelTv.text = contact.phoneLabel
                labelTv.visibility = View.VISIBLE
            } else {
                labelTv.visibility = View.GONE
            }
            
            if (contact.photoUri != null) {
                try {
                    iv.setImageURI(Uri.parse(contact.photoUri))
                    iv.visibility = View.VISIBLE
                    iv.alpha = imageAlpha
                } catch (e: Exception) {
                    iv.visibility = View.GONE
                }
            } else {
                iv.visibility = View.GONE
            }
        } else {
            nameTv.text = "+"
            labelTv.visibility = View.GONE
            iv.visibility = View.GONE
        }
    }

    private fun onButtonClick(index: Int) {
        val contact = storageService.loadContact(index)
        if (contact != null) {
            makeCall(contact.phoneNumber)
        } else {
            lastClickedButtonIndex = index
            checkAndOpenPicker()
        }
    }

    private fun onButtonLongClick3s(index: Int) {
        val contact = storageService.loadContact(index)
        if (contact != null) {
            val intent = Intent(this, ContactEditActivity::class.java).apply {
                putExtra("index", index)
                putExtra("name", contact.name)
                putExtra("number", contact.phoneNumber)
                putExtra("photoUri", contact.photoUri)
                putExtra("label", contact.phoneLabel)
            }
            startActivity(intent)
        } else {
            lastClickedButtonIndex = index
            checkAndOpenPicker()
        }
    }

    private fun checkAndOpenPicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            showContactsDisclosure()
        } else {
            openContactPicker()
        }
    }

    private fun showContactsDisclosure() {
        AlertDialog.Builder(this)
            .setTitle("גישה לאנשי קשר")
            .setMessage("אפליקציה זו זקוקה לגישה לאנשי הקשר שלך כדי לאפשר לך לבחור מספרים ולהגדיר אותם ככפתורי חיוג מהיר. אנשי הקשר שלך לא נאספים או מועברים לשום שרת.")
            .setPositiveButton("הבנתי") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    private fun pickIndividualColors(index: Int, contact: Contact) {
        ColorPickerDialogHelper.show(this, "Select Background for ${contact.name}") { bgColor ->
            ColorPickerDialogHelper.show(this, "Select Text Color for ${contact.name}") { textColor ->
                storageService.saveContact(index, contact.name, contact.phoneNumber, contact.photoUri, contact.phoneLabel)
                storageService.setButtonBgColor(index, bgColor)
                storageService.setButtonTextColor(index, textColor)
                setupGrid()
            }
        }
    }

    private var lastCallTime = 0L
    private val callDebounceTime = 2000L

    private fun makeCall(phoneNumber: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCallTime < callDebounceTime) return
        lastCallTime = currentTime

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 101)
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            try {
                startActivity(intent)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Call error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}