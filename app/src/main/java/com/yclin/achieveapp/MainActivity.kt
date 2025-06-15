package com.yclin.achieveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                    val showBottomBar = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Habits.route,
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
                                    user = user,
                                    onLogout = {
                                        authViewModel.logout {
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0)
                                            }
                                        }
                                    }
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
}