package com.example.aplikacjemobilne.repository

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R

class TranslationAdapter : RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {
    private val translations = mutableListOf<TranslationItem>()

    data class TranslationItem(
        val sourceLanguage: String,
        val targetLanguage: String,
        val sourceWord: String,
        var targetWord: String
    )

    class TranslationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLanguages: TextView = view.findViewById(R.id.textViewLanguages)
        val editTextTranslation: EditText = view.findViewById(R.id.editTextTranslation)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_translation, parent, false)
        return TranslationViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        val item = translations[position]
        holder.textViewLanguages.text = "${item.sourceLanguage}: ${item.sourceWord} → ${item.targetLanguage}:"
        holder.editTextTranslation.setText(item.targetWord)
        
        // Dodaj listener do EditText
        holder.editTextTranslation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                item.targetWord = s.toString()
            }
        })

        holder.buttonDelete.setOnClickListener {
            removeTranslation(position)
        }
    }

    override fun getItemCount() = translations.size

    fun addTranslation(translation: TranslationItem) {
        translations.add(translation)
        notifyItemInserted(translations.size - 1)
    }

    private fun removeTranslation(position: Int) {
        if (position in translations.indices) {
            translations.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, translations.size)
        }
    }

    fun getTranslations(): List<TranslationItem> = translations.toList()

    fun clear() {
        val size = translations.size
        translations.clear()
        notifyItemRangeRemoved(0, size)
    }
} 