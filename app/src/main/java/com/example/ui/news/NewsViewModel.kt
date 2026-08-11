package com.example.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ApiKeyManager
import com.example.data.database.NewsEntity
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNewsForDetail = MutableStateFlow<NewsEntity?>(null)
    val selectedNewsForDetail: StateFlow<NewsEntity?> = _selectedNewsForDetail.asStateFlow()

    private val _isGeneratingAiNews = MutableStateFlow(false)
    val isGeneratingAiNews: StateFlow<Boolean> = _isGeneratingAiNews.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val newsList: StateFlow<List<NewsEntity>> = combine(
        repository.allNews,
        _selectedCategory,
        _searchQuery
    ) { allNews, category, query ->
        allNews.filter { news ->
            val matchesCategory = if (category == "Todas") true else news.category.equals(category, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                news.title.contains(query, ignoreCase = true) ||
                news.summary.contains(query, ignoreCase = true) ||
                news.content.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openNewsDetail(news: NewsEntity) {
        _selectedNewsForDetail.value = news
    }

    fun closeNewsDetail() {
        _selectedNewsForDetail.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun publishNewsManual(
        title: String,
        summary: String,
        content: String,
        category: String,
        authorName: String
    ) {
        viewModelScope.launch {
            try {
                repository.publishNews(
                    title = title,
                    summary = summary,
                    content = content,
                    category = category,
                    authorName = authorName,
                    isAiGenerated = false
                )
                _statusMessage.value = "Notícia publicada com sucesso!"
            } catch (e: Exception) {
                _statusMessage.value = "Erro ao publicar notícia: ${e.localizedMessage}"
            }
        }
    }

    fun generateNewsWithAi(theme: String, category: String) {
        viewModelScope.launch {
            _isGeneratingAiNews.value = true
            val lang = ApiKeyManager.getLanguage()
            val result = repository.generateAndSaveNews(
                theme = theme,
                category = category,
                language = lang
            )
            _isGeneratingAiNews.value = false

            if (result.isSuccess) {
                _statusMessage.value = "Notícia gerada com sucesso pela IA do Gemini!"
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erro ao gerar notícia com IA."
                _statusMessage.value = err
            }
        }
    }

    fun deleteNews(id: String) {
        viewModelScope.launch {
            repository.deleteNews(id)
            if (_selectedNewsForDetail.value?.id == id) {
                _selectedNewsForDetail.value = null
            }
            _statusMessage.value = "Notícia removida."
        }
    }
}
