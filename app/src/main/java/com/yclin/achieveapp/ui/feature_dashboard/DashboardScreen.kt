package com.yclin.achieveapp.ui.feature_dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.ui.components.AchieveAppBar
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


    Scaffold(
        topBar = {
            AchieveAppBar(
                title = "今日视图"
            )
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
            Text(
                text = "你好，今天是",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // *** 修改点: 调用 DashboardSection 时不再传递 emptyMessage ***
            DashboardSection(
                title = "今日任务 (${todayTasks.size})"
            ) {
                if (todayTasks.isEmpty()) {
                    Text(
                        text = "太棒了！今天没有待办任务。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp) // 为空消息添加一些垂直间距
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayTasks.forEach { task ->
                            TaskItem(task = task, navController = navController)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // *** 修改点: 调用 DashboardSection 时不再传递 emptyMessage ***
            DashboardSection(
                title = "今日习惯 (${todayHabits.size})"
            ) {
                if (todayHabits.isEmpty()) {
                    Text(
                        text = "今天没有需要培养的习惯。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp) // 为空消息添加一些垂直间距
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayHabits.forEach { habitItem ->
                            HabitDashboardCard(
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
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    modifier: Modifier = Modifier,
    // *** 修改点: 移除了 emptyMessage 参数 ***
    content: @Composable ColumnScope.() -> Unit // content lambda 作为最后一个参数，可以使用尾随 lambda 语法调用
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        // *** 修改点: 直接调用 content() ***
        content() // Column 已经提供了 ColumnScope
    }
}

@Composable
fun TaskItem(task: Task, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // navController.navigate("${Screen.AddEditTask.route}/${task.id}") // 导航到任务详情或编辑页
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            // 可以根据需要添加任务状态或优先级的指示图标
        }
    }
}


@Composable
fun HabitDashboardCard(
    item: HabitDashboardItem,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompletedToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.habit.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isCompletedToday) TextDecoration.LineThrough else null
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (item.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (item.isCompletedToday) "标记为未完成" else "标记为完成",
                    tint = if (item.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}