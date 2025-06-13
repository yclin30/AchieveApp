package com.yclin.achieveapp.ui.feature_auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    loginError: String? = null,
    loading: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                Text("登录", style = MaterialTheme.typography.headlineMedium)
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
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (loginError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(loginError, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onLogin(username, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && password.isNotBlank() && !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("登录")
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("没有账号？立即注册")
                }
            }
        }
    }
}