package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.model.data.Question
import io.helpdesk.model.db.QuestionDao
import javax.inject.Inject

@HiltViewModel
class FaqsViewModel @Inject constructor(private val dao: QuestionDao) : ViewModel() {
    val questions = Pager(PagingConfig(pageSize = 10)) {
        QuestionsDataSource(dao)
    }.flow.cachedIn(viewModelScope)
}

/**
 * data source for [Question] using [PagingSource]
 */
private class QuestionsDataSource(private val dao: QuestionDao) : PagingSource<Int, Question>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Question> {
        return try {
            val questions = dao.faqs()
            LoadResult.Page(
                data = questions,
                prevKey = null,
                nextKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Question>): Int? = null
}