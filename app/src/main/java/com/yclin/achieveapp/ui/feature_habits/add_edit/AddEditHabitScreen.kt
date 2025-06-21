package com.yclin.achieveapp.ui.feature_habits.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    navController: NavController,
    viewModel: AddEditHabitViewModel,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val frequencyType by viewModel.frequencyType.collectAsState()
    val weekDays by viewModel.weekDays.collectAsState()
    val habitId by viewModel.habitId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val operationSuccess by viewModel.operationSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 监听操作成功
    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            viewModel.resetOperationSuccess()
            navController.popBackStack()
        }
    }

    // 监听错误
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (habitId == -1L) "添加习惯" else "编辑习惯")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        if (isLoading) {
            // 显示加载状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 习惯名称
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::updateName,
                    label = { Text("习惯名称") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error?.contains("名称") == true,
                    supportingText = if (error?.contains("名称") == true) {
                        { Text(error!!, color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // 习惯描述
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("习惯描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // 频率选择
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "频率设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = frequencyType == 1,
                                onClick = { viewModel.updateFrequencyType(1) }
                            )
                            Text("每日")
                            Spacer(modifier = Modifier.width(24.dp))
                            RadioButton(
                                selected = frequencyType == 2,
                                onClick = { viewModel.updateFrequencyType(2) }
                            )
                            Text("每周")
                        }

                        // 每周频率时显示周几选择
                        if (frequencyType == 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "选择周几：",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val days = listOf("一", "二", "三", "四", "五", "六", "日")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                days.forEachIndexed { idx, label ->
                                    val bit = 1 shl idx
                                    val checked = weekDays and bit != 0
                                    FilterChip(
                                        selected = checked,
                                        onClick = {
                                            val new = if (checked) weekDays and bit.inv() else weekDays or bit
                                            viewModel.updateWeekDays(new)
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            // 如果是每周频率但没选择任何天，显示错误
                            if (error?.contains("至少需要选择一天") == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 保存按钮
                Button(
                    onClick = { viewModel.saveHabit() },
                    enabled = name.isNotBlank() && !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存中...")
                    } else {
                        Text("保存")
                    }
                }
            }
        }
    }
}