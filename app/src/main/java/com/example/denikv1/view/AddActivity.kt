package com.example.denikv1.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.denikv1.R
import com.example.denikv1.controller.CestaController
import com.example.denikv1.controller.CestaControllerImpl
import com.example.denikv1.custom.CustomArrayAdapter
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.model.CestaModel
import com.example.denikv1.model.CestaModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import android.webkit.JavascriptInterface
import android.widget.ScrollView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.denikv1.custom.LocationHelper
import com.example.denikv1.custom.MyLocationListener

class AddActivity : AppCompatActivity() {
    private val cestaModel: CestaModel = CestaModelImpl(this)
    private val cestaController: CestaController = CestaControllerImpl(cestaModel)
    private var selectedDate: Long = 0
    private var selectedButton: ImageButton? = null
    private var selectedButton2: ImageButton? = null
    private var cestaId: Long = 0
    private var selectedButtonTag: String? = null
    private var selectedButtonTag2: String? = null
    private var isCestaCreated: Boolean = false
    private var currentCesta: CestaEntity? = null
    private var isCestaDeleted: Boolean = false
    private var oldCesta: CestaEntity? = null
    lateinit var locationHelper: LocationHelper
    private val locationListener = MyLocationListener(this)
    private lateinit var btnGetLocation: Button

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zapis)

        cestaId = calculateNextCestaId()

        setupCustomActionBar()
        setupUIComponents()
        toggleVisibility()
        setupButtons()
        setupSpinner()

        revertChangesButton()
        saveAndBack()

        val receivedIntent = intent
        cestaId = receivedIntent.getLongExtra("cestaId", 0)

        if (cestaId != 0L) {
            lifecycleScope.launch {
                val cesta = cestaModel.getCestaById(cestaId)
                currentCesta = cesta
                fillUI(cesta)
                oldCesta = cesta
            }
        }

        // Inicializujte LocationHelper
        locationHelper = LocationHelper(this)

        // Check for permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are granted, proceed to get the location
            locationHelper.requestLocationUpdates(locationListener)
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }
        updateEditTextWithLastLocation()
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    fun updateEditTextWithLastLocation() {
        val latitudeEditText = findViewById<EditText>(R.id.latitudeEditText)
        val longitudeEditText = findViewById<EditText>(R.id.longitudeEditText)
        val webView: WebView = findViewById(R.id.webView)
        val scrollView: ScrollView = findViewById(R.id.scrollViewZapis)

        webView.setOnTouchListener { _, event ->
            // Zabránit posunu ScrollView, pokud se dotyky nacházejí v mapě
            if (isTouchInsideMap(event)) {
                // Dotyk je uvnitř mapy, vrátit true pro zabránění posunu ScrollView
                scrollView.requestDisallowInterceptTouchEvent(true)
            } else {
                // Dotyk není uvnitř mapy, povolit posun ScrollView
                scrollView.requestDisallowInterceptTouchEvent(false)
            }
            false // Vrátit false, aby se prováděly další dotyky
        }

        // Nastavení cesty k HTML souboru s mapou
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            WebAppInterface(latitudeEditText, longitudeEditText),
            "Android"
        )
        webView.webViewClient = WebViewClient()

        btnGetLocation = findViewById(R.id.btnGetLocation)
        btnGetLocation.setOnClickListener {
            locationListener.lastLocation?.let { lastLocation ->
                val latitude = lastLocation.latitude
                val longitude = lastLocation.longitude

                // Aktualizovat hodnoty v EditText prvcích
                latitudeEditText.setText(latitude.toString())
                longitudeEditText.setText(longitude.toString())

                val javascriptCommand = "updateMapToCurrentLocation($latitude, $longitude);"
                webView.post { webView.loadUrl("javascript:$javascriptCommand") }
            }
        }

        webView.loadUrl("file:///android_asset/leaflet_map.html")
    }

    private fun isTouchInsideMap(event: MotionEvent): Boolean {
        val webView: WebView = findViewById(R.id.webView)

        // Zjistěte souřadnice dotyku
        val x = event.x.toInt()
        val y = event.y.toInt()

        // Zjistěte souřadnice oblasti mapy ve webView
        val webViewLocation = IntArray(2)
        webView.getLocationOnScreen(webViewLocation)
        val webViewX = webViewLocation[0]
        val webViewY = webViewLocation[1]
        val webViewWidth = webView.width
        val webViewHeight = webView.height

        // Zjistěte, zda je dotyk uvnitř oblasti mapy
        return (x >= webViewX && x <= webViewX + webViewWidth
                && y >= webViewY && y <= webViewY + webViewHeight)
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to get the location
                    locationHelper.requestLocationUpdates(locationListener)
                } else {
                    // Permission denied, handle accordingly
                    // You might want to show a message or take appropriate action
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun revertChangesButton() {
        val buttonShowAdd: Button = findViewById(R.id.button_vratit_zmeny)
        buttonShowAdd.setOnClickListener {

            if (cestaId.toInt() == 0) {
                lifecycleScope.launch {
                    val intent = Intent(this@AddActivity, CestaViewImp::class.java)
                    startActivity(intent)
                    currentCesta?.let { deleteCesta(it) }
                }
            }
            else {
                lifecycleScope.launch {
                    oldCesta?.let { it1 -> cestaModel.updateCesta(it1) }
                    val intent = Intent(this@AddActivity, CestaViewImp::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun saveAndBack() {
        val buttonShowAdd: Button = findViewById(R.id.button_ulozit)
        buttonShowAdd.setOnClickListener {
            saveCesta()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupCustomActionBar() {
        val customBarAdd = layoutInflater.inflate(R.layout.custom_bar_add, FrameLayout(this))
        supportActionBar?.customView = customBarAdd
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.elevation = 0f

        customBarAdd.findViewById<Button>(R.id.action_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        customBarAdd.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            lifecycleScope.launch {
                currentCesta?.let { deleteCesta(it) }
                isCestaDeleted = true
            }
        }
    }

    private fun calculateNextCestaId(): Long {
        val result = runBlocking {
            val deferredResult = async(Dispatchers.Default) {
                val allTasks = cestaController.getAllCesta()
                if (allTasks.isNotEmpty()) {
                    allTasks.last().id + 1
                } else {
                    1
                }
            }
            deferredResult.await()
        }
        return result
    }

    private fun setupUIComponents() {
        val routeNameEditText: EditText = findViewById(R.id.nameEditText)
        val fallEditText: EditText = findViewById(R.id.fallEditText)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val minuteEditText: EditText = findViewById(R.id.minutesEditText)
        val secondEditText: EditText = findViewById(R.id.secondsEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val opinionRatingBar: RatingBar = findViewById(R.id.opinionRatingBar)
        val datePicker: DatePicker = findViewById(R.id.datePicker)
        val latitudeEditText: EditText = findViewById(R.id.latitudeEditText)
        val longitudeEditText: EditText = findViewById(R.id.longitudeEditText)

        val textWatcher = createTextWatcher()
        val itemSelectedListener = createItemSelectedListener()

        routeNameEditText.addTextChangedListener(textWatcher)
        fallEditText.addTextChangedListener(textWatcher)
        routeStyleSpinner.onItemSelectedListener = itemSelectedListener
        routeGradeSpinner.onItemSelectedListener = itemSelectedListener
        minuteEditText.addTextChangedListener(textWatcher)
        descriptionEditText.addTextChangedListener(textWatcher)
        secondEditText.addTextChangedListener(textWatcher)
        latitudeEditText.addTextChangedListener(textWatcher)
        longitudeEditText.addTextChangedListener(textWatcher)

        opinionRatingBar.setOnRatingBarChangeListener { _, _, _ -> saveCesta() }
        setupDatePickerListener(datePicker)

        val linearLayoutFalls: LinearLayout = findViewById(R.id.falls)
        routeStyleSpinner.onItemSelectedListener = createClimbStyleItemSelectedListener(linearLayoutFalls)
    }

    private fun createTextWatcher(): TextWatcher {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveCesta()
            }
        }
        return textWatcher
    }

    private fun createItemSelectedListener(): AdapterView.OnItemSelectedListener {
        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveCesta()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        return itemSelectedListener
    }

    private fun setupDatePickerListener(datePicker: DatePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                saveCesta()
            }
        }
    }

    private fun createClimbStyleItemSelectedListener(linearLayoutFalls: LinearLayout): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Získání vybrané položky ze Spinneru
                val selectedStyle = parentView?.getItemAtPosition(position).toString()

                // Nastavení viditelnosti pole pro počet pádů podle vybraného stylu
                linearLayoutFalls.visibility = if (selectedStyle.equals("On sight", ignoreCase = true) ||
                    selectedStyle.equals("Flash", ignoreCase = true)
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun toggleVisibility() {
        val showHideButton: LinearLayout = findViewById(R.id.showHideButton)
        val showHideButtonImg: ImageView = findViewById(R.id.showHideButtonImg)

        val descriptionAll: LinearLayout = findViewById(R.id.Description_hide)


        descriptionAll.visibility = View.GONE


        showHideButton.setOnClickListener {
            if (descriptionAll.visibility == View.VISIBLE) {
                // Pokud jsou položky viditelné, skryj je
                descriptionAll.visibility = View.GONE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_right)
            } else {
                // Pokud jsou položky skryté, zobraz je
                descriptionAll.visibility = View.VISIBLE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_down)
            }
        }
    }

    private fun setupButtons() {
        val gradeButtons = listOf(
            R.id.button_plus to "+",
            R.id.button_nula to "",
            R.id.button_minus to "-"
        )

        val characterButtons = listOf(
            R.id.button_sila to "Silová",
            R.id.button_technika to "Technická",
            R.id.button_kombinace to "Kombinace"
        )

        gradeButtons.forEach { (buttonId, actionName) ->
            setupButtonAction(actionName, findViewById(buttonId))
        }

        characterButtons.forEach { (buttonId, actionName) ->
            setupButtonAction(actionName, findViewById(buttonId))
        }
    }

    private fun setupButtonAction(actionName: String, button: ImageButton) {
        button.setOnClickListener {
            val isSelectedButton = when (actionName) {
                in listOf("+", "", "-") -> selectedButton
                in listOf("Silová", "Technická", "Kombinace") -> selectedButton2
                else -> null
            }

            // Odmáčkne předchozí tlačítko, pokud existuje
            isSelectedButton?.isSelected = false

            button.isSelected = true

            when (actionName) {
                in listOf("+", "", "-") -> {
                    selectedButtonTag = when (actionName) {
                        "+" -> "plus"
                        "" -> "nula"
                        "-" -> "minus"
                        else -> null
                    }
                    selectedButton = button
                }
                in listOf("Silová", "Technická", "Kombinace") -> {
                    selectedButtonTag2 = when (actionName) {
                        "Silová" -> "Síla"
                        "Technická" -> "Technika"
                        "Kombinace" -> "Kombinace"
                        else -> null
                    }
                    selectedButton2 = button
                }
            }

            // Aktualizujte barvy tlačítek
            updateSelectedButtonView()

            onButtonClicked(button)
            saveCesta()
        }
    }

    private fun onButtonClicked(view: View) {
        // Odmáčkne předchozí tlačítko, pokud existuje
        when (view) {
            selectedButton -> selectedButton?.isSelected = false
            selectedButton2 -> selectedButton2?.isSelected = false
        }

        view.isSelected = true

        when (view.id) {
            R.id.button_plus, R.id.button_nula, R.id.button_minus -> {
                selectedButtonTag = when (view.id) {
                    R.id.button_plus -> "plus"
                    R.id.button_nula -> "nula"
                    R.id.button_minus -> "minus"
                    else -> null
                }
                selectedButton = view as? ImageButton
            }
            R.id.button_sila, R.id.button_technika, R.id.button_kombinace -> {
                selectedButtonTag2 = when (view.id) {
                    R.id.button_sila -> "Síla"
                    R.id.button_technika -> "Technika"
                    R.id.button_kombinace -> "Kombinace"
                    else -> null
                }
                selectedButton2 = view as? ImageButton
            }
        }

        // Aktualizujte barvy tlačítek
        updateSelectedButtonView()
    }

    private fun updateSelectedButtonView() {
        val buttonPlus: ImageButton = findViewById(R.id.button_plus)
        val buttonNula: ImageButton = findViewById(R.id.button_nula)
        val buttonMinus: ImageButton = findViewById(R.id.button_minus)

        val buttonSila: ImageButton = findViewById(R.id.button_sila)
        val buttonTechnika: ImageButton = findViewById(R.id.button_technika)
        val buttonKombinace: ImageButton = findViewById(R.id.button_kombinace)

        when (selectedButtonTag) {
            "plus" -> buttonPlus.isSelected = true
            "nula" -> buttonNula.isSelected = true
            "minus" -> buttonMinus.isSelected = true
        }

        buttonPlus.setBackgroundResource(if (selectedButtonTag == "plus") R.drawable.icon_selection_background else R.color.polozka)
        buttonNula.setBackgroundResource(if (selectedButtonTag == "nula") R.drawable.icon_selection_background else R.color.polozka)
        buttonMinus.setBackgroundResource(if (selectedButtonTag == "minus") R.drawable.icon_selection_background else R.color.polozka)

        when (selectedButtonTag2) {
            "Síla" -> buttonSila.isSelected = true
            "Technika" -> buttonTechnika.isSelected = true
            "Kombinace" -> buttonKombinace.isSelected = true
        }

        buttonSila.setBackgroundResource(if (selectedButtonTag2 == "Síla") R.drawable.icon_selection_background else R.color.polozka)
        buttonTechnika.setBackgroundResource(if (selectedButtonTag2 == "Technika") R.drawable.icon_selection_background else R.color.polozka)
        buttonKombinace.setBackgroundResource(if (selectedButtonTag2 == "Kombinace") R.drawable.icon_selection_background else R.color.polozka)
    }

    private fun setupSpinner() {
        val difficultyLevels = resources.getStringArray(R.array.Grade)
        val styleLevels = resources.getStringArray(R.array.Style)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)

        val adapterDif = CustomArrayAdapter(this, R.layout.item_spinner, difficultyLevels.toList())
        val adapterStyle = CustomArrayAdapter(this, R.layout.item_spinner, styleLevels.toList())
        adapterDif.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterStyle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        routeGradeSpinner.adapter = adapterDif
        routeStyleSpinner.adapter = adapterStyle
    }

    private fun saveCesta() {
        val routeNameEditText: EditText = findViewById(R.id.nameEditText)
        val fallEditText: EditText = findViewById(R.id.fallEditText)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val minuteEditText: EditText = findViewById(R.id.minutesEditText)
        val secondEditText: EditText = findViewById(R.id.secondsEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val opinionRatingBar: RatingBar = findViewById(R.id.opinionRatingBar)
        val latitudeEditText: EditText = findViewById(R.id.latitudeEditText)
        val longitudeEditText: EditText = findViewById(R.id.longitudeEditText)
        val signImage = if (routeGradeSpinner.selectedItem.toString() == "x") {
            ""
        } else {
            when (selectedButtonTag) {
                "plus" -> "+"
                "nula" -> ""
                "minus" -> "-"
                else -> ""
            }
        }
        val charImage = when (selectedButtonTag2) {
            "Síla" -> "Silová"
            "Technika" -> "Technická"
            "Kombinace" -> "Kombinace"
            else -> ""
        }

        val cestaName = routeNameEditText.text.toString()
        val fallCountString = fallEditText.text.toString()
        val styleSpinner = routeStyleSpinner.selectedItem.toString()
        val gradeSpinner = routeGradeSpinner.selectedItem.toString()
        val minuteString = minuteEditText.text.toString()
        val secondString = secondEditText.text.toString()
        val descriptionroute = descriptionEditText.text.toString()
        val latitude = latitudeEditText.text.toString()
        val longitude = longitudeEditText.text.toString()

        val currentDate = if (selectedDate == 0L) System.currentTimeMillis() else selectedDate

        if (cestaName.isNotBlank()) {
            val timeMinutes: Int = if (minuteString.isNotBlank()) {
                minuteString.toInt()
            } else {
                0
            }

            val timeSecond = if (secondString.isNotBlank()) {
                secondString.toInt()
            } else {
                0
            }

            lifecycleScope.launch {
                val lastCestaId = cestaModel.getLastCestaId() ?: 0
                val existingCesta = currentCesta?.let {
                    cestaController.getCestaById(it.id)

                }


                if (existingCesta==null) {
                    val newCesta = CestaEntity(
                        lastCestaId+1,  // Auto-generate ID
                        cestaName,
                        fallCountString.toIntOrNull() ?: 0,
                        styleSpinner,
                        gradeSpinner,
                        signImage,
                        charImage,
                        minuteString.toIntOrNull() ?: 0,
                        secondString.toIntOrNull() ?: 0,
                        descriptionroute,
                        opinionRatingBar.rating,
                        currentDate,
                        latitude.toDoubleOrNull() ?: 0.0,
                        longitude.toDoubleOrNull() ?: 0.0
                    )
                    cestaModel.insertCesta(newCesta)
                    currentCesta = newCesta
                    isCestaCreated = true
                }

                if (existingCesta!=null) {
                    // Update existing Cesta
                    existingCesta.routeName = cestaName
                    existingCesta.fallCount = fallCountString.toIntOrNull() ?: 0
                    existingCesta.climbStyle = styleSpinner
                    existingCesta.gradeNum = gradeSpinner
                    existingCesta.gradeSign = signImage
                    existingCesta.routeChar = charImage
                    existingCesta.timeMinute = timeMinutes
                    existingCesta.timeSecond = timeSecond
                    existingCesta.description = descriptionroute
                    existingCesta.rating = opinionRatingBar.rating
                    existingCesta.date = currentDate
                    existingCesta.latitude = latitude.toDoubleOrNull() ?: 0.0
                    existingCesta.longitude = longitude.toDoubleOrNull() ?: 0.0

                    cestaModel.updateCesta(existingCesta)
                }
            }
        }
    }

    private fun fillUI(cesta: CestaEntity) {
        val routeNameEditText: EditText = findViewById(R.id.nameEditText)
        val fallEditText: EditText = findViewById(R.id.fallEditText)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val minuteEditText: EditText = findViewById(R.id.minutesEditText)
        val secondEditText: EditText = findViewById(R.id.secondsEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val opinionRatingBar: RatingBar = findViewById(R.id.opinionRatingBar)
        val datePicker: DatePicker = findViewById(R.id.datePicker)
        val latitudeEditText: EditText = findViewById(R.id.latitudeEditText)
        val longitudeEditText: EditText = findViewById(R.id.longitudeEditText)

        routeNameEditText.setText(cesta.routeName)
        fallEditText.setText(cesta.fallCount.toString())
        minuteEditText.setText(cesta.timeMinute.toString())
        secondEditText.setText(cesta.timeSecond.toString())
        descriptionEditText.setText(cesta.description)
        opinionRatingBar.rating = cesta.rating
        latitudeEditText.setText(cesta.latitude.toString())
        longitudeEditText.setText(cesta.longitude.toString())

        // Nastavení vybraného tlačítka
        selectedButtonTag = when (cesta.gradeSign) {
            "+" -> "plus"
            "-" -> "minus"
            else -> "nula"
        }

        selectedButtonTag2 = when (cesta.routeChar) {
            "Silová" -> "Síla"
            "Technická" -> "Technika"
            "Kombinace" -> "Kombinace"
            else -> null
        }

        // Znovu aktualizovat zobrazení vybraného tlačítka
        updateSelectedButtonView()

        // Nastavení data do DatePickeru
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = cesta.date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        datePicker.updateDate(year, month, dayOfMonth)

        // Nastavení pozice v Spinnerch
        val difficultyLevels = resources.getStringArray(R.array.Grade)
        val styleLevels = resources.getStringArray(R.array.Style)

        routeGradeSpinner.setSelection(difficultyLevels.indexOf(cesta.gradeNum))
        routeStyleSpinner.setSelection(styleLevels.indexOf(cesta.climbStyle))
    }

    private fun deleteCesta(cesta: CestaEntity) {
        lifecycleScope.launch {
            if (cestaModel.getAllCesta().size == 1) {
                cestaModel.removeCesta(cesta)
            }
            else {
                cestaModel.removeCesta(cesta)
            }
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}

class WebAppInterface(private val latitudeEditText: EditText, private val longitudeEditText: EditText) {
    @JavascriptInterface
    fun updateCoordinates(latitude: String, longitude: String) {
        // Aktualizovat hodnoty v EditText
        latitudeEditText.setText(latitude)
        longitudeEditText.setText(longitude)
    }
}

