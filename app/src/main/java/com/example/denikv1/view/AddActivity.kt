package com.example.denikv1

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class AddActivity : AppCompatActivity() {
    private val cestaModel: CestaModel = CestaModelImpl(this)
    private val cestaController: CestaController = CestaControllerImpl(cestaModel)
    private var selectedDate: Long = 0
    private var selectedButton: ImageButton? = null
    private var selectedButton2: ImageButton? = null
    private var gradeModifier: String = ""
    private var charModifier: String = ""
    private var cestaId: Long = 0
    private var selectedButtonTag: String? = null
    private var selectedButtonTag2: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zapis)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        setupSpinner()
        val showHideButton: LinearLayout = findViewById(R.id.showHideButton)
        val showHideButtonImg: ImageView = findViewById(R.id.showHideButtonImg)
        val timeLayout: LinearLayout = findViewById(R.id.timeLayout)
        val descriptionLayout: LinearLayout = findViewById(R.id.descriptionLayout)
        val opinionLayout: LinearLayout = findViewById(R.id.opinionLayout)

        timeLayout.visibility = View.GONE
        descriptionLayout.visibility = View.GONE
        opinionLayout.visibility = View.GONE

        // Přidání posluchače pro tlačítko showHideButton
        showHideButton.setOnClickListener {
            // Změna viditelnosti položek podle aktuálního stavu
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

        val plusButton: ImageButton = findViewById(R.id.button_plus)
        val nulaButton: ImageButton = findViewById(R.id.button_nula)
        val minusButton: ImageButton = findViewById(R.id.button_minus)

        val silaButton: ImageButton = findViewById(R.id.button_sila)
        val technikaButton: ImageButton = findViewById(R.id.button_technika)
        val kombinaceButton: ImageButton = findViewById(R.id.button_kombinace)

        plusButton.setOnClickListener {
            updateGradeModifier("+", plusButton)
            onButtonClicked(plusButton)
        }

        nulaButton.setOnClickListener {
            updateGradeModifier("", nulaButton)
            onButtonClicked(nulaButton)
        }

        minusButton.setOnClickListener {
            updateGradeModifier("-", minusButton)
            onButtonClicked(minusButton)
        }

        silaButton.setOnClickListener {
            updateCharModifier("Silová", silaButton)
            onButtonClicked2(silaButton)
        }

        technikaButton.setOnClickListener {
            updateCharModifier("Technická", technikaButton)
            onButtonClicked2(technikaButton)
        }

        kombinaceButton.setOnClickListener {
            updateCharModifier("Kombinace", kombinaceButton)
            onButtonClicked2(kombinaceButton)
        }

        val addCestaButton: Button = findViewById(R.id.saveButton)
        addCestaButton.setOnClickListener {
            newCesta()
        }

        val datePicker: DatePicker = findViewById(R.id.datePicker)
        datePicker.setOnDateChangedListener() { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        }

        val receivedIntent = intent
        cestaId = receivedIntent.getLongExtra("cestaId", 0)

        if (cestaId != 0L) {
            lifecycleScope.launch {
                val cesta = cestaModel.getCestaById(cestaId)
                fillUI(cesta)
            }
        }
    }

    fun onButtonClicked(view: View) {
        // Odmáčkne předchozí tlačítko, pokud existuje
        selectedButton?.isSelected = false

        view.isSelected = true

        selectedButtonTag = when (view.id) {
            R.id.button_plus -> "plus"
            R.id.button_nula -> "nula"
            R.id.button_minus -> "minus"
            else -> null
        }
        selectedButton = view as? ImageButton



        // Aktualizujte barvy tlačítek
        updateSelectedButtonView()
    }

    fun onButtonClicked2(view: View) {
        // Odmáčkne předchozí tlačítko, pokud existuje
        selectedButton2?.isSelected = false

        view.isSelected = true

        selectedButtonTag2 = when (view.id) {
            R.id.button_sila -> "Síla"
            R.id.button_technika -> "Technika"
            R.id.button_kombinace -> "Kombinace"
            else -> null
        }
        selectedButton2 = view as? ImageButton

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


        buttonPlus.setBackgroundResource(if (selectedButtonTag == "plus") R.drawable.icon_selection_background else R.color.white)
        buttonNula.setBackgroundResource(if (selectedButtonTag == "nula") R.drawable.icon_selection_background else R.color.white)
        buttonMinus.setBackgroundResource(if (selectedButtonTag == "minus") R.drawable.icon_selection_background else R.color.white)

        when (selectedButtonTag2) {
            "Síla" -> buttonSila.isSelected = true
            "Technika" -> buttonTechnika.isSelected = true
            "Kombinace" -> buttonKombinace.isSelected = true
        }


        buttonSila.setBackgroundResource(if (selectedButtonTag2 == "Síla") R.drawable.icon_selection_background else R.color.white)
        buttonTechnika.setBackgroundResource(if (selectedButtonTag2 == "Technika") R.drawable.icon_selection_background else R.color.white)
        buttonKombinace.setBackgroundResource(if (selectedButtonTag2 == "Kombinace") R.drawable.icon_selection_background else R.color.white)

    }






    private fun setupSpinner() {
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)

        val difficultyLevels = resources.getStringArray(R.array.Grade)
        val styleLevels = resources.getStringArray(R.array.Style)
        val characterLevels = resources.getStringArray(R.array.Character)

        val adapterDif = CustomArrayAdapter(this, R.layout.item_spinner, difficultyLevels.toList())
        val adapterStyle = CustomArrayAdapter(this, R.layout.item_spinner, styleLevels.toList())
        val adapterCharacter = CustomArrayAdapter(this, R.layout.item_spinner, characterLevels.toList())

        routeGradeSpinner.adapter = adapterDif
        routeStyleSpinner.adapter = adapterStyle

        adapterDif.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterStyle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterCharacter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        routeGradeSpinner.adapter = adapterDif
        routeStyleSpinner.adapter = adapterStyle
    }

    private fun updateGradeModifier(value: String, button: ImageButton) {
        gradeModifier = value
        selectedButton?.isSelected = false
        button.isSelected = true
        selectedButton = button
    }





    private fun updateCharModifier(value: String, button: ImageButton) {
        charModifier = value
        selectedButton2?.isSelected = false
        button.isSelected = true
        selectedButton2 = button
    }


    private fun newCesta() {
        val routeNameEditText: EditText = findViewById(R.id.nameEditText)
        val fallEditText: EditText = findViewById(R.id.fallEditText)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val signImage = when {
            selectedButtonTag == "plus" -> "+"
            selectedButtonTag == "nula" -> ""
            selectedButtonTag == "minus" -> "-"
            else -> ""
        }
        val charImage = when {
            selectedButtonTag2 == "Síla" -> "Silová"
            selectedButtonTag2 == "Technika" -> "Technická"
            selectedButtonTag2 == "Kombinace" -> "Kombinace"
            else -> ""
        }
        val minuteEditText: EditText = findViewById(R.id.minutesEditText)
        val secondEditText: EditText = findViewById(R.id.secondsEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val opinionEditText: EditText = findViewById(R.id.opinionEditText)

        val cestaName = routeNameEditText.text.toString()
        val fallCountString = fallEditText.text.toString()
        val styleSpinner = routeStyleSpinner.selectedItem.toString()
        val gradeSpinner = routeGradeSpinner.selectedItem.toString()

        val minuteString = minuteEditText.text.toString()
        val secondString = secondEditText.text.toString()
        val descriptionroute = descriptionEditText.text.toString()
        val opinionroute = opinionEditText.text.toString()

        val currentDate = if (selectedDate == 0L) System.currentTimeMillis() else selectedDate

        if (cestaName.isNotBlank() && fallCountString.isNotBlank() && styleSpinner.isNotBlank()
            && gradeSpinner.isNotBlank() && charImage.isNotBlank()) {

            var timeMinutes = 0
            var timeSecond = 0

            timeMinutes = if(minuteString.isNotBlank()) {
                minuteString.toInt()
            } else {
                0
            }

            timeSecond = if(secondString.isNotBlank()) {
                secondString.toInt()
            } else {
                0
            }

            val newCesta = CestaEntity(
                routeName = cestaName,
                fallCount = fallCountString.toInt(),
                climbStyle = styleSpinner,
                gradeNum = gradeSpinner,
                gradeSign = signImage,
                routeChar = charImage,
                timeMinute = timeMinutes,
                timeSecond = timeSecond,
                description = descriptionroute,
                opinion = opinionroute,
                date = currentDate
            )

            lifecycleScope.launch {
                if (cestaId != 0L) {
                    val existingCesta = cestaModel.getCestaById(cestaId)
                    existingCesta.apply {
                        this.routeName = cestaName
                        this.fallCount = fallCountString.toInt()
                        this.climbStyle = styleSpinner
                        this.gradeNum = gradeSpinner
                        this.gradeSign = signImage
                        this.routeChar = charImage
                        this.timeMinute = timeMinutes
                        this.timeSecond = timeSecond
                        this.description = descriptionroute
                        this.opinion = opinionroute
                        this.date = currentDate
                    }

                    cestaModel.addOrUpdateCesta(existingCesta)

                    cestaController.showToast("Cesta aktualizována!", Toast.LENGTH_LONG)
                    finish()
                } else {
                    cestaModel.addOrUpdateCesta(newCesta)

                    cestaController.showToast("Cesta přidána!", Toast.LENGTH_LONG)
                    finish()
                }
            }
        } else {
            cestaController.showToast("Nevyplnil jste všechno.", Toast.LENGTH_SHORT)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun fillUI(cesta: CestaEntity) {
        val routeNameEditText: EditText = findViewById(R.id.nameEditText)
        val fallEditText: EditText = findViewById(R.id.fallEditText)
        val routeStyleSpinner: Spinner = findViewById(R.id.styleSpinner)
        val routeGradeSpinner: Spinner = findViewById(R.id.difficultySpinner)
        val minuteEditText: EditText = findViewById(R.id.minutesEditText)
        val secondEditText: EditText = findViewById(R.id.secondsEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val opinionEditText: EditText = findViewById(R.id.opinionEditText)
        val datePicker: DatePicker = findViewById(R.id.datePicker)

        routeNameEditText.setText(cesta.routeName)
        fallEditText.setText(cesta.fallCount.toString())
        minuteEditText.setText(cesta.timeMinute.toString())
        secondEditText.setText(cesta.timeSecond.toString())
        descriptionEditText.setText(cesta.description)
        opinionEditText.setText(cesta.opinion)

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

}
