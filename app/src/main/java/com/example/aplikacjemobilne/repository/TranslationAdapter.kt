package com.example.aplikacjemobilne.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R

class TranslationAdapter : RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {
    private val translations = mutableListOf<TranslationItem>()

    data class TranslationItem(
        val sourceLanguage: String,
        val targetLanguage: String,
        val sourceWord: String,
        val targetWord: String
    )

    class TranslationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTranslation: TextView = view.findViewById(R.id.textViewTranslation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_translation, parent, false)
        return TranslationViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        val item = translations[position]
        holder.textViewTranslation.text = "${item.sourceLanguage}: ${item.sourceWord} → ${item.targetLanguage}: ${item.targetWord}"
    }

    override fun getItemCount() = translations.size

    fun addTranslation(translation: TranslationItem) {
        translations.add(translation)
        notifyItemInserted(translations.size - 1)
    }

    fun getTranslations(): List<TranslationItem> = translations.toList()

    fun clear() {
        val size = translations.size
        translations.clear()
        notifyItemRangeRemoved(0, size)
    }
} 