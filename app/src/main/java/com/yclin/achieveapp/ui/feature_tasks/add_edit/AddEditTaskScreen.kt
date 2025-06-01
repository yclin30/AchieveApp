package com.yclin.achieveapp.ui.feature_tasks.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    val priority by viewModel.priority.collectAsState()
    val taskId by viewModel.taskId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationSuccess by viewModel.operationSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val titleFocusRequester = remember { FocusRequester() }

    // 操作成功后导航回上一页
    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            viewModel.resetOperationSuccess()
            navController.popBackStack()
        }
    }

    // 如果是新任务，自动聚焦到标题输入框
    LaunchedEffect(Unit) {
        if (taskId == -1L) {
            titleFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (taskId == -1L) "新建任务" else "编辑任务") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题输入
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("任务标题") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocusRequester),
                        isError = title.isBlank(),
                        supportingText = {
                            if (title.isBlank()) {
                                Text("标题不能为空")
                            }
                        }
                    )

                    // 描述输入
                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("任务描述（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // 截止日期选择
                    OutlinedTextField(
                        value = dueDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "",
                        onValueChange = { },
                        label = { Text("截止日期（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "选择日期"
                                    )
                                }
                                if (dueDate != null) {
                                    IconButton(onClick = { viewModel.updateDueDate(null) }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "清除日期"
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // 日期选择器对话框
                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())
                                ?.toInstant()?.toEpochMilli()
                        )

                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let {
                                            val selectedDate = Instant.ofEpochMilli(it)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
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

                    // 优先级选择
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = priority == 1,
                            onClick = { viewModel.updatePriority(1) }
                        )
                        Text(
                            text = "高",
                            modifier = Modifier.clickable { viewModel.updatePriority(1) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        RadioButton(
                            selected = priority == 2,
                            onClick = { viewModel.updatePriority(2) }
                        )
                        Text(
                            text = "中",
                            modifier = Modifier.clickable { viewModel.updatePriority(2) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        RadioButton(
                            selected = priority == 3,
                            onClick = { viewModel.updatePriority(3) }
                        )
                        Text(
                            text = "低",
                            modifier = Modifier.clickable { viewModel.updatePriority(3) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        RadioButton(
                            selected = priority == 0,
                            onClick = { viewModel.updatePriority(0) }
                        )
                        Text(
                            text = "无",
                            modifier = Modifier.clickable { viewModel.updatePriority(0) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 保存按钮
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                return@Button
                            }
                            viewModel.saveTask()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = title.isNotBlank()
                    ) {
                        Text(text = if (taskId == -1L) "添加任务" else "更新任务")
                    }
                }
            }
        }
    }
}