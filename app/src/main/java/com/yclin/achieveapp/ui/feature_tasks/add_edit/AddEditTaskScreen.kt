package com.yclin.achieveapp.ui.feature_tasks.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yclin.achieveapp.data.database.entity.QuadrantType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    navController: NavController,
    viewModel: AddEditTaskViewModel,
    modifier: Modifier = Modifier
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val isImportant by viewModel.isImportant.collectAsState()
    val isUrgent by viewModel.isUrgent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationSuccess by viewModel.operationSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val taskId by viewModel.taskId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // 监听操作成功
    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            navController.popBackStack()
            viewModel.resetOperationSuccess()
        }
    }

    // 监听错误
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (taskId == -1L) "新建任务" else "编辑任务")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveTask() },
                        enabled = !isLoading && title.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 任务标题
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::updateTitle,
                label = { Text("任务标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null && title.isBlank()
            )

            // 任务描述
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::updateDescription,
                label = { Text("任务描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 四象限选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "四象限分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 重要性选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isImportant,
                            onCheckedChange = viewModel::updateImportant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "重要",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "对目标达成有重大影响",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 紧急性选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isUrgent,
                            onCheckedChange = viewModel::updateUrgent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "紧急",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "需要立即处理或有明确截止时间",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 当前象限显示
                    val currentQuadrant = viewModel.getCurrentQuadrant()
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (currentQuadrant) {
                                QuadrantType.URGENT_IMPORTANT -> MaterialTheme.colorScheme.errorContainer
                                QuadrantType.IMPORTANT_NOT_URGENT -> MaterialTheme.colorScheme.primaryContainer
                                QuadrantType.URGENT_NOT_IMPORTANT -> MaterialTheme.colorScheme.tertiaryContainer
                                QuadrantType.NOT_URGENT_NOT_IMPORTANT -> MaterialTheme.colorScheme.surfaceContainer
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "当前象限：${currentQuadrant.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentQuadrant.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // 截止日期
            OutlinedTextField(
                value = dueDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "",
                onValueChange = { },
                label = { Text("截止日期（可选）") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Row {
                        if (dueDate != null) {
                            IconButton(onClick = { viewModel.updateDueDate(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除日期")
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            viewModel.updateDueDate(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}