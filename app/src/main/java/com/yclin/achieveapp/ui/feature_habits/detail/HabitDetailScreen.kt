package com.yclin.achieveapp.ui.feature_habits.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    navController: NavController,
    viewModel: HabitDetailViewModel
) {
    val habitWithCompletions by viewModel.habitWithCompletions.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    if (habitWithCompletions == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val habit = habitWithCompletions!!.habit
    val completions = habitWithCompletions!!.completions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit.name) }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("习惯描述：${habit.description}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("连续天数：${habit.currentStreak}  最长：${habit.longestStreak}")
            Spacer(modifier = Modifier.height(16.dp))
            Text("本月打卡：", style = MaterialTheme.typography.titleMedium)
            HabitCalendar(
                currentMonth = LocalDate.now().withDayOfMonth(1),
                completions = completions.filter { it.isCompleted }.map { it.date },
                onDayClick = { day -> viewModel.toggleCompletion(day) }
            )
        }
    }
}

@Composable
fun HabitCalendar(
    currentMonth: LocalDate,
    completions: List<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (currentMonth.dayOfWeek.value % 7) // 0=周日
    val today = LocalDate.now()
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (0..6).forEach {
                val dayName = LocalDate.of(2023, 1, 1).plusDays(it.toLong())
                    .dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(dayName, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            }
        }
        var day = 1
        for (week in 0..5) {
            Row(Modifier.fillMaxWidth()) {
                for (dow in 0..6) {
                    if (week == 0 && dow < firstDayOfWeek) {
                        Box(Modifier.weight(1f).height(36.dp))
                    } else if (day <= daysInMonth) {
                        val date = currentMonth.withDayOfMonth(day)
                        val checked = completions.contains(date)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(36.dp)
                                .padding(2.dp)
                                .background(
                                    if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else if (date == today) Color.LightGray.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .clickable { onDayClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.toString())
                        }
                        day++
                    }
                }
            }
        }
    }
}