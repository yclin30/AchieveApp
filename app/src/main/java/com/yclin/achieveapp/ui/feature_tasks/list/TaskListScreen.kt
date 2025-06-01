package com.yclin.achieveapp.ui.feature_tasks.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.ui.navigation.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val currentFilter by viewModel.filterState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务列表") },
                actions = {
                    Box {
                        TextButton(
                            onClick = { showFilterMenu = !showFilterMenu }
                        ) {
                            Text(
                                text = when (currentFilter) {
                                    TaskFilter.ALL -> "全部任务"
                                    TaskFilter.ACTIVE -> "进行中"
                                    TaskFilter.COMPLETED -> "已完成"
                                    TaskFilter.TODAY -> "今天"
                                    TaskFilter.OVERDUE -> "逾期"
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "过滤选项"
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("全部任务") },
                                onClick = {
                                    viewModel.setFilter(TaskFilter.ALL)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("进行中") },
                                onClick = {
                                    viewModel.setFilter(TaskFilter.ACTIVE)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("已完成") },
                                onClick = {
                                    viewModel.setFilter(TaskFilter.COMPLETED)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("今天") },
                                onClick = {
                                    viewModel.setFilter(TaskFilter.TODAY)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("逾期") },
                                onClick = {
                                    viewModel.setFilter(TaskFilter.OVERDUE)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditTask.route) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (tasks.isEmpty()) {
                // 空状态
                EmptyTasksMessage(currentFilter)
            } else {
                // 任务列表
                TaskList(
                    tasks = tasks,
                    onTaskClick = { task ->
                        navController.navigate(Screen.AddEditTask.createRoute(task.id))
                    },
                    onTaskCompleteToggle = { task ->
                        viewModel.toggleTaskCompleted(task)
                    },
                    onDeleteTask = { task ->
                        viewModel.deleteTask(task)
                    }
                )
            }
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskCompleteToggle: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tasks,
            key = { task -> task.id }
        ) { task ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it * 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                TaskItem(
                    task = task,
                    onTaskClick = onTaskClick,
                    onTaskCompleteToggle = onTaskCompleteToggle,
                    onDeleteTask = onDeleteTask
                )
            }
        }
        // 添加底部间距
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onTaskCompleteToggle: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态切换按钮
            IconButton(
                onClick = { onTaskCompleteToggle(task) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted)
                        Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (task.isCompleted) "标记为未完成" else "标记为已完成",
                    tint = if (task.isCompleted)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // 任务内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTaskClick(task) }
                    .padding(horizontal = 8.dp)
            ) {
                // 任务标题
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                // 任务描述（如果有）
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // 截止日期（如果有）
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val isOverdue = task.dueDate.isBefore(LocalDate.now()) && !task.isCompleted
                    val dateColor = when {
                        task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        isOverdue -> MaterialTheme.colorScheme.error
                        task.dueDate == LocalDate.now() -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Text(
                        text = "截止日期: ${task.dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = dateColor
                    )
                }

                // 优先级指示器（如果设置了优先级）
                if (task.priority > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val priorityColor = when (task.priority) {
                            1 -> Color.Red.copy(alpha = 0.8f)
                            2 -> Color(0xFFFFA500) // Orange
                            else -> Color(0xFF4CAF50) // Green
                        }

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(priorityColor)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = when (task.priority) {
                                1 -> "高优先级"
                                2 -> "中优先级"
                                else -> "低优先级"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 删除按钮
            IconButton(
                onClick = { onDeleteTask(task) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除任务",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyTasksMessage(filter: TaskFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (filter) {
                TaskFilter.ALL -> "没有任务"
                TaskFilter.ACTIVE -> "没有进行中的任务"
                TaskFilter.COMPLETED -> "没有已完成的任务"
                TaskFilter.TODAY -> "今天没有待办任务"
                TaskFilter.OVERDUE -> "没有逾期任务"
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击下方的 + 按钮添加新任务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}