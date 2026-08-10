package com.example.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.StudyLesson
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudyUiState(
    val subject: String = "Matemática",
    val topicInput: String = "",
    val isLoading: Boolean = false,
    val currentLesson: StudyLesson? = null,
    val currentSectionIndex: Int = 0,
    val selectedCheckOption: Int = -1,
    val isCheckChecked: Boolean = false,
    val errorMessage: String? = null
)

class StudyViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    fun updateSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    fun updateTopicInput(topic: String) {
        _uiState.value = _uiState.value.copy(topicInput = topic)
    }

    fun generateLesson() {
        val topic = _uiState.value.topicInput.ifBlank { "Introdução ao Assunto" }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.generateLesson(_uiState.value.subject, topic)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentLesson = result.getOrNull(),
                    currentSectionIndex = 0,
                    selectedCheckOption = -1,
                    isCheckChecked = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Erro ao criar aula."
                )
            }
        }
    }

    fun nextSection() {
        val lesson = _uiState.value.currentLesson ?: return
        if (_uiState.value.currentSectionIndex + 1 < lesson.sections.size) {
            _uiState.value = _uiState.value.copy(
                currentSectionIndex = _uiState.value.currentSectionIndex + 1
            )
        }
    }

    fun previousSection() {
        if (_uiState.value.currentSectionIndex > 0) {
            _uiState.value = _uiState.value.copy(
                currentSectionIndex = _uiState.value.currentSectionIndex - 1
            )
        }
    }

    fun selectCheckOption(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCheckOption = index)
    }

    fun checkCheckAnswer() {
        _uiState.value = _uiState.value.copy(isCheckChecked = true)
    }

    fun resetLesson() {
        _uiState.value = StudyUiState()
    }
}
