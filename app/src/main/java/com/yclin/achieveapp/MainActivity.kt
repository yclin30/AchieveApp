package com.yclin.achieveapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yclin.achieveapp.ui.components.AchieveBottomNavigation
import com.yclin.achieveapp.ui.feature_dashboard.DashboardScreen
import com.yclin.achieveapp.ui.feature_dashboard.DashboardViewModel
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitScreen
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitViewModel
import com.yclin.achieveapp.ui.feature_habits.detail.HabitDetailScreen
import com.yclin.achieveapp.ui.feature_habits.detail.HabitDetailViewModel
import com.yclin.achieveapp.ui.feature_habits.list.HabitListScreen
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskScreen
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskViewModel
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListScreen
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListViewModel
import com.yclin.achieveapp.ui.navigation.Screen
import com.yclin.achieveapp.ui.theme.AchieveTheme
import com.yclin.achieveapp.ui.feature_auth.LoginScreen
import com.yclin.achieveapp.ui.feature_auth.RegisterScreen
import com.yclin.achieveapp.ui.feature_profile.ProfileScreen
import com.yclin.achieveapp.ui.feature_auth.AuthViewModel
import com.yclin.achieveapp.ui.feature_auth.AuthViewModelFactory
import com.yclin.achieveapp.ui.feature_notification.NotificationSettingsScreen
import com.yclin.achieveapp.ui.feature_notification.NotificationSettingsViewModel
import com.yclin.achieveapp.ui.feature_search.SearchScreen
import com.yclin.achieveapp.ui.feature_search.SearchViewModel

class MainActivity : ComponentActivity() {

