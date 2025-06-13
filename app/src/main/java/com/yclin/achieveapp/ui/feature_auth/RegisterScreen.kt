package com.yclin.achieveapp.ui.feature_auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    registerError: String? = null,
    loading: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("注册", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text("确认密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (registerError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(registerError, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onRegister(username, email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && password == confirmPwd && !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("注册")
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text("已有账号？去登录")
                }
            }
        }
    }
}