package com.yclin.achieveapp.ui.feature_search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.data.model.SearchFilter
import com.yclin.achieveapp.data.model.SearchResult
import com.yclin.achieveapp.data.model.SearchResultType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val groupedResults by viewModel.groupedResults.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔍 搜索栏
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onClearClick = viewModel::clearSearch,
            isSearching = isSearching,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🏷️ 筛选标签
        FilterChips(
            filter = searchFilter,
            onFilterChange = viewModel::updateFilter,
            onToggleResultType = viewModel::toggleResultType,
            onToggleIncludeCompleted = viewModel::toggleIncludeCompletedTasks
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 搜索结果
        if (searchQuery.isBlank()) {
            // 空状态 - 搜索提示
            EmptySearchState()
        } else if (isSearching) {
            // 加载状态
            LoadingState()
        } else if (groupedResults.isEmpty()) {
            // 无结果状态
            NoResultsState(query = searchQuery)
        } else {
            // 搜索结果列表
            SearchResultsList(
                groupedResults = groupedResults,
                onTaskClick = { task ->
                    navController.navigate("add_edit_task/${task.id}")
                },
                onHabitClick = { habit ->
                    navController.navigate("add_edit_habit/${habit.id}")
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索任务和习惯...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
        },
        trailingIcon = {
            Row {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (query.isNotBlank()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空"
                        )
                    }
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun FilterChips(
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onToggleResultType: (SearchResultType?) -> Unit,
    onToggleIncludeCompleted: () -> Unit
) {
    Column {
        // 类型筛选
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter.type == null,
                onClick = { onToggleResultType(null) },
                label = { Text("全部") }
            )
            FilterChip(
                selected = filter.type == SearchResultType.TASK,
                onClick = {
                    onToggleResultType(
                        if (filter.type == SearchResultType.TASK) null
                        else SearchResultType.TASK
                    )
                },
                label = { Text("任务") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            FilterChip(
                selected = filter.type == SearchResultType.HABIT,
                onClick = {
                    onToggleResultType(
                        if (filter.type == SearchResultType.HABIT) null
                        else SearchResultType.HABIT
                    )
                },
                label = { Text("习惯") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Loop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        // 已完成任务筛选（仅在显示任务时可见）
        if (filter.type == null || filter.type == SearchResultType.TASK) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filter.includeCompletedTasks,
                    onClick = onToggleIncludeCompleted,
                    label = { Text("包含已完成") },
                    leadingIcon = if (filter.includeCompletedTasks) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    groupedResults: Map<SearchResultType, List<SearchResult>>,
    onTaskClick: (SearchResult.TaskResult) -> Unit,
    onHabitClick: (SearchResult.HabitResult) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 任务结果
        groupedResults[SearchResultType.TASK]?.let { tasks ->
            if (tasks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "📋 任务",
                        count = tasks.size
                    )
                }
                items(tasks) { result ->
                    val taskResult = result as SearchResult.TaskResult
                    TaskResultItem(
                        result = taskResult,
                        onClick = { onTaskClick(taskResult) }
                    )
                }
            }
        }

        // 习惯结果
        groupedResults[SearchResultType.HABIT]?.let { habits ->
            if (habits.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🔄 习惯",
                        count = habits.size
                    )
                }
                items(habits) { result ->
                    val habitResult = result as SearchResult.HabitResult
                    HabitResultItem(
                        result = habitResult,
                        onClick = { onHabitClick(habitResult) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskResultItem(
    result: SearchResult.TaskResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 象限颜色指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(result.quadrantColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (result.isCompleted) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "已完成",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (result.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.quadrantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = result.quadrantColor
                    )

                    result.dueDate?.let { dueDate ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "截止: ${dueDate.format(DateTimeFormatter.ofPattern("MM/dd"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitResultItem(
    result: SearchResult.HabitResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 习惯图标
            Icon(
                imageVector = Icons.Outlined.Loop,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (result.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.frequencyText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (result.currentStreak > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "连续 ${result.currentStreak} 天",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "开始搜索任务和习惯",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "输入关键词来查找相关内容",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "搜索中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "未找到相关结果",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "尝试搜索 \"$query\" 的其他关键词",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}