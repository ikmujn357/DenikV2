package com.example.denikv1.custom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.R

// Adapter pro propojení dat cest s RecyclerView
class CestaAdapter(private var cesta: List<CestaEntity>, private val cestaClickListener: (Long) -> Unit) :
    RecyclerView.Adapter<CestaAdapter.CestaViewHolder>() {

    // ViewHolder pro každý prvek v RecyclerView
    class CestaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Reference na textová pole v layoutu item_cesta
        val cestaName: TextView = view.findViewById(R.id.cestaName)
        val cestaGrade: TextView = view.findViewById(R.id.cestaGrade)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    // Mapa pro uchování mapování routeChar na zdroje obrázků
    private val routeCharToImageMap = mapOf(
        "Silová" to R.drawable.sila,
        "Technická" to R.drawable.technika,
        "Kombinace" to R.drawable.kombinace
    )

    // Vytvoří nový ViewHolder vytvořením nového view z layoutu item_cesta
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CestaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cesta, parent, false)
        return CestaViewHolder(view)
    }

    // Nastaví obsah ViewHolderu na základě pozice v seznamu
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CestaViewHolder, position: Int) {
        val currentCesta = cesta[position]

        // Nastavení textu v textových polích ViewHolderu
        holder.cestaName.text = currentCesta.routeName
        holder.cestaGrade.text = currentCesta.gradeNum

        // Nastavení obrázku podle hodnoty routeChar
        val imageResource = routeCharToImageMap[currentCesta.routeChar] ?: R.drawable.ikonaucesty
        holder.imageView.setImageResource(imageResource)

        // Nastavení odstupu mezi položkami v RecyclerView
        val spacingInPixels = holder.itemView.resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels)
        holder.itemView.layoutParams = layoutParams

        holder.itemView.setOnClickListener {
            // Získání ID cesty a volání Listener
            val cestaId = currentCesta.id
            cestaClickListener.invoke(cestaId)
        }
    }

    // Vrací počet položek v seznamu
    override fun getItemCount() = cesta.size

}