    // ✅ 权限请求启动器
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，可以发送通知
            // 可以显示成功提示或启用通知功能
        } else {
            // 权限被拒绝，可以显示说明或引导用户到设置
            // 你可以在这里显示一个对话框解释为什么需要权限
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 应用启动时检查和请求通知权限
        checkAndRequestNotificationPermission()

        // ✅ 存储当前用户ID到SharedPreferences（为通知功能使用）
        storeCurrentUserId()

        setContent {
            AchieveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(application))
                    val user by authViewModel.user.collectAsState()
                    val loading by authViewModel.loading.collectAsState()
                    val error by authViewModel.error.collectAsState()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // ✅ 用户变化时更新存储的用户ID
                    user?.let { currentUser ->
                        storeCurrentUserId(currentUser.userId)
                    }

                    val showBottomBar = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Habits.route,
                        Screen.Search.route, // ✅ 添加搜索页面到底部导航
                        Screen.Profile.route
                    )

                    // 登录后进主界面，未登录进登录页
                    val startDestination = if (user == null) Screen.Login.route else Screen.Dashboard.route

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                AchieveBottomNavigation(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // ========== 登录页面 =========
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onLogin = { username, password ->
                                        authViewModel.login(username, password) {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate(Screen.Register.route)
                                    },
                                    loginError = error,
                                    loading = loading
                                )
                            }
                            // ========== 注册页面 =========
                            composable(Screen.Register.route) {
                                RegisterScreen(
                                    onRegister = { username, email, password ->
                                        authViewModel.register(username, email, password) {
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(Screen.Register.route) { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.popBackStack()
                                    },
                                    registerError = error,
                                    loading = loading
                                )
                            }
                            // ========== 我的页面 =========
                            composable(Screen.Profile.route) {
                                ProfileScreen(
                                    navController = navController,
                                    user = user,
                                    onLogout = {
                                        authViewModel.logout {
                                            // ✅ 退出登录时清除用户ID
                                            clearCurrentUserId()
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0)
                                            }
                                        }
                                    }
                                )
                            }
                            // ✅ 通知设置页面
                            composable(Screen.NotificationSettings.route) {
                                NotificationSettingsScreen(
                                    navController = navController,
                                    viewModel = viewModel(
                                        factory = NotificationSettingsViewModel.provideFactory()
                                    )
                                )
                            }
                            // ========== 仪表盘 =========
                            composable(Screen.Dashboard.route) {
                                val application = this@MainActivity.application as AchieveApp
                                val dashboardViewModel: DashboardViewModel = viewModel(
                                    factory = DashboardViewModel.provideFactory(
                                        userId = user?.userId ?: -1L,
                                        taskDao = application.database.taskDao(),
                                        habitDao = application.database.habitDao(),
                                        habitCompletionDao = application.database.habitCompletionDao(),
                                        application = application
                                    )
                                )
                                DashboardScreen(
                                    navController = navController,
                                    viewModel = dashboardViewModel
                                )
                            }
                            // ========== 任务列表 =========
                            composable(Screen.Tasks.route) {
                                val taskListViewModel: TaskListViewModel = viewModel(
                                    factory = TaskListViewModel.Factory(application as AchieveApp, user?.userId ?: -1L)
                                )
                                TaskListScreen(
                                    navController = navController,
                                    viewModel = taskListViewModel
                                )
                            }
                            // ========== 习惯列表 =========
                            composable(Screen.Habits.route) {
                                HabitListScreen(navController = navController,
                                    userId = user?.userId ?: -1L)
                            }
                            // ✅ 搜索页面
                            composable(Screen.Search.route) {
                                SearchScreen(
                                    navController = navController,
                                    viewModel = viewModel(
                                        factory = SearchViewModel.provideFactory(user?.userId ?: -1L)
                                    )
                                )
                            }
                            // ========== 添加/编辑任务 =========
                            composable(Screen.AddEditTask.route) {
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(userId = user?.userId ?: -1L,-1L)
                                )
                                AddEditTaskScreen(
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }
                            composable(
                                route = "${Screen.AddEditTask.route}/{taskId}",
                                arguments = listOf(
                                    navArgument("taskId") {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) { backStackEntry ->
                                val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(
                                        userId = user?.userId ?: -1L,
                                        taskId = taskId
                                    )
                                )
                                AddEditTaskScreen(
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }
                            // ========== 添加/编辑习惯 =========
                            composable(Screen.AddEditHabit.route) {
                                val addEditHabitViewModel: AddEditHabitViewModel = viewModel(
                                    factory = AddEditHabitViewModel.provideFactory(user?.userId ?: -1L, -1L)
                                )
                                AddEditHabitScreen(
                                    navController = navController,
                                    viewModel = addEditHabitViewModel
                                )
                            }
                            composable(
                                route = "${Screen.AddEditHabit.route}/{habitId}",
                                arguments = listOf(
                                    navArgument("habitId") {
                                        type = NavType.LongType
                                        defaultValue = -1L
                                    }
                                )
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
                                val addEditHabitViewModel: AddEditHabitViewModel = viewModel(
                                    factory = AddEditHabitViewModel.provideFactory(
                                        user?.userId ?: -1L,
                                        habitId
                                    )
                                )
                                AddEditHabitScreen(
                                    navController = navController,
                                    viewModel = addEditHabitViewModel
                                )
                            }
                            // ========== 习惯详情 =========
                            composable(
                                route = Screen.HabitDetail.route,
                                arguments = listOf(
                                    navArgument("habitId") {
                                        type = NavType.LongType
                                    }
                                )
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
                                val habitDetailViewModel: HabitDetailViewModel = viewModel(
                                    factory = HabitDetailViewModel.provideFactory(
                                        habitId,
                                        userId = user?.userId ?: -1L,
                                        application as AchieveApp
                                    )
                                )
                                HabitDetailScreen(
                                    navController = navController,
                                    viewModel = habitDetailViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ 检查和请求通知权限
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予，无需操作
                }
                else -> {
                    // 请求权限
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // Android 13 以下版本默认有通知权限
    }

    // ✅ 存储当前用户ID（供通知功能使用）
    private fun storeCurrentUserId(userId: Long = 1L) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putLong("current_user_id", userId)
            .apply()
    }

    // ✅ 清除用户ID
    private fun clearCurrentUserId() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .remove("current_user_id")
            .apply()
    }

    // ✅ 获取当前用户ID
    fun getCurrentUserId(): Long {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("current_user_id", 1L)
    }

    // ✅ 检查通知权限状态
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下默认有权限
        }
    }

    // ✅ 手动请求通知权限（供设置页面调用）
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}