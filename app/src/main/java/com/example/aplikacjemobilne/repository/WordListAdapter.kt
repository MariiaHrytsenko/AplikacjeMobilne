package com.example.aplikacjemobilne.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.Word

class WordListAdapter : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {
    private val words = mutableListOf<WordWithTranslations>()

    data class WordWithTranslations(
        val sourceWord: Word,
        val translations: List<Word>
    )

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewWord: TextView = view.findViewById(R.id.textViewWord)
        val textViewTranslations: TextView = view.findViewById(R.id.textViewTranslations)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word_with_translations, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val item = words[position]
        holder.textViewWord.text = "${item.sourceWord.languageCode}: ${item.sourceWord.word}"
        holder.textViewTranslations.text = item.translations
            .joinToString("\n") { "${it.languageCode}: ${it.word}" }
    }

    override fun getItemCount() = words.size

    fun setWords(newWords: List<WordWithTranslations>) {
        words.clear()
        words.addAll(newWords)
        notifyDataSetChanged()
    }
} 