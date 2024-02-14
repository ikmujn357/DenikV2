package com.example.denikv1.view

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.denikv1.R
import com.example.denikv1.view.statistics.AllStatisticsFragment
import com.example.denikv1.view.statistics.DailyStatisticsFragment

// Zobrazení statistiky
class ShowStatistics : AppCompatActivity() {
    // Instance fragmentů pro denní a celkovou statistiku
    private val dailyStatistics = DailyStatisticsFragment()
    private val allStatistics = AllStatisticsFragment()
    // Fragment, který se načítá na začátku
    private var activeFragment: Fragment = dailyStatistics

    // Metoda volaná při vytvoření aktivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistika)

        // Přidání fragmentů do kontejneru a skrytí jednoho z nich
        supportFragmentManager.beginTransaction().add(R.id.content_container, allStatistics, "2").hide(allStatistics).commit()
        supportFragmentManager.beginTransaction().add(R.id.content_container, dailyStatistics, "1").commit()

        // Nastavení horního panelu
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setCustomView(R.layout.custom_bar_statistika)

        val buttonBack: Button = actionBar?.customView?.findViewById(R.id.action_back)!!

        buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val buttonDenni: Button = actionBar.customView?.findViewById(R.id.button_denni)!!
        val buttonCelkova: Button = actionBar.customView.findViewById(R.id.button_celkova)!!

        buttonDenni.setOnClickListener {
            // Přepnutí na fragment s denní statistikou
            supportFragmentManager.beginTransaction().hide(activeFragment).show(dailyStatistics).commit()
            activeFragment = dailyStatistics
        }


        buttonCelkova.setOnClickListener {
            // Přepnutí na fragment s celkovou statistikou
            supportFragmentManager.beginTransaction().hide(activeFragment).show(allStatistics).commit()
            activeFragment = allStatistics
        }


    }

}
