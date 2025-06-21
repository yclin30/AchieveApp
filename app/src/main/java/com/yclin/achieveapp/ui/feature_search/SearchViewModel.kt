package com.yclin.achieveapp.ui.feature_search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.model.SearchFilter
import com.yclin.achieveapp.data.model.SearchResult
import com.yclin.achieveapp.data.model.SearchResultType
import com.yclin.achieveapp.data.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val userId: Long,
    private val searchRepository: SearchRepository
) : ViewModel() {

    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 搜索筛选
    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    // 是否正在搜索
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // 🔧 修复：简化搜索结果的处理
    val searchResults: StateFlow<List<SearchResult>> = combine(
        _searchQuery.debounce(300), // 防抖 300ms
        _searchFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            _isSearching.value = false
            flowOf(emptyList<SearchResult>())
        } else {
            _isSearching.value = true
            searchRepository.searchAll(userId, query, filter)
                .onEach { _isSearching.value = false }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 分组后的搜索结果
    val groupedResults: StateFlow<Map<SearchResultType, List<SearchResult>>> = searchResults
        .map { results ->
            results.groupBy { it.type }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 更新搜索筛选
     */
    fun updateFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    /**
     * 切换结果类型筛选
     */
    fun toggleResultType(type: SearchResultType?) {
        val currentFilter = _searchFilter.value
        _searchFilter.value = currentFilter.copy(type = type)
    }

    /**
     * 切换是否包含已完成任务
     */
    fun toggleIncludeCompletedTasks() {
        val currentFilter = _searchFilter.value
        _searchFilter.value = currentFilter.copy(
            includeCompletedTasks = !currentFilter.includeCompletedTasks
        )
    }

    /**
     * 清空搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    companion object {
        fun provideFactory(userId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AchieveApp
                return SearchViewModel(
                    userId = userId,
                    searchRepository = application.searchRepository
                ) as T
            }
        }
    }
}