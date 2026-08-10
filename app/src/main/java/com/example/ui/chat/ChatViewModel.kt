package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatMessageEntity
import com.example.data.repositories.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _uiState.value.isLoading) return

        _uiState.value = ChatUiState(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.sendMessageToTutor(userText = userText.trim())
            if (result.isFailure) {
                _uiState.value = ChatUiState(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Erro desconhecido."
                )
            } else {
                _uiState.value = ChatUiState(isLoading = false, errorMessage = null)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
