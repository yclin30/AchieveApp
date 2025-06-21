package com.yclin.achieveapp.ui.feature_tasks.list

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.yclin.achieveapp.data.database.entity.QuadrantType
import com.yclin.achieveapp.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel,
    modifier: Modifier = Modifier
) {
    val urgentImportantTasks by viewModel.urgentAndImportantTasks.collectAsState()
    val importantNotUrgentTasks by viewModel.importantNotUrgentTasks.collectAsState()
    val urgentNotImportantTasks by viewModel.urgentNotImportantTasks.collectAsState()
    val notUrgentNotImportantTasks by viewModel.notUrgentNotImportantTasks.collectAsState()
    val showCompleted by viewModel.showCompleted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val taskStats by viewModel.getTaskStats().collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 监听同步事件
    LaunchedEffect(Unit) {
        viewModel.syncEvent.collectLatest { event ->
            when (event) {
                is SyncUiEvent.Success -> snackbarHostState.showSnackbar(event.message)
                is SyncUiEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("四象限任务管理")
                        if (taskStats.totalActiveTasks > 0) {
                            Text(
                                text = "${taskStats.totalActiveTasks} 个活跃任务",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // 显示/隐藏已完成任务
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            imageVector = if (showCompleted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showCompleted) "隐藏已完成" else "显示已完成",
                            tint = if (showCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // 同步按钮
                    IconButton(
                        onClick = { viewModel.syncTasks() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "同步")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditTask.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 四象限说明卡片
            QuadrantInfoCard()

            // 第一行：重要且紧急 | 重要不紧急
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuadrantCard(
                    modifier = Modifier.weight(1f),
                    quadrant = QuadrantType.URGENT_IMPORTANT,
                    tasks = urgentImportantTasks,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    borderColor = MaterialTheme.colorScheme.error,
                    onTaskClick = { task ->
                        navController.navigate(Screen.AddEditTask.createRoute(task.id))
                    },
                    onTaskAction = { action ->
                        viewModel.handleTaskAction(action)
                    }
                )

                QuadrantCard(
                    modifier = Modifier.weight(1f),
                    quadrant = QuadrantType.IMPORTANT_NOT_URGENT,
                    tasks = importantNotUrgentTasks,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    borderColor = MaterialTheme.colorScheme.primary,
                    onTaskClick = { task ->
                        navController.navigate(Screen.AddEditTask.createRoute(task.id))
                    },
                    onTaskAction = { action ->
                        viewModel.handleTaskAction(action)
                    }
                )
            }

            // 第二行：紧急不重要 | 不紧急不重要
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuadrantCard(
                    modifier = Modifier.weight(1f),
                    quadrant = QuadrantType.URGENT_NOT_IMPORTANT,
                    tasks = urgentNotImportantTasks,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    onTaskClick = { task ->
                        navController.navigate(Screen.AddEditTask.createRoute(task.id))
                    },
                    onTaskAction = { action ->
                        viewModel.handleTaskAction(action)
                    }
                )

                QuadrantCard(
                    modifier = Modifier.weight(1f),
                    quadrant = QuadrantType.NOT_URGENT_NOT_IMPORTANT,
                    tasks = notUrgentNotImportantTasks,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = MaterialTheme.colorScheme.outline,
                    onTaskClick = { task ->
                        navController.navigate(Screen.AddEditTask.createRoute(task.id))
                    },
                    onTaskAction = { action ->
                        viewModel.handleTaskAction(action)
                    }
                )
            }
        }
    }
}

@Composable
fun QuadrantInfoCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "重要 ↑",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "四象限时间管理法",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "→ 紧急",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuadrantCard(
    quadrant: QuadrantType,
    tasks: List<Task>,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onTaskClick: (Task) -> Unit,
    onTaskAction: (TaskAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoveDialog by remember { mutableStateOf<Task?>(null) }

    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 象限标题和任务计数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quadrant.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = quadrant.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                // 任务计数徽章
                if (tasks.isNotEmpty()) {
                    val activeTasks = tasks.count { !it.isCompleted }
                    if (activeTasks > 0) {
                        Badge(
                            containerColor = borderColor,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = activeTasks.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 任务列表
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = contentColor.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 })
                        ) {
                            QuadrantTaskItem(
                                task = task,
                                onClick = { onTaskClick(task) },
                                onToggleCompleted = {
                                    onTaskAction(TaskAction.ToggleCompleted(task.id))
                                },
                                onDelete = {
                                    onTaskAction(TaskAction.DeleteTask(task.id))
                                },
                                onMove = {
                                    showMoveDialog = task
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 移动任务对话框
    showMoveDialog?.let { task ->
        MoveTaskDialog(
            task = task,
            currentQuadrant = quadrant,
            onDismiss = { showMoveDialog = null },
            onMove = { targetQuadrant ->
                onTaskAction(TaskAction.MoveTask(task.id, targetQuadrant))
                showMoveDialog = null
            }
        )
    }
}

@Composable
fun QuadrantTaskItem(
    task: Task,
    onClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 完成状态切换
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = if (task.isCompleted) "标记为未完成" else "标记为已完成",
                        tint = if (task.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 任务内容
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                    )

                    // 描述（如果有）
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // 截止日期
                    if (task.dueDate != null) {
                        val isOverdue = task.dueDate.isBefore(LocalDate.now()) && !task.isCompleted
                        val isToday = task.dueDate == LocalDate.now()

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = when {
                                    task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.dueDate.format(DateTimeFormatter.ofPattern("MM-dd")),
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }

                // 菜单按钮
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多选项",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("移动到...") },
                            onClick = {
                                onMove()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DriveFileMove, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoveTaskDialog(
    task: Task,
    currentQuadrant: QuadrantType,
    onDismiss: () -> Unit,
    onMove: (QuadrantType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移动任务") },
        text = {
            Column {
                Text("将「${task.title}」移动到：")
                Spacer(modifier = Modifier.height(16.dp))

                QuadrantType.values().forEach { quadrant ->
                    if (quadrant != currentQuadrant) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMove(quadrant) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = quadrant.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = quadrant.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}