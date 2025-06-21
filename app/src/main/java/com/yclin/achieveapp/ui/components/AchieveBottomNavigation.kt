package com.yclin.achieveapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yclin.achieveapp.ui.navigation.Screen

@Composable
fun AchieveBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // 避免在导航栈中堆积相同的目标
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    DASHBOARD("仪表盘", Icons.Default.Dashboard, Screen.Dashboard.route),
    TASKS("四象限", Icons.Default.GridView, Screen.Tasks.route),
    HABITS("习惯", Icons.Default.Repeat, Screen.Habits.route),
    SEARCH("搜索", Icons.Default.Search, Screen.Search.route),
    PROFILE("我的", Icons.Default.Person, Screen.Profile.route)
}