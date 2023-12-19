package com.example.denikv1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Calendar

class FindActivity : AppCompatActivity() {

    // Deklarace proměnných pro odkazy na UI prvky
    private lateinit var nameEditText: EditText
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutByName: LinearLayout
    private lateinit var layoutByDate: LinearLayout
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vyhledavani)

        // Inicializace UI prvků
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerViewVyhledavani)
        nameEditText = findViewById(R.id.nameEditText)
        layoutByName = findViewById(R.id.layoutByName)
        layoutByDate = findViewById(R.id.layoutByDate)
        radioGroup = findViewById(R.id.searchOptions)

        // Nastavení aktuálního data pro CalendarView
        calendarView.date = System.currentTimeMillis()

        // Nastavení aktuálního data pro vyhledávání cest
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Načtení a zobrazení cest pro aktuální den
        loadAndDisplayCesty(currentDate)

        // Nastavení Listener pro změnu data v CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            onDateChanged(year, month, dayOfMonth)
        }

        // Nastavení Listener pro tlačítko hledání
        val findButton: Button = findViewById(R.id.findButton)
        findButton.setOnClickListener {
            onFindButtonClick()
        }

        // Nastavení Listener pro změnu výběru v RadioGroup
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioName -> showLayoutByName()
                R.id.radioDate -> showLayoutByDate()
            }
        }

        // Zobrazení layoutu pro vyhledávání podle názvu při startu aktivity
        showLayoutByName()

        // Nastavení tlačítka zpět v akčním baru
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
    }

    // Metoda pro změnu vybraného data v CalendarView
    private fun onDateChanged(year: Int, month: Int, dayOfMonth: Int) {
        // Nastavení začátku a konce vybraného dne pro načtení cest
        val selectedDateStart = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val selectedDateEnd = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Načtení a zobrazení cest pro vybraný den
        loadAndDisplayCesty(selectedDateStart, selectedDateEnd)
    }

    // Metoda pro obsluhu tlačítka hledání
    private fun onFindButtonClick() {
        // Zjištění, zda je zobrazen layout pro hledání podle názvu
        if (isLayoutByNameVisible()) {
            val searchName = nameEditText.text.toString()
            // Pokud je vyplněný název, načti a zobraz cesty
            if (searchName.isNotBlank()) {
                loadAndDisplayCesty(searchName)
            }
        } else {
            // Pokud je zobrazen layout pro hledání podle data, načti a zobraz cesty pro vybrané datum
            val selectedDate = calendarView.date
            loadAndDisplayCesty(selectedDate)
        }
    }

    // Metoda pro načtení a zobrazení cest pro zadané datum nebo název
    private fun loadAndDisplayCesty(startDate: Long, endDate: Long = startDate) {
        val controller = CestaControllerImpl(CestaModelImpl(this))
        // Spuštění asynchronní operace načítání cest
        lifecycleScope.launch {
            try {
                val cesty = controller.getAllCestaForDateRange(startDate, endDate)
                // Aktualizace obsahu RecyclerView
                updateRecyclerView(cesty)
            } catch (e: Exception) {
                // Logování chyby v případě neúspěchu
                Log.e("FindActivity", "Nelze načíst cesty", e)
            }
        }
    }

    // Metoda pro načtení a zobrazení cest pro zadaný název
    private fun loadAndDisplayCesty(searchName: String) {
        val controller = CestaControllerImpl(CestaModelImpl(this))
        // Spuštění asynchronní operace načítání cest
        lifecycleScope.launch {
            try {
                val cesty = controller.getAllCestaByName(searchName)
                // Aktualizace obsahu RecyclerView
                updateRecyclerView(cesty)
            } catch (e: Exception) {
                // Logování chyby v případě neúspěchu
                Log.e("FindActivity", "Nelze načíst cesty", e)
            }
        }
    }

    // Metoda pro aktualizaci obsahu RecyclerView
    private fun updateRecyclerView(cesty: List<CestaEntity>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CestaAdapter(cesty) { cestaId ->
            // Přesměrování na AddActivity s předáním ID cesty
            val intent = Intent(this@FindActivity, AddActivity::class.java)
            intent.putExtra("cestaId", cestaId)
            startActivity(intent)
        }
    }

    // Metoda pro kontrolu, zda je zobrazen layout pro hledání podle názvu
    private fun isLayoutByNameVisible(): Boolean {
        return layoutByName.visibility == LinearLayout.VISIBLE
    }

    // Metoda pro zobrazení layoutu pro hledání podle názvu
    private fun showLayoutByName() {
        layoutByName.visibility = LinearLayout.VISIBLE
        layoutByDate.visibility = LinearLayout.GONE
    }

    // Metoda pro zobrazení layoutu pro hledání podle data
    private fun showLayoutByDate() {
        layoutByName.visibility = LinearLayout.GONE
        layoutByDate.visibility = LinearLayout.VISIBLE
    }

    // Metoda pro nastavení akce tlačítka zpět
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
