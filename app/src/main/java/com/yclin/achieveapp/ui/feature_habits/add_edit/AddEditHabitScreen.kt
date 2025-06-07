package com.yclin.achieveapp.ui.feature_habits.add_edit

import androidx.compose.foundation.layout.*
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
    val operationSuccess by viewModel.operationSuccess.collectAsState()

    LaunchedEffect(operationSuccess) {
        if (operationSuccess) {
            viewModel.resetOperationSuccess()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("添加/编辑习惯") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("习惯名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::updateDescription,
                label = { Text("习惯描述（可选）") },
                modifier = Modifier.fillMaxWidth()
            )

            // 频率选择
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("频率：")
                RadioButton(
                    selected = frequencyType == 1,
                    onClick = { viewModel.updateFrequencyType(1) }
                )
                Text("每日")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = frequencyType == 2,
                    onClick = { viewModel.updateFrequencyType(2) }
                )
                Text("每周")
            }
            // 若为每周，显示选择周几
            if (frequencyType == 2) {
                val days = listOf("一", "二", "三", "四", "五", "六", "日")
                Row {
                    days.forEachIndexed { idx, label ->
                        val bit = 1 shl idx
                        val checked = weekDays and bit != 0
                        FilterChip(
                            selected = checked,
                            onClick = {
                                val new = if (checked) weekDays and bit.inv() else weekDays or bit
                                viewModel.updateWeekDays(new)
                            },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
            Button(
                onClick = { viewModel.saveHabit() },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}