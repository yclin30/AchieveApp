package com.yclin.achieveapp.ui.feature_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.User
import com.yclin.achieveapp.ui.navigation.Screen

@Composable
fun ProfileScreen(
    navController: NavController,
    user: User? = null,
    onLogout: () -> Unit = {}
) {
    // ✅ 获取应用实例和DAO
    val context = LocalContext.current
    val application = context.applicationContext as AchieveApp
    val taskDao = application.database.taskDao()
    val habitDao = application.database.habitDao()

    // ✅ 统计数据状态
    var completedTasksCount by remember { mutableIntStateOf(0) }
    var activeHabitsCount by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ 实时加载统计数据
    LaunchedEffect(user?.userId) {
        user?.userId?.let { userId ->
            try {
                // 并行获取统计数据
                completedTasksCount = taskDao.getCompletedTasksCount(userId)
                activeHabitsCount = habitDao.getActiveHabitsCount(userId)
                longestStreak = habitDao.getLongestStreak(userId)
            } catch (e: Exception) {
                // 处理错误，保持默认值
                completedTasksCount = 0
                activeHabitsCount = 0
                longestStreak = 0
            } finally {
                isLoading = false
            }
        } ?: run {
            // 用户未登录时重置数据
            completedTasksCount = 0
            activeHabitsCount = 0
            longestStreak = 0
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 用户信息卡片
        UserInfoCard(
            user = user,
            onEditProfile = {
                // 编辑资料功能
            }
        )

        // ✅ 动态统计信息卡片
        StatisticsCard(
            completedTasks = completedTasksCount,
            activeHabits = activeHabitsCount,
            longestStreak = longestStreak,
            isLoading = isLoading
        )

        // 设置部分
        SettingsSection(navController = navController)

        // 其他功能
        OtherActionsSection(
            onLogout = onLogout
        )
    }
}

@Composable
private fun UserInfoCard(
    user: User?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "头像",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 用户信息
                Column(modifier = Modifier.weight(1f)) {
                    if (user != null) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "未登录",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ✅ 更新统计卡片，支持动态数据和加载状态
@Composable
private fun StatisticsCard(
    completedTasks: Int,
    activeHabits: Int,
    longestStreak: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "使用统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // ✅ 刷新指示器
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = if (isLoading) "--" else completedTasks.toString(),
                    label = "完成任务",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary,
                    isLoading = isLoading
                )
                StatisticItem(
                    value = if (isLoading) "--" else activeHabits.toString(),
                    label = "活跃习惯",
                    icon = Icons.Default.Repeat,
                    color = MaterialTheme.colorScheme.secondary,
                    isLoading = isLoading
                )
                StatisticItem(
                    value = if (isLoading) "--" else "${longestStreak}天",
                    label = "最长连续",
                    icon = Icons.Default.CalendarToday,
                    color = MaterialTheme.colorScheme.tertiary,
                    isLoading = isLoading
                )
            }
        }
    }
}

// ✅ 更新统计项，支持加载状态
@Composable
private fun StatisticItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    isLoading: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 通知设置
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "通知提醒",
                subtitle = "管理每日任务和习惯提醒",
                onClick = {
                    navController.navigate(Screen.NotificationSettings.route)
                }
            )
        }
    }
}

@Composable
private fun OtherActionsSection(
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "其他",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 退出登录按钮
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 40.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}