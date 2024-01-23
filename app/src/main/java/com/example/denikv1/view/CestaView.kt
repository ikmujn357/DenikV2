package com.example.denikv1.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.denikv1.R
import com.example.denikv1.controller.CestaController
import com.example.denikv1.controller.CestaControllerImpl
import com.example.denikv1.custom.CestaAdapter
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.model.CestaModel
import com.example.denikv1.model.CestaModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

interface CestaView {
    fun displayCesty()
    fun addButton()
    fun findButton()
    fun statisticsButton()
    fun exportButton()
}

// Implementace view seznamu cest
class CestaViewImp : AppCompatActivity(), CestaView, CoroutineScope by MainScope() {
    private lateinit var controller: CestaController
    private lateinit var customActionBarButton: View
    private lateinit var nameEditText: EditText
    private lateinit var recyclerView: RecyclerView

    // Metoda volaná při vytvoření aktivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Inicializace repozitáře pro práci s cestami
        val cestaRepository: CestaModel = CestaModelImpl(this)
        controller = CestaControllerImpl(cestaRepository)

        // Inicializace customActionBarButton
        customActionBarButton = layoutInflater.inflate(R.layout.custom_bar_main, null)

        // Odstranění stínu z action baru
        supportActionBar?.elevation = 0f
        supportActionBar?.customView = customActionBarButton
        supportActionBar?.setDisplayShowCustomEnabled(true)

        // Zobrazení seznamu cest a tlačítek pro přidání cesty, vyhledání a statistiky
        displayCesty()
        addButton()
        findButton()
        statisticsButton()
        exportButton()

        nameEditText = findViewById(R.id.nameEditText)
        recyclerView = findViewById(R.id.recyclerView)
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
                    // Pokud je pole prázdné, zobrazíme všechny cesty
                    displayCesty()
                } else {
                    // Jinak provedeme filtrování podle zadaného textu
                    filterCestyByName(s.toString())
                }
            }
        })
    }

    private fun filterCestyByName(searchText: String) {
        lifecycleScope.launch {
            val filteredCesty = controller.getAllCestaByName(searchText)
            updateRecyclerView(filteredCesty)
        }
    }

    private fun updateRecyclerView(cesty: List<CestaEntity>) {
        val layoutManager = LinearLayoutManager(this@CestaViewImp)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CestaAdapter(cesty) { cestaId ->
            val intent = Intent(this@CestaViewImp, AddActivity::class.java)
            intent.putExtra("cestaId", cestaId)
            startActivity(intent)
        }
    }



    // Metoda pro zobrazení seznamu cest
    override fun displayCesty() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        lifecycleScope.launch(Dispatchers.Main) {
            val layoutManager = LinearLayoutManager(this@CestaViewImp)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            recyclerView.layoutManager = layoutManager

            val adapter = CestaAdapter(controller.getAllCesta().filter { it.routeName != "" }) { cestaId ->
                // Přesměrování na AddActivity s předáním ID cesty
                val intent = Intent(this@CestaViewImp, AddActivity::class.java)
                intent.putExtra("cestaId", cestaId)
                startActivity(intent)
            }

            recyclerView.adapter = adapter
        }
    }

    // Metoda pro přidání tlačítka pro přidání nové cesty
    override fun addButton() {
        val buttonShowAdd: ImageButton = findViewById(R.id.button_add)
        buttonShowAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)


        }}


    // Metoda pro přidání tlačítka pro vyhledávání
    override fun findButton() {
        val buttonShowFind: Button = customActionBarButton.findViewById(R.id.button_find)
        buttonShowFind.setOnClickListener {
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
        }
    }

    // Metoda pro přidání tlačítka pro zobrazení statistik
    override fun statisticsButton() {
        val buttonShowStatistics: Button = customActionBarButton.findViewById(R.id.button_statistics)
        buttonShowStatistics.setOnClickListener {
            val intent = Intent(this, ShowStatistics::class.java)
            startActivity(intent)
        }
    }

    // Metoda volaná při obnovení aktivity
    override fun onResume() {
        super.onResume()

        // Spuštění metody pro zobrazení seznamu cest v rámci aktivity
        launch {
            displayCesty()
        }
    }



    // Metoda pro přidání tlačítka pro export dat
    override fun exportButton() {
        val exportButton = customActionBarButton.findViewById<Button>(R.id.exportButton)
        exportButton.setOnClickListener {
            lifecycleScope.launch {
                val fileName = "exported_data.csv"
                try {
                    controller.exportDataToFile(applicationContext, fileName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}