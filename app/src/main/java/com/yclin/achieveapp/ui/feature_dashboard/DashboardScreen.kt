package com.yclin.achieveapp.ui.feature_dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.ui.components.AchieveAppBar
import com.yclin.achieveapp.ui.navigation.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel
) {
    val today = LocalDate.now()
    val formattedDate = "${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}, " +
            today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))

    val todayTasks by viewModel.todayTasks.collectAsState()
    val todayHabits by viewModel.todayHabits.collectAsState()
    val quadrantStats by viewModel.quadrantStats.collectAsState()

    Scaffold(
        topBar = {
            AchieveAppBar(
                title = "今日视图"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditTask.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 问候语
            Text(
                text = "你好，yclin30！今天是",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 任务概览卡片
            TaskOverviewCard(
                totalTasks = quadrantStats.totalActiveTasks,
                urgentTasks = quadrantStats.urgentImportantCount + quadrantStats.urgentNotImportantCount,
                onViewAllTasks = { navController.navigate(Screen.Tasks.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 今日任务
            DashboardSection(
                title = "今日任务 (${todayTasks.size})"
            ) {
                if (todayTasks.isEmpty()) {
                    EmptyStateCard(
                        message = "太棒了！今天没有待办任务。",
                        icon = Icons.Default.TaskAlt,
                        actionText = "添加新任务",
                        onAction = { navController.navigate(Screen.AddEditTask.route) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayTasks.forEach { task ->
                            SimpleTaskItem(
                                task = task,
                                navController = navController,
                                onToggleComplete = { viewModel.toggleTaskCompletion(task.id) }
                            )
                        }

                        // 查看所有任务按钮
                        if (todayTasks.isNotEmpty()) {
                            TextButton(
                                onClick = { navController.navigate(Screen.Tasks.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("查看所有任务")
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 今日习惯
            DashboardSection(
                title = "今日习惯 (${todayHabits.size})"
            ) {
                if (todayHabits.isEmpty()) {
                    EmptyStateCard(
                        message = "今天没有需要培养的习惯。",
                        icon = Icons.Default.Repeat,
                        actionText = "添加新习惯",
                        onAction = { navController.navigate(Screen.AddEditHabit.route) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayHabits.forEach { habitItem ->
                            SimpleHabitItem(
                                item = habitItem,
                                onToggleComplete = {
                                    viewModel.toggleHabitCompletion(
                                        habitItem.habit.id,
                                        habitItem.today,
                                        habitItem.isCompletedToday
                                    )
                                }
                            )
                        }

                        // 查看所有习惯按钮
                        if (todayHabits.isNotEmpty()) {
                            TextButton(
                                onClick = { navController.navigate(Screen.Habits.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("查看所有习惯")
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TaskOverviewCard(
    totalTasks: Int,
    urgentTasks: Int,
    onViewAllTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewAllTasks() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "活跃任务",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalTasks 个总计",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (urgentTasks > 0) {
                    Text(
                        text = "$urgentTasks 个紧急",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "查看全部",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SimpleTaskItem(
    task: Task,
    navController: NavController,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.AddEditTask.createRoute(task.id))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态按钮
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (task.isCompleted) "标记为未完成" else "标记为完成",
                    tint = if (task.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 任务内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                // 显示任务属性（重要/紧急）
                if (task.isImportant || task.isUrgent) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (task.isImportant) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("重要", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (task.isUrgent) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.error
                            ) {
                                Text("紧急", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // 截止日期指示器
            if (task.dueDate != null) {
                val isOverdue = task.dueDate.isBefore(LocalDate.now()) && !task.isCompleted
                val isToday = task.dueDate == LocalDate.now()

                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "截止日期",
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        isOverdue -> MaterialTheme.colorScheme.error
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
fun SimpleHabitItem(
    item: HabitDashboardItem,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompletedToday)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态按钮
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (item.isCompletedToday) "标记为未完成" else "标记为完成",
                    tint = if (item.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 习惯名称
            Text(
                text = item.habit.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isCompletedToday) TextDecoration.LineThrough else null,
                color = if (item.isCompletedToday)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // 连续天数（如果有的话）
            if (item.habit.currentStreak > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        "${item.habit.currentStreak}天",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}