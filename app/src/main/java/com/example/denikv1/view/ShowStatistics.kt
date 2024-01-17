package com.example.denikv1.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.denikv1.R
import com.example.denikv1.view.statistics.AllStatisticsFragment
import com.example.denikv1.view.statistics.DailyStatisticsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

//zobrazení statistiky
class ShowStatistics : AppCompatActivity() {
    // Instance fragmentů pro denní a celkovou statistiku
    private val dailyStatistics = DailyStatisticsFragment()
    private val allStatistics = AllStatisticsFragment()
    // fragment, který se načítá na začátku
    private var activeFragment: Fragment = dailyStatistics

    // Metoda volaná při vytvoření aktivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nastavení obsahu  layout statistika.xml
        setContentView(R.layout.statistika)

        // Nastavení tlačítka zpět v action baru
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Odstranění stínu z action baru
        supportActionBar?.elevation = 0f

        // Přidání fragmentů do kontejneru a skrytí jednoho z nich
        supportFragmentManager.beginTransaction().add(R.id.content_container, allStatistics, "2").hide(allStatistics).commit()
        supportFragmentManager.beginTransaction().add(R.id.content_container, dailyStatistics, "1").commit()

        // Nastavení  pro navigační lištu
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dailyStatitics -> {

                    // Přepnutí na fragment s denní statistikou
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(dailyStatistics).commit()
                    activeFragment = dailyStatistics
                }
                R.id.allStatitics -> {

                    // Přepnutí na fragment s celkovou statistikou
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(allStatistics).commit()
                    activeFragment = allStatistics
                }
            }
            true
        }
    }

    // Metoda volaná při stisku tlačítka zpět v action baru
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}