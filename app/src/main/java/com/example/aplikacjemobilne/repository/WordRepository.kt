package com.example.aplikacjemobilne.repository

import com.example.aplikacjemobilne.data.*

class WordRepository(
    private val wordDao: WordDao,
    private val languageDao: LanguageDao,
    private val translationDao: TranslationDao,
    private val learningSetDao: LearningSetDao,
    private val learningStatisticDao: LearningStatisticDao
) {
    // Word operations
    suspend fun insertWord(word: Word) = wordDao.insert(word)
    suspend fun getWordById(id: Int) = wordDao.getWordById(id)
    suspend fun getAllWords() = wordDao.getAllWords()
    suspend fun updateWord(word: Word) = wordDao.update(word)
    suspend fun deleteWord(word: Word) = wordDao.delete(word)

    // Language operations
    suspend fun insertLanguage(language: Language) = languageDao.insert(language)
    suspend fun getAllLanguages() = languageDao.getAllLanguages()
    suspend fun deleteLanguage(language: Language) = languageDao.delete(language)

    // Translation operations
    suspend fun insertTranslation(translation: Translation) = translationDao.insert(translation)
    suspend fun getAllTranslations() = translationDao.getAllTranslations()
    suspend fun deleteTranslation(translation: Translation) = translationDao.delete(translation)

    // LearningSet operations
    suspend fun insertLearningSet(set: LearningSet) = learningSetDao.insert(set)
    suspend fun getAllLearningSets() = learningSetDao.getAllLearningSets()
    suspend fun deleteLearningSet(set: LearningSet) = learningSetDao.delete(set)

    // LearningStatistic operations
    suspend fun insertLearningStatistic(statistic: LearningStatistic) = learningStatisticDao.insert(statistic)
    suspend fun getStatisticsForSet(setId: Int) = learningStatisticDao.getStatisticsForSet(setId)
    suspend fun deleteLearningStatistic(statistic: LearningStatistic) = learningStatisticDao.delete(statistic)
}