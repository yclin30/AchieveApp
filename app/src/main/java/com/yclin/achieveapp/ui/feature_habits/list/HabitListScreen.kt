package com.yclin.achieveapp.ui.feature_habits.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    viewModel: HabitListViewModel = viewModel(
        factory = HabitListViewModel.Factory(
            navController.context.applicationContext as com.yclin.achieveapp.AchieveApp
        )
    ),
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("习惯追踪") },
                actions = {
                    // 可以添加其他操作按钮
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditHabit.createRoute(-1L))
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "添加习惯")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {

                habits.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无习惯，点击 + 添加")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            HabitItem(
                                habit = habit,
                                onEdit = {
                                    navController.navigate(Screen.AddEditHabit.createRoute(habit.id))
                                },
                                onDelete = { viewModel.deleteHabit(habit) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit  // 点击整个卡片也可以编辑
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row {
                    Text(
                        text = "连续: ${habit.currentStreak} 天",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "最长: ${habit.longestStreak} 天",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (habit.frequencyType == 1) "每日"
                    else "每周${weekDaysToStr(habit.weekDays)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,  // 使用编辑图标更合适
                    contentDescription = "编辑习惯"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除习惯",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun weekDaysToStr(weekDays: Int): String {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    return days.withIndex()
        .filter { (i, _) -> weekDays and (1 shl i) != 0 }
        .joinToString("") { it.value }
}