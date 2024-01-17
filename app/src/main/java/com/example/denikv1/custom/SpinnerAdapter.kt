package com.example.denikv1.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.denikv1.R

// Vlastní adaptér pro Spinner
class CustomArrayAdapter(context: Context, resource: Int, items: List<String>) :
    ArrayAdapter<String>(context, resource, items) {

    // Metoda pro získání zobrazení (Spinner neotevřený)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    // Metoda pro získání zobrazení (Spinner otevřený)
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    // Vytvoření zobrazení pro konkrétní položku
    private fun createItemView(position: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(R.layout.item_spinner, parent, false)

        val textView = view.findViewById<TextView>(R.id.text)

        textView.text = getItem(position)



        return view
    }
}
