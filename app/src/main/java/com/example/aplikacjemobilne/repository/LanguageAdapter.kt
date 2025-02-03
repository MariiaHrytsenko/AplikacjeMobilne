package com.example.aplikacjemobilne.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.Language

class LanguageAdapter(
    private val onEditClick: (Language) -> Unit,
    private val onDeleteClick: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private val languages = mutableListOf<Language>()

    class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLanguageCode: TextView = view.findViewById(R.id.textViewLanguageCode)
        val textViewLanguageName: TextView = view.findViewById(R.id.textViewLanguageName)
        val buttonEdit: ImageButton = view.findViewById(R.id.buttonEdit)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.textViewLanguageCode.text = language.code
        holder.textViewLanguageName.text = language.name

        holder.buttonEdit.setOnClickListener {
            onEditClick(language)
        }

        holder.buttonDelete.setOnClickListener {
            onDeleteClick(language)
        }
    }

    override fun getItemCount() = languages.size

    fun setLanguages(newLanguages: List<Language>) {
        languages.clear()
        languages.addAll(newLanguages)
        notifyDataSetChanged()
    }

    fun removeLanguage(language: Language) {
        val position = languages.indexOfFirst { it.code == language.code }
        if (position != -1) {
            languages.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, languages.size)
        }
    }
} 