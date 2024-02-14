package com.example.denikv1.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.denikv1.R

/**
 * Třída pro vlastní adaptér ArrayAdapter pro zpracování seznamu řetězců.
 *
 * @param context Kontext aktivity nebo aplikace.
 * @param resource ID layoutu pro zobrazení jedné položky seznamu.
 * @param items Seznam položek, které se mají zobrazit v adaptéru.
 */
class CustomArrayAdapter(context: Context, resource: Int, items: List<String>) :
    ArrayAdapter<String>(context, resource, items) {

    /**
     * Vrací zobrazení položky na základě její pozice v seznamu.
     * Tato metoda se používá pro zobrazení položek v rozevíracím seznamu.
     *
     * @param position Pozice položky v seznamu.
     * @param convertView View, které může být znovu použito pro zobrazení položky.
     * @param parent Rodičovský ViewGroup, do kterého bude vytvořené zobrazení přidáno.
     * @return Zobrazení položky na dané pozici v seznamu.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    /**
     * Vrací zobrazení položky na základě její pozice v seznamu, které bude použito v rozevíracím seznamu.
     *
     * @param position Pozice položky v seznamu.
     * @param convertView View, které může být znovu použito pro zobrazení položky.
     * @param parent Rodičovský ViewGroup, do kterého bude vytvořené zobrazení přidáno.
     * @return Zobrazení položky na dané pozici v seznamu, které bude použito v rozevíracím seznamu.
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    /**
     * Vytváří a vrací zobrazení položky pro adaptér.
     *
     * @param position Pozice položky v seznamu.
     * @param parent Rodičovský ViewGroup, do kterého bude vytvořené zobrazení přidáno.
     * @return Zobrazení položky pro adaptér.
     */
    private fun createItemView(position: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(R.layout.item_spinner, parent, false)

        val textView = view.findViewById<TextView>(R.id.text)

        textView.text = getItem(position)



        return view
    }
}
