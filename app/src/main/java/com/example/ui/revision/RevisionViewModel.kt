package com.example.ui.revision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.TopicProgressEntity
import com.example.data.models.RevisionRecommendation
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RevisionUiState(
    val isLoadingRecommendations: Boolean = false,
    val recommendations: List<RevisionRecommendation> = emptyList(),
    val errorMessage: String? = null
)

class RevisionViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    val weakTopics: StateFlow<List<TopicProgressEntity>> = repository.weakTopics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allProgress: StateFlow<List<TopicProgressEntity>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    fun loadAiRecommendations() {
        val topicsList = weakTopics.value.map { "${it.subject}: ${it.topic}" }
        val topicsToFetch = if (topicsList.isNotEmpty()) topicsList else listOf("Matemática: Equações", "Português: Crase")

        _uiState.value = _uiState.value.copy(isLoadingRecommendations = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.getRevisionRecommendations(topicsToFetch)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRecommendations = false,
                    recommendations = result.getOrNull() ?: emptyList()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingRecommendations = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Erro ao gerar recomendações."
                )
            }
        }
    }
}
