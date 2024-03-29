package com.usbapps.climbingdiary.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.usbapps.climbingdiary.R
import com.usbapps.climbingdiary.controller.CestaController
import com.usbapps.climbingdiary.controller.CestaControllerImpl
import com.usbapps.climbingdiary.custom.CustomArrayAdapter
import com.usbapps.climbingdiary.model.CestaEntity
import com.usbapps.climbingdiary.model.CestaModel
import com.usbapps.climbingdiary.model.CestaModelImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import android.webkit.JavascriptInterface
import android.widget.ScrollView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.usbapps.climbingdiary.custom.LocationHelper
import com.usbapps.climbingdiary.custom.MyLocationListener
import kotlinx.coroutines.CoroutineScope

class AddActivity : AppCompatActivity() {
    private val cestaModel: CestaModel = CestaModelImpl(this)
    private val cestaController: CestaController = CestaControllerImpl(cestaModel)
    private var selectedDate: Long = 0
    private var selectedButton: ImageButton? = null
    private var selectedButton2: ImageButton? = null
    private var selectedButton3: ImageButton? = null
    private var cestaId: Long = 0
    private var selectedButtonTag: String? = null
    private var selectedButtonTag2: String? = null
    private var selectedButtonTag3: String? = null
    private var isCestaCreated: Boolean = false
    private var currentCesta: CestaEntity? = null
    private var isCestaDeleted: Boolean = false
    private var oldCesta: CestaEntity? = null
    lateinit var locationHelper: LocationHelper
    private val locationListener = MyLocationListener(this)
    private lateinit var layoutUIAA: LinearLayout
    private lateinit var layoutFrench: LinearLayout

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zapis)
        cestaId = calculateNextCestaId()
        setupCustomActionBar()
        setupUIComponents()
        toggleVisibility()
        setupButtons()
        showGradeSystem()
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
        // Inicializace LocationHelper
        locationHelper = LocationHelper(this)
        // Kontrola oprávnění
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Oprávnění jsou udělena, pokračujte v získávání polohy
            locationHelper.requestLocationUpdates(locationListener)
        } else {
            // Požádat o oprávnění
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }
        updateEditTextWithLastLocation()
    }

    private fun setupGradeSpinner(stringArrayId: Int) {
        val resources: Resources = resources
        val gradeLevels = resources.getStringArray(stringArrayId)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val adapter = CustomArrayAdapter(this, R.layout.item_spinner, gradeLevels.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        routeGradeSpinner.adapter = adapter
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    fun updateEditTextWithLastLocation() {
        val latitudeEditText = findViewById<EditText>(R.id.latitudeEditText)
        val longitudeEditText = findViewById<EditText>(R.id.longitudeEditText)
        val webView: WebView = findViewById(R.id.webView)
        val scrollView: ScrollView = findViewById(R.id.scrollViewZapis)
        val bottomDistanceThreshold = 1050 // Vzdálenost odspodu v px, po kterou se ScrollView nebude scrollovat

        webView.setOnTouchListener { _, event ->
            val webViewLocation = IntArray(2)
            webView.getLocationOnScreen(webViewLocation)

            val webViewTop = webViewLocation[1]
            val webViewBottom = webViewTop + webView.height

            // Zjistíme, zda se dotyk nachází v WebView
            val isTouchInWebView = event.rawY >= webViewTop && event.rawY <= webViewBottom

            // Zjistíme, zda se dotyk nachází dostatečně daleko odspodu
            val isTouchFarEnoughFromBottom = event.rawY >= webViewBottom - bottomDistanceThreshold

            // Zabránění posunu ScrollView, pokud je dotyk uvnitř WebView a zároveň dostatečně daleko odspodu
            if (isTouchInWebView && isTouchFarEnoughFromBottom) {
                scrollView.requestDisallowInterceptTouchEvent(true)
                Log.d("mapa", "je v WebView a je dostatečně daleko odspodu")
            } else {
                scrollView.requestDisallowInterceptTouchEvent(false)
                Log.d("mapa", "není v WebView nebo není dostatečně daleko odspodu")
            }

            false // Vrátíme false, aby se prováděly další dotyky
        }

        // Nastavení cesty k HTML souboru s mapou
        webView.settings.javaScriptEnabled = true
        // Přidáme rozhraní pro žádost o aktualizace polohy
        webView.addJavascriptInterface(
            object {
                @JavascriptInterface
                fun requestLocationUpdates() {
                    // Zde můžete volat kód pro žádost o aktualizace polohy ze záznamníku polohy
                    // Tento kód by měl být podobný tomu, co jste měli v posluchači kliknutí na tlačítko
                    locationListener.lastLocation?.let { lastLocation ->
                        val latitude = lastLocation.latitude
                        val longitude = lastLocation.longitude

                        // Aktualizovat hodnoty v EditText prvcích
                        latitudeEditText.setText(latitude.toString())
                        longitudeEditText.setText(longitude.toString())

                        // Zavoláme JavaScriptovou funkci k aktualizaci mapy s přijatou polohou
                        webView.post { webView.loadUrl("javascript:updateMapToCurrentLocation($latitude, $longitude);") }
                    }
                }
            }, "LocationRequestInterface"
        )
        // Přidáme rozhraní WebAppInterface pro manipulaci s políčky pro zeměpisnou šířku a délku
        webView.addJavascriptInterface(
            object {
                @JavascriptInterface
                fun updateCoordinates(latitude: Double, longitude: Double) {
                    // Spustíme novou coroutine
                    CoroutineScope(Dispatchers.Main).launch {
                        // Aktualizujeme hodnoty v EditText
                        latitudeEditText.setText(latitude.toString())
                        longitudeEditText.setText(longitude.toString())
                    }
                }
            },
            "Android"
        )
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/leaflet_map.html")


    }

    private fun loadCestaLocationOnMap(cesta: CestaEntity) {
        val webView: WebView = findViewById(R.id.webView)
        val latitude = cesta.latitude
        val longitude = cesta.longitude
        val javascriptCommand = "updateMapToCurrentLocation($latitude, $longitude);"
        webView.post { webView.loadUrl("javascript:$javascriptCommand") }
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

    private fun revertChangesButton() {
        val buttonShowAdd: Button = findViewById(R.id.button_vratit_zmeny)
        buttonShowAdd.setOnClickListener {
            if (cestaId.toInt() == 0) {
                lifecycleScope.launch {
                    onBackPressedDispatcher.onBackPressed()
                    currentCesta?.let { deleteCesta(it) }
                }
            }
            else {
                lifecycleScope.launch {
                    oldCesta?.let { it1 -> cestaModel.updateCesta(it1) }
                    onBackPressedDispatcher.onBackPressed()
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

    private fun showGradeSystem() {
        val buttonUIAA = findViewById<Button>(R.id.button_UIAA)
        val buttonFrench = findViewById<Button>(R.id.button_French)

        layoutUIAA = findViewById(R.id.topPanel)
        layoutFrench = findViewById(R.id.topPanelFrench)
        buttonUIAA.setBackgroundResource(R.drawable.rectangle_grade_button)
        buttonFrench.setBackgroundResource(android.R.color.transparent)


        layoutUIAA.visibility = View.VISIBLE
        layoutFrench.visibility = View.GONE

        buttonUIAA.setOnClickListener {
            layoutUIAA.visibility = View.VISIBLE
            layoutFrench.visibility = View.GONE
            setupGradeSpinner(R.array.GradeUIAA)
            selectedButtonTag3 = null
            selectedButton3 = null
            selectedButtonTag = null
            selectedButton = null

            buttonUIAA.setBackgroundResource(R.drawable.rectangle_grade_button)
            buttonFrench.setBackgroundResource(android.R.color.transparent)
        }
        buttonFrench.setOnClickListener {
            selectedButtonTag = null
            selectedButton = null
            // Nastavit viditelnost layoutu French na VISIBLE
            layoutFrench.visibility = View.VISIBLE
            layoutUIAA.visibility = View.GONE
            setupGradeSpinner(R.array.GradeFrench)
            buttonFrench.setBackgroundResource(R.drawable.rectangle_grade_button)
            buttonUIAA.setBackgroundResource(android.R.color.transparent)
        }

        updateSelectedButtonView()
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
                showHideButtonImg.setImageResource(R.drawable.arrow_right)
            } else {
                // Pokud jsou položky skryté, zobraz je
                descriptionAll.visibility = View.VISIBLE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_down)
                currentCesta?.let { cesta -> loadCestaLocationOnMap(cesta) }
            }
        }
    }

    private fun setupButtons() {
        val gradeButtons = listOf(
            R.id.button_plus to "+",
            R.id.button_nula to "",
            R.id.button_minus to "-",
            R.id.button_a to "a",
            R.id.button_b to "b",
            R.id.button_c to "c"
        )
        val characterButtons = listOf(
            R.id.button_sila to "Silová",
            R.id.button_technika to "Technická",
            R.id.button_kombinace to "Kombinace"
        )

        val frenchPlusButtons = listOf(
            R.id.button_plusFrench to "plusfrench"

        )

        gradeButtons.forEach { (buttonId, actionName) ->
            setupButtonAction(actionName, findViewById(buttonId))
        }

        characterButtons.forEach { (buttonId, actionName) ->
            setupButtonAction(actionName, findViewById(buttonId))
        }

        frenchPlusButtons.forEach { (buttonId, actionName) ->
            setupButtonAction(actionName, findViewById(buttonId))
        }
    }

    private fun setupButtonAction(actionName: String, button: ImageButton) {
        button.setOnClickListener {
            // Pokud klikáme na tlačítko plusFrench
            if (actionName == "plusfrench") {
                // Pokud je tlačítko již vybráno, odznačíme ho
                if (button.isSelected) {
                    button.isSelected = false
                    selectedButtonTag3 = null
                    selectedButton3 = null
                } else { // Jinak ho označíme
                    button.isSelected = true
                    selectedButtonTag3 = "plusfrench"
                    selectedButton3 = button
                }
            } else { // Pokud klikáme na jiné tlačítko
                val isSelectedButton = when (actionName) {
                    in listOf("+", "", "-", "a", "b", "c") -> selectedButton
                    in listOf("Silová", "Technická", "Kombinace") -> selectedButton2
                    else -> null
                }

                // Odmáčkne předchozí tlačítko, pokud existuje
                isSelectedButton?.isSelected = false
                button.isSelected = true
                when (actionName) {
                    in listOf("+", "", "-", "a", "b", "c") -> {
                        selectedButtonTag = actionName
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
            selectedButton3 -> selectedButton3?.isSelected = false
        }

        // Pokud klikáme na tlačítko plusFrench
        if (view.id == R.id.button_plusFrench) {
            // Invertovat stav tlačítka (přepnout mezi označením a odznačením)
            selectedButton3?.isSelected = !(selectedButton3?.isSelected ?: false)
            if (selectedButton3?.isSelected == true) {
                selectedButtonTag3 = "plusfrench"
                selectedButton3 = view as? ImageButton
            } else {
                selectedButtonTag3 = null
                selectedButton3 = null
            }
        } else { // Pokud klikáme na jiné tlačítko
            view.isSelected = true
            when (view.id) {
                R.id.button_plus, R.id.button_nula, R.id.button_minus -> {
                    selectedButtonTag = when (view.id) {
                        R.id.button_plus -> "plus"
                        R.id.button_nula -> "nula"
                        R.id.button_minus -> "minus"
                        R.id.button_a -> "a"
                        R.id.button_b -> "b"
                        R.id.button_c -> "c"
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
        }

        // Aktualizujte barvy tlačítek
        updateSelectedButtonView()
    }


    private fun updateSelectedButtonView() {
        val buttonPlus: ImageButton = findViewById(R.id.button_plus)
        val buttonNula: ImageButton = findViewById(R.id.button_nula)
        val buttonMinus: ImageButton = findViewById(R.id.button_minus)

        val buttonA: ImageButton = findViewById(R.id.button_a)
        val buttonB: ImageButton = findViewById(R.id.button_b)
        val buttonC: ImageButton = findViewById(R.id.button_c)

        val buttonPlusFrench: ImageButton = findViewById(R.id.button_plusFrench)

        val buttonSila: ImageButton = findViewById(R.id.button_sila)
        val buttonTechnika: ImageButton = findViewById(R.id.button_technika)
        val buttonKombinace: ImageButton = findViewById(R.id.button_kombinace)

        when (selectedButtonTag) {
            "plus" -> buttonPlus.isSelected = true
            "nula" -> buttonNula.isSelected = true
            "minus" -> buttonMinus.isSelected = true
            "a" -> buttonA.isSelected = true
            "b" -> buttonB.isSelected = true
            "c" -> buttonC.isSelected = true
        }

        buttonPlus.setBackgroundResource(if (selectedButtonTag == "plus") R.drawable.icon_selection_background else R.color.polozka)
        buttonNula.setBackgroundResource(if (selectedButtonTag == "nula") R.drawable.icon_selection_background else R.color.polozka)
        buttonMinus.setBackgroundResource(if (selectedButtonTag == "minus") R.drawable.icon_selection_background else R.color.polozka)
        buttonA.setBackgroundResource(if (selectedButtonTag == "a") R.drawable.icon_selection_background else R.color.polozka)
        buttonB.setBackgroundResource(if (selectedButtonTag == "b") R.drawable.icon_selection_background else R.color.polozka)
        buttonC.setBackgroundResource(if (selectedButtonTag == "c") R.drawable.icon_selection_background else R.color.polozka)

        when (selectedButtonTag2) {
            "Síla" -> buttonSila.isSelected = true
            "Technika" -> buttonTechnika.isSelected = true
            "Kombinace" -> buttonKombinace.isSelected = true
        }
        buttonSila.setBackgroundResource(if (selectedButtonTag2 == "Síla") R.drawable.icon_selection_background else R.color.polozka)
        buttonTechnika.setBackgroundResource(if (selectedButtonTag2 == "Technika") R.drawable.icon_selection_background else R.color.polozka)
        buttonKombinace.setBackgroundResource(if (selectedButtonTag2 == "Kombinace") R.drawable.icon_selection_background else R.color.polozka)

        when (selectedButtonTag3) {
            "plusfrench" -> buttonPlusFrench.isSelected = true
        }
        buttonPlusFrench.setBackgroundResource(if (selectedButtonTag3 == "plusfrench") R.drawable.icon_selection_background else R.color.polozka)
    }

    private fun setupSpinner() {
        val difficultyLevels = resources.getStringArray(R.array.GradeUIAA)
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
                "a" -> "a"
                "b" -> "b"
                "c" -> "c"
                else -> ""
            }
        }
        val charImage = when (selectedButtonTag2) {
            "Síla" -> "Silová"
            "Technika" -> "Technická"
            "Kombinace" -> "Kombinace"
            else -> ""
        }


        val frenchPlus = if (routeGradeSpinner.selectedItem.toString() == "x") {
            ""
        } else {
            when (selectedButtonTag3) {
                "plusfrench" -> "+"
                else -> ""
            }
        }


        val cestaName = routeNameEditText.text.toString()
        val fallCountString = fallEditText.text.toString()
        val styleSpinner = routeStyleSpinner.selectedItem.toString()
        val gradeSpinner = routeGradeSpinner.selectedItem.toString() + signImage + frenchPlus
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
        val buttonUIAA = findViewById<Button>(R.id.button_UIAA)
        val buttonFrench = findViewById<Button>(R.id.button_French)
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

        val gradeLevels = resources.getStringArray(R.array.GradeUIAA)
        val styleLevels = resources.getStringArray(R.array.Style)

        // Rozdělení hodnoty gradeNUM
        val gradeNumParts = cesta.gradeNum.split(Regex("(?<=\\d)(?=[+\\-a-zA-Z])|(?<=[+\\-a-zA-Z])"))
        println("Části: ${gradeNumParts.joinToString(" ")}")


        // Nastavení hodnoty čísla do spinneru
        routeGradeSpinner.setSelection(gradeLevels.indexOf(gradeNumParts[0]))
        gradeNumParts.forEach { println(it) }
        // Nastavení vybraného tlačítka znaku
        selectedButtonTag = when (gradeNumParts.getOrNull(1)) {
            "+", "-" -> {
                // Show topPanel and hide topPanelFrench
                layoutUIAA.visibility = View.VISIBLE
                layoutFrench.visibility = View.GONE
                buttonUIAA.setBackgroundResource(R.drawable.rectangle_grade_button)
                buttonFrench.setBackgroundResource(android.R.color.transparent)
                if (gradeNumParts.getOrNull(1) == "+") "plus" else "minus"
            }
            "a", "b", "c" -> {
                // Show topPanelFrench and hide topPanel
                layoutUIAA.visibility = View.GONE
                layoutFrench.visibility = View.VISIBLE
                buttonFrench.setBackgroundResource(R.drawable.rectangle_grade_button)
                buttonUIAA.setBackgroundResource(android.R.color.transparent)
                gradeNumParts.getOrNull(1)
            }
            else -> {
                layoutUIAA.visibility = View.VISIBLE
                layoutFrench.visibility = View.GONE
                "nula"
            }
        }

        selectedButtonTag = when (gradeNumParts.getOrNull(1)) {
            "+" -> "plus"
            "-" -> "minus"
            "a" -> "a"
            "b" -> "b"
            "c" -> "c"
            else -> "nula"
        }


        selectedButtonTag2 = when (cesta.routeChar) {
            "Silová" -> "Síla"
            "Technická" -> "Technika"
            "Kombinace" -> "Kombinace"
            else -> null
        }

        // Aktualizace odkazu na vybrané tlačítko pro informace o stylu
        selectedButton2 = when (selectedButtonTag2) {
            "Síla" -> findViewById(R.id.button_sila)
            "Technika" -> findViewById(R.id.button_technika)
            "Kombinace" -> findViewById(R.id.button_kombinace)
            else -> null
        }

        selectedButtonTag3 = when (gradeNumParts.getOrNull(2)) {
            "+" -> "plusfrench"
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
