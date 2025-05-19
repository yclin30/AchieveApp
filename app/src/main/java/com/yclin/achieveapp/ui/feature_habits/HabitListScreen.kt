package com.yclin.achieveapp.ui.feature_habits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.yclin.achieveapp.ui.components.AchieveAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AchieveAppBar(
                title = "习惯跟踪"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: 将在后续实现跳转到添加习惯页面 */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加习惯"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // 现在使用空状态占位符，后续会替换为真实习惯列表
        }
    }
}