package com.yclin.achieveapp.ui.feature_dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.ui.components.AchieveAppBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val formattedDate = "${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}, " +
            "${today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}"

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
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

            DashboardSection(
                title = "今日任务",
                emptyMessage = "今天没有待办任务"
            )

            Spacer(modifier = Modifier.height(24.dp))

            DashboardSection(
                title = "今日习惯",
                emptyMessage = "今天没有需要培养的习惯"
            )
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    emptyMessage: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 使用空状态作为占位符
        Column(modifier = Modifier.height(160.dp)) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}