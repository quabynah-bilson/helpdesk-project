package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.model.data.Question
import io.helpdesk.model.db.QuestionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaqsViewModel @Inject constructor(private val dao: QuestionDao) : ViewModel() {
    private val _uiState = MutableStateFlow<QuestionsUIState>(QuestionsUIState.Loading)
    val uiState: StateFlow<QuestionsUIState> get() = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(QuestionsUIState.Loading)

            dao.faqs().collectLatest { faqs ->
                if (faqs.isEmpty()) _uiState.emit(QuestionsUIState.Error("no questions found"))
                else _uiState.emit(QuestionsUIState.Success(PagingData.from(faqs)))
            }
        }
    }
}


sealed class QuestionsUIState {
    data class Success(val faqs: PagingData<Question>) : QuestionsUIState()
    data class Error(val reason: String) : QuestionsUIState()
    object Loading : QuestionsUIState()
}