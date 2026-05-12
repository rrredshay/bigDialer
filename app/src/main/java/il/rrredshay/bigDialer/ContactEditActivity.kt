package il.rrredshay.bigDialer

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ContactEditActivity : AppCompatActivity() {

    private lateinit var storageService: StorageService
    private lateinit var contactManager: ContactManager
    
    private var index: Int = -1
    private var name: String = ""
    private var number: String = ""
    private var label: String? = null
    private var photoUri: String? = null
    
    private var selectedBgColor: Int = Color.parseColor("#000080")
    private var selectedTextColor: Int = Color.WHITE
    private var selectedAlpha: Int = 50

    private lateinit var previewCard: MaterialCardView
    private lateinit var previewImage: ImageView
    private lateinit var previewName: TextView
    private lateinit var previewLabel: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoUri = it.toString()
            updatePreview()
        }
    }

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            contactUri?.let { uri ->
                val contactInfo = contactManager.getContactInfo(uri)
                if (contactInfo != null) {
                    name = contactInfo.name
                    number = contactInfo.phoneNumber
                    photoUri = contactInfo.photoUri
                    label = contactInfo.phoneLabel
                    updatePreview()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_edit)

        storageService = StorageService(this)
        contactManager = ContactManager(this)
        
        index = intent.getIntExtra("index", -1)
        name = intent.getStringExtra("name") ?: ""
        number = intent.getStringExtra("number") ?: ""
        label = intent.getStringExtra("label")
        photoUri = intent.getStringExtra("photoUri")
        
        selectedBgColor = storageService.getButtonBgColor(index)
        selectedTextColor = storageService.getButtonTextColor(index)
        selectedAlpha = storageService.getButtonImageAlphaInt(index)

        previewCard = findViewById(R.id.previewCard)
        previewImage = findViewById(R.id.previewImage)
        previewName = findViewById(R.id.previewName)
        previewLabel = findViewById(R.id.previewLabel)

        findViewById<Button>(R.id.btnChangeImage).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnChangeContact).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickContactLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btnResetContact).setOnClickListener {
            name = ""
            number = ""
            photoUri = null
            label = null
            selectedBgColor = Color.parseColor("#000080")
            selectedTextColor = Color.WHITE
            selectedAlpha = 50
            updatePreview()
        }

        findViewById<Button>(R.id.btnSaveContact).setOnClickListener {
            if (name.isBlank() && number.isBlank()) {
                storageService.deleteContact(index)
            } else {
                storageService.saveContact(index, name, number, photoUri, label)
                storageService.setButtonBgColor(index, selectedBgColor)
                storageService.setButtonTextColor(index, selectedTextColor)
                storageService.setButtonImageAlphaInt(index, selectedAlpha)
            }
            finish()
        }

        val sbAlpha = findViewById<SeekBar>(R.id.sbImageAlpha)
        val tvAlpha = findViewById<TextView>(R.id.tvAlphaValue)
        sbAlpha.progress = selectedAlpha
        tvAlpha.text = "$selectedAlpha%"
        
        sbAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedAlpha = progress
                tvAlpha.text = "$progress%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupColorPickers()
        updatePreview()
    }

    private fun updatePreview() {
        previewCard.setCardBackgroundColor(selectedBgColor)
        previewName.setTextColor(selectedTextColor)
        previewLabel.setTextColor(selectedTextColor)
        previewName.text = if (name.isNotBlank()) name else "שם איש קשר"
        previewLabel.text = label ?: ""
        
        if (!photoUri.isNullOrBlank()) {
            try {
                previewImage.setImageURI(Uri.parse(photoUri))
                previewImage.visibility = View.VISIBLE
                previewImage.alpha = selectedAlpha / 100f
            } catch (e: Exception) {
                previewImage.setImageResource(android.R.drawable.ic_menu_gallery)
                previewImage.visibility = View.VISIBLE
                previewImage.alpha = (selectedAlpha / 100f) * 0.4f
            }
        } else {
            previewImage.setImageResource(android.R.drawable.ic_menu_gallery)
            previewImage.visibility = View.VISIBLE
            previewImage.alpha = (selectedAlpha / 100f) * 0.4f // Lower alpha for placeholder
        }
    }

    private fun setupColorPickers() {
        val colors = intArrayOf(
            Color.TRANSPARENT, Color.parseColor("#000080"), Color.BLACK, Color.WHITE, Color.RED,
            Color.parseColor("#008000"), Color.BLUE, Color.YELLOW, Color.parseColor("#FFA500"),
            Color.parseColor("#800080"), Color.parseColor("#008080"), Color.parseColor("#800000"), Color.GRAY,
            Color.parseColor("#FFC0CB"), Color.parseColor("#A52A2A"), Color.parseColor("#4B0082"), Color.GREEN,
            Color.CYAN, Color.MAGENTA, Color.parseColor("#E91E63"), Color.parseColor("#2196F3"),
            Color.parseColor("#4CAF50"), Color.parseColor("#FFEB3B"), Color.parseColor("#FF5722"), Color.parseColor("#795548")
        )

        val rvBg = findViewById<RecyclerView>(R.id.rvBgColors)
        rvBg.layoutManager = GridLayoutManager(this, 8)
        rvBg.adapter = ColorAdapter(colors) { color ->
            selectedBgColor = color
            updatePreview()
        }

        val rvText = findViewById<RecyclerView>(R.id.rvTextColors)
        rvText.layoutManager = GridLayoutManager(this, 8)
        rvText.adapter = ColorAdapter(colors) { color ->
            selectedTextColor = color
            updatePreview()
        }
    }

    class ColorAdapter(private val colors: IntArray, private val onColorSelected: (Int) -> Unit) :
        RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorView: View = view.findViewById(R.id.colorCircle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color_circle, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val color = colors[position]
            if (color == Color.TRANSPARENT) {
                holder.colorView.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel)
                holder.colorView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.colorView.setBackgroundColor(color)
            }
            holder.itemView.setOnClickListener { onColorSelected(color) }
        }

        override fun getItemCount() = colors.size
    }
}