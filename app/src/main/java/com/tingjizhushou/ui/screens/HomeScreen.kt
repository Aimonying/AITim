package com.tingjizhushou.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tingjizhushou.R
import com.tingjizhushou.ui.RecordViewModel
import com.tingjizhushou.ui.theme.TingJiZhuShouTheme

/**
 * 主屏幕
 * 显示三个核心功能入口：实时录音、上传录音、输入文本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: RecordViewModel = viewModel()
    val context = LocalContext.current
    
    TingJiZhuShouTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "听记助手") },
                    actions = {
                        // 设置按钮
                        IconButton(onClick = { /* TODO: 打开设置 */ }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                item {
                    Text(
                        text = "欢迎使用听记助手",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                // 功能卡片1：实时录音
                item {
                    FunctionCard(
                        title = "🎤 实时录音",
                        description = "点击开始录音并实时转文字",
                        onClick = {
                            navController.navigate("record/REALTIME")
                        }
                    )
                }
                
                // 功能卡片2：上传录音
                item {
                    FunctionCard(
                        title = "📁 上传录音",
                        description = "选择音频文件进行转写",
                        onClick = {
                            navController.navigate("record/UPLOAD")
                        }
                    )
                }
                
                // 功能卡片3：输入文本
                item {
                    FunctionCard(
                        title = "⌨️ 输入文本",
                        description = "直接输入或粘贴文本生成纪要",
                        onClick = {
                            navController.navigate("record/TEXT")
                        }
                    )
                }
                
                // 最近记录入口
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "查看历史记录")
                    }
                }
            }
        }
    }
}

/**
 * 功能卡片组件
 */
@Composable
fun FunctionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
