package com.yclin.achieveapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme // Material 3 主题相关
import androidx.compose.material3.Scaffold // Material 3 脚手架布局
import androidx.compose.material3.Surface // Material 3 表面组件
import androidx.compose.runtime.getValue // 导入 getValue 以便使用 by 委托
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // 导入 viewModel Composable 函数
import androidx.navigation.NavType // 导航参数类型
import androidx.navigation.compose.NavHost // 导航容器
import androidx.navigation.compose.composable // 定义导航目标
import androidx.navigation.compose.currentBackStackEntryAsState // 获取当前导航状态
import androidx.navigation.compose.rememberNavController // 创建和记住 NavController
import androidx.navigation.navArgument // 定义导航参数
// UI 组件
import com.yclin.achieveapp.ui.components.AchieveBottomNavigation
// 各个功能的屏幕和 ViewModel
import com.yclin.achieveapp.ui.feature_dashboard.DashboardScreen
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitScreen
import com.yclin.achieveapp.ui.feature_habits.add_edit.AddEditHabitViewModel
import com.yclin.achieveapp.ui.feature_habits.detail.HabitDetailScreen
import com.yclin.achieveapp.ui.feature_habits.detail.HabitDetailViewModel
import com.yclin.achieveapp.ui.feature_habits.list.HabitListScreen
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskScreen
import com.yclin.achieveapp.ui.feature_tasks.add_edit.AddEditTaskViewModel
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListScreen
import com.yclin.achieveapp.ui.feature_tasks.list.TaskListViewModel
// 导航路由定义
import com.yclin.achieveapp.ui.navigation.Screen
// 应用主题
import com.yclin.achieveapp.ui.theme.AchieveTheme

/**
 * 应用的主 Activity，是所有 UI 的入口点。
 */
class MainActivity : ComponentActivity() {
    /**
     * Activity 创建时调用。
     * 在这里设置 Compose UI 内容。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 setContent 设置 Compose UI
        setContent {
            // 应用自定义的 AchieveTheme 主题
            AchieveTheme {
                // Surface 是一个 Material Design 组件，用于绘制带有背景颜色和阴影的区域
                Surface(
                    modifier = Modifier.fillMaxSize(), // 使 Surface 填满整个可用空间
                    color = MaterialTheme.colorScheme.background // 设置背景色为主题定义的背景色
                ) {
                    // 创建并记住一个 NavController 实例，用于在 Composable 之间导航
                    val navController = rememberNavController()
                    // 获取当前导航栈的条目作为 Compose 状态，当路由改变时，UI 会自动更新
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    // 从导航栈条目中获取当前路由的字符串路径
                    val currentRoute = navBackStackEntry?.destination?.route

                    // 判断当前路由是否是预定义的主屏幕路由之一，用于决定是否显示底部导航栏
                    val showBottomBar = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.Tasks.route,
                        Screen.Habits.route
                    )

                    // Scaffold 是一个高级别的 Material Design 布局组件，
                    // 它提供了放置 TopAppBar, BottomAppBar, FloatingActionButton 等标准元素的槽位。
                    Scaffold(
                        // 定义底部导航栏的内容
                        bottomBar = {
                            // 只有当 showBottomBar 为 true 时才显示 AchieveBottomNavigation
                            if (showBottomBar) {
                                AchieveBottomNavigation(navController = navController)
                            }
                        }
                    ) { innerPadding -> // innerPadding 是 Scaffold 提供的，用于确保 NavHost 内容不会被 AppBar 或 BottomBar遮挡
                        // NavHost 是导航图的容器，在这里定义所有可导航的目标（Composable 函数）
                        NavHost(
                            navController = navController, // 将 NavController 传递给 NavHost
                            startDestination = Screen.Dashboard.route, // 设置应用的起始导航目标
                            modifier = Modifier.padding(innerPadding) // 应用 Scaffold 提供的内边距
                        ) {
                            // --- 仪表盘屏幕 ---
                            composable(Screen.Dashboard.route) { // 定义仪表盘屏幕的导航目标
                                DashboardScreen(navController = navController) // 加载仪表盘屏幕的 Composable
                            }

                            // --- 任务列表屏幕 ---
                            composable(Screen.Tasks.route) { // 定义任务列表屏幕的导航目标
                                // 使用 viewModel() 获取 TaskListViewModel 实例
                                // 通过 Factory 传入 Application 实例，因为 ViewModel 可能需要它
                                val taskListViewModel: TaskListViewModel = viewModel(
                                    factory = TaskListViewModel.Factory(application as AchieveApp)
                                )
                                TaskListScreen( // 加载任务列表屏幕的 Composable
                                    navController = navController,
                                    viewModel = taskListViewModel
                                )
                            }

                            // --- 习惯列表屏幕 ---
                            composable(Screen.Habits.route) { // 定义习惯列表屏幕的导航目标
                                // 注意：HabitListScreen 在这里没有显式传递 ViewModel。
                                // 它可能内部使用 viewModel() 获取默认实例，或者其 ViewModel 不需要 Application。
                                HabitListScreen(navController = navController) // 加载习惯列表屏幕的 Composable
                            }

                            // --- 添加任务页面 ---
                            // 定义添加新任务的导航目标
                            composable(Screen.AddEditTask.route) {
                                // 获取 AddEditTaskViewModel 实例，传入 -1L 表示这是一个新建任务的操作
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(-1L)
                                )
                                AddEditTaskScreen( // 加载添加/编辑任务屏幕的 Composable
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }

                            // --- 编辑任务页面 ---
                            // 定义编辑现有任务的导航目标，路由中包含一个 taskId 参数
                            composable(
                                route = "${Screen.AddEditTask.route}/{taskId}", // 路由路径包含参数 taskId
                                arguments = listOf( // 定义导航参数
                                    navArgument("taskId") { // 参数名为 "taskId"
                                        type = NavType.LongType // 参数类型为 Long
                                        defaultValue = -1L      // 如果导航时未提供 taskId，则默认为 -1L
                                    }
                                )
                            ) { backStackEntry -> // backStackEntry 包含当前路由的信息和参数
                                // 从 backStackEntry 中获取 taskId 参数值
                                val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
                                // 获取 AddEditTaskViewModel 实例，传入获取到的 taskId
                                val addEditTaskViewModel: AddEditTaskViewModel = viewModel(
                                    factory = AddEditTaskViewModel.provideFactory(taskId)
                                )
                                AddEditTaskScreen( // 加载添加/编辑任务屏幕的 Composable
                                    navController = navController,
                                    viewModel = addEditTaskViewModel
                                )
                            }

                            // --- 添加习惯页面 ---
                            composable(Screen.AddEditHabit.route) { // 定义添加新习惯的导航目标
                                val addEditHabitViewModel: AddEditHabitViewModel = viewModel(
                                    factory = AddEditHabitViewModel.provideFactory(-1L) // -1L 表示新建习惯
                                )
                                AddEditHabitScreen( // 加载添加/编辑习惯屏幕的 Composable
                                    navController = navController,
                                    viewModel = addEditHabitViewModel
                                )
                            }

                            // --- 编辑习惯页面 ---
                            composable(
                                route = "${Screen.AddEditHabit.route}/{habitId}", // 路由路径包含参数 habitId
                                arguments = listOf(
                                    navArgument("habitId") { // 参数名为 "habitId"
                                        type = NavType.LongType // 参数类型为 Long
                                        defaultValue = -1L      // 默认值为 -1L
                                    }
                                )
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L // 获取 habitId
                                val addEditHabitViewModel: AddEditHabitViewModel = viewModel(
                                    factory = AddEditHabitViewModel.provideFactory(habitId) // 传入 habitId
                                )
                                AddEditHabitScreen( // 加载添加/编辑习惯屏幕的 Composable
                                    navController = navController,
                                    viewModel = addEditHabitViewModel
                                )
                            }

                            // --- 习惯详情页面 ---
                            // 定义习惯详情页面的导航目标，路由中包含一个 habitId 参数
                            composable(
                                route = Screen.HabitDetail.route, // Screen.HabitDetail.route 应为 "habit_detail/{habitId}" 或类似形式
                                arguments = listOf(
                                    navArgument("habitId") { // 参数名为 "habitId"
                                        type = NavType.LongType // 参数类型为 Long，这里没有默认值，表示必须提供
                                    }
                                )
                            ) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L // 获取 habitId
                                // 获取 HabitDetailViewModel 实例，通过工厂传入 habitId 和 Application 实例
                                val habitDetailViewModel: HabitDetailViewModel = viewModel(
                                    factory = HabitDetailViewModel.provideFactory(
                                        habitId,
                                        application as AchieveApp // 将 Application 实例传递给 ViewModel 工厂
                                    )
                                )
                                HabitDetailScreen( // 加载习惯详情屏幕的 Composable
                                    navController = navController,
                                    viewModel = habitDetailViewModel
                                )
                            }
                        } // NavHost 结束
                    } // Scaffold 结束
                } // Surface 结束
            } // AchieveTheme 结束
        } // setContent 结束
    } // onCreate 结束
} // MainActivity 类结束