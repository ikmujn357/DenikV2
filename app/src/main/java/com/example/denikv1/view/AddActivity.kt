package com.example.denikv1.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zapis)

        cestaId = calculateNextCestaId()

        setupCustomActionBar()
        setupUIComponents()
        toggleVisibility()
        setupButtons()
        setupSpinner()

        val receivedIntent = intent
        cestaId = receivedIntent.getLongExtra("cestaId", 0)

        if (cestaId != 0L) {
            lifecycleScope.launch {
                val cesta = cestaModel.getCestaById(cestaId)
                currentCesta = cesta
                fillUI(cesta)
            }
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

        val textWatcher = createTextWatcher()
        val itemSelectedListener = createItemSelectedListener()

        routeNameEditText.addTextChangedListener(textWatcher)
        fallEditText.addTextChangedListener(textWatcher)
        routeStyleSpinner.onItemSelectedListener = itemSelectedListener
        routeGradeSpinner.onItemSelectedListener = itemSelectedListener
        minuteEditText.addTextChangedListener(textWatcher)
        descriptionEditText.addTextChangedListener(textWatcher)
        secondEditText.addTextChangedListener(textWatcher)

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
        val timeLayout: LinearLayout = findViewById(R.id.timeLayout)
        val descriptionLayout: LinearLayout = findViewById(R.id.descriptionLayout)
        val opinionLayout: LinearLayout = findViewById(R.id.opinionLayout)

        timeLayout.visibility = View.GONE
        descriptionLayout.visibility = View.GONE
        opinionLayout.visibility = View.GONE

        showHideButton.setOnClickListener {
            if (timeLayout.visibility == View.VISIBLE) {
                // Pokud jsou položky viditelné, skryj je
                timeLayout.visibility = View.GONE
                descriptionLayout.visibility = View.GONE
                opinionLayout.visibility = View.GONE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_right)
            } else {
                // Pokud jsou položky skryté, zobraz je
                timeLayout.visibility = View.VISIBLE
                descriptionLayout.visibility = View.VISIBLE
                opinionLayout.visibility = View.VISIBLE
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
        val signImage = when (selectedButtonTag) {
            "plus" -> "+"
            "nula" -> ""
            "minus" -> "-"
            else -> ""
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
                        currentDate
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

        routeNameEditText.setText(cesta.routeName)
        fallEditText.setText(cesta.fallCount.toString())
        minuteEditText.setText(cesta.timeMinute.toString())
        secondEditText.setText(cesta.timeSecond.toString())
        descriptionEditText.setText(cesta.description)
        opinionRatingBar.rating = cesta.rating

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
        calendar.timeInMillis = cesta.date // předpokládám, že cesta.date je typu Long

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
            finish() // nebo naviguj na jinou obrazovku podle potřeby
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
