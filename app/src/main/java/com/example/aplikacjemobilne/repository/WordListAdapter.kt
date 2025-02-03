package com.example.aplikacjemobilne.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.Word

class WordListAdapter(
    private val onEditClick: (WordWithTranslations) -> Unit,
    private val onDeleteClick: (WordWithTranslations) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {
    private val words = mutableListOf<WordWithTranslations>()

    data class WordWithTranslations(
        val sourceWord: Word,
        val translations: List<Word>
    )

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewWord: TextView = view.findViewById(R.id.textViewWord)
        val textViewTranslations: TextView = view.findViewById(R.id.textViewTranslations)
        val buttonEdit: ImageButton = view.findViewById(R.id.buttonEdit)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
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

        holder.buttonEdit.setOnClickListener {
            onEditClick(item)
        }

        holder.buttonDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = words.size

    fun setWords(newWords: List<WordWithTranslations>) {
        words.clear()
        words.addAll(newWords)
        notifyDataSetChanged()
    }

    fun removeWord(wordToRemove: WordWithTranslations) {
        val position = words.indexOfFirst { it.sourceWord.id == wordToRemove.sourceWord.id }
        if (position != -1) {
            words.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, words.size)
        }
    }
} 