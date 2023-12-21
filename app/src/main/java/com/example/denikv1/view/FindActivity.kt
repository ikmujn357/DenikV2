package com.example.denikv1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private lateinit var nameEditText: EditText
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutByName: LinearLayout
    private lateinit var layoutByDate: LinearLayout
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vyhledavani)

        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerViewVyhledavani)
        nameEditText = findViewById(R.id.nameEditText)
        layoutByName = findViewById(R.id.layoutByName)
        layoutByDate = findViewById(R.id.layoutByDate)
        radioGroup = findViewById(R.id.searchOptions)

        calendarView.date = System.currentTimeMillis()

        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        loadAndDisplayCesty(currentDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            onDateChanged(year, month, dayOfMonth)
        }



        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioName -> {
                    (recyclerView.adapter as? CestaAdapter)?.clear()
                    showLayoutByName()
                }
                R.id.radioDate -> {
                    showLayoutByDate()
                    loadAndDisplayCestyForCurrentDate()
                }
            }
        }

        showLayoutByName()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f

        // Přidání TextWatcheru k EditText
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nepotřebujeme tuto metodu
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nepotřebujeme tuto metodu
            }

            override fun afterTextChanged(s: Editable?) {
                // Po změně textu provedeme hledání
                if (s.isNullOrBlank()) {
                    clearSearch()
                } else {
                    onFindButtonClick()
                }
            }
        })
    }

    private fun clearSearch() {
        (recyclerView.adapter as? CestaAdapter)?.clear()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun onDateChanged(year: Int, month: Int, dayOfMonth: Int) {
        val selectedDateStart = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val selectedDateEnd = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        loadAndDisplayCesty(selectedDateStart, selectedDateEnd)
    }

    private fun onFindButtonClick() {
        if (isLayoutByNameVisible()) {
            val searchName = nameEditText.text.toString()
            if (searchName.isNotBlank()) {
                loadAndDisplayCesty(searchName)
            }
        } else {
            val selectedDate = calendarView.date
            loadAndDisplayCesty(selectedDate)
        }
    }

    private fun loadAndDisplayCesty(startDate: Long, endDate: Long = startDate) {
        val controller = CestaControllerImpl(CestaModelImpl(this))
        lifecycleScope.launch {
            try {
                val cesty = controller.getAllCestaForDateRange(startDate, endDate)
                updateRecyclerView(cesty)
            } catch (e: Exception) {
                Log.e("FindActivity", "Nelze načíst cesty", e)
            }
        }
    }

    private fun loadAndDisplayCesty(searchName: String) {
        val controller = CestaControllerImpl(CestaModelImpl(this))
        lifecycleScope.launch {
            try {
                val cesty = controller.getAllCestaByName(searchName)
                updateRecyclerView(cesty)
            } catch (e: Exception) {
                Log.e("FindActivity", "Nelze načíst cesty", e)
            }
        }
    }

    private fun loadAndDisplayCestyForCurrentDate() {
        val currentCalendar = Calendar.getInstance()
        onDateChanged(
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun updateRecyclerView(cesty: List<CestaEntity>) {
        val layoutManager = LinearLayoutManager(this@FindActivity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CestaAdapter(cesty) { cestaId ->
            val intent = Intent(this@FindActivity, AddActivity::class.java)
            intent.putExtra("cestaId", cestaId)
            startActivity(intent)
        }
    }

    private fun isLayoutByNameVisible(): Boolean {
        return layoutByName.visibility == LinearLayout.VISIBLE
    }

    private fun showLayoutByName() {
        layoutByName.visibility = LinearLayout.VISIBLE
        layoutByDate.visibility = LinearLayout.GONE
    }

    private fun showLayoutByDate() {
        layoutByName.visibility = LinearLayout.GONE
        layoutByDate.visibility = LinearLayout.VISIBLE

        calendarView.date = System.currentTimeMillis()
        onDateChanged(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
