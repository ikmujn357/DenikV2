package com.usbapps.climbingdiary.custom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usbapps.climbingdiary.model.CestaEntity
import com.usbapps.climbingdiary.R

/**
 * Adaptér pro propojení dat cest s RecyclerView.
 *
 * @param cesta Seznam cest, které budou zobrazeny v RecyclerView.
 * @param cestaClickListener Funkce, která se spustí při kliknutí na položku cesty. Přijímá ID cesty.
 */
class CestaAdapter(
    private var cesta: List<CestaEntity>,
    private val cestaClickListener: (Long) -> Unit
) : RecyclerView.Adapter<CestaAdapter.CestaViewHolder>() {

    /**
     * ViewHolder pro každou položku v RecyclerView.
     *
     * @param view Zobrazení položky cesty.
     */
    class CestaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Reference na textová pole a obrázek v layoutu item_cesta
        val cestaName: TextView = view.findViewById(R.id.cestaName)
        val cestaGrade: TextView = view.findViewById(R.id.cestaGrade)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    /**
     * Mapa pro přiřazení zdrojů obrázků k typům cest.
     * Klíčem je typ cesty a hodnotou je ID obrázku.
     */
    private val routeCharToImageMap = mapOf(
        "Silová" to R.drawable.sila,
        "Technická" to R.drawable.technika,
        "Kombinace" to R.drawable.kombinace
    )

    /**
     * Vytváří nový viewHolder pro každou položku v RecyclerView.
     *
     * @param parent Rodičovský ViewGroup, do kterého se bude náš viewHolder připojen po nafouknutí.
     * @param viewType Typ zobrazení vytvářeného viewHolderu.
     * @return Nový viewHolder, který obsahuje zadané zobrazení položky.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CestaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cesta, parent, false)
        return CestaViewHolder(view)
    }

    /**
     * Nastavuje obsah ViewHolderu na základě pozice v seznamu.
     *
     * @param holder ViewHolder, který má být aktualizován.
     * @param position Pozice položky v seznamu.
     */
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

        // Nastavení posluchače kliknutí na položku cesty
        holder.itemView.setOnClickListener {
            // Získání ID cesty a volání Listeneru
            val cestaId = currentCesta.id
            cestaClickListener.invoke(cestaId)
        }
    }

    /**
     * Vrací počet položek v seznamu cest.
     *
     * @return Počet položek v seznamu cest.
     */
    override fun getItemCount() = cesta.size
}
