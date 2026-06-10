package com.tingjizhushou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tingjizhushou.ui.state.RecordingStatus

/**
 * 录音按钮组件
 * 支持三种状态：空闲、录音中、暂停
 */
@Composable
fun RecordButton(
    status: RecordingStatus,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    // 颜色动画
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            RecordingStatus.IDLE -> MaterialTheme.colorScheme.primary
            RecordingStatus.RECORDING -> Color.Red
            RecordingStatus.PAUSED -> Color(0xFFFF9800) // Orange
            RecordingStatus.COMPLETED -> Color.Gray
            RecordingStatus.ERROR -> Color.Red.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )
    
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈脉冲效果（录音中时显示）
        if (status == RecordingStatus.RECORDING) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(
                        color = backgroundColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
        
        // 主按钮
        FloatingActionButton(
            onClick = {
                when (status) {
                    RecordingStatus.IDLE -> onStartClick()
                    RecordingStatus.RECORDING -> onPauseClick()
                    RecordingStatus.PAUSED -> onResumeClick()
                    RecordingStatus.COMPLETED -> {} // 不响应
                    RecordingStatus.ERROR -> onStartClick()
                }
            },
            modifier = Modifier.size(80.dp),
            containerColor = backgroundColor,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = when (status) {
                    RecordingStatus.IDLE -> Icons.Default.Mic
                    RecordingStatus.RECORDING -> Icons.Default.Pause
                    RecordingStatus.PAUSED -> Icons.Default.Mic
                    RecordingStatus.COMPLETED -> Icons.Default.Mic
                    RecordingStatus.ERROR -> Icons.Default.Mic
                },
                contentDescription = when (status) {
                    RecordingStatus.IDLE -> "开始录音"
                    RecordingStatus.RECORDING -> "暂停录音"
                    RecordingStatus.PAUSED -> "继续录音"
                    RecordingStatus.COMPLETED -> "录音完成"
                    RecordingStatus.ERROR -> "重试录音"
                },
                modifier = Modifier.size(36.dp)
            )
        }
        
        // 停止按钮（录音中或暂停时显示）
        if (status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED) {
            FloatingActionButton(
                onClick = onStopClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp),
                containerColor = Color.Gray,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止录音",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
