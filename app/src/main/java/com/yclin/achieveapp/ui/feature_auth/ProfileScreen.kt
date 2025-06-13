package com.yclin.achieveapp.ui.feature_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yclin.achieveapp.data.database.entity.User

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 32.dp, end = 32.dp)
        ) {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(32.dp))
            if (user != null) {
                Text(text = "用户名：${user.username}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                Text(text = "邮箱：${user.email}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("退出登录")
                }
            } else {
                Text(
                    "未登录",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}