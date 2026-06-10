package com.tingjizhushou.ui.screens.result

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.ui.components.LoadingOverlay

/**
 * 结果页面
 * 显示转写内容、会议纪要，提供保存和分享功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentRecord by viewModel.currentRecord.collectAsState()
    val editedTranscript by viewModel.editedTranscript.collectAsState()
    val saveOptions by viewModel.saveOptions.collectAsState()
    
    val context = LocalContext.current
    
    // 当前选中的标签页
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("转写内容", "会议纪要", "录音")
    
    // 加载记录
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    
    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // 可以显示一个Snackbar或Toast
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("转写结果") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = viewModel.shareContent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "分享到"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            )
        }
    ) { paddingValues ->
        LoadingOverlay(
            isLoading = uiState.isLoading,
            message = "加载中..."
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 标签页
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                // 内容区域
                when (selectedTab) {
                    0 -> TranscriptTab(
                        transcript = editedTranscript,
                        onTranscriptChange = { viewModel.updateTranscript(it) }
                    )
                    1 -> SummaryTab(
                        summary = currentRecord?.meetingMinutes ?: "",
                        onRegenerate = { viewModel.regenerateSummary() },
                        isGenerating = uiState.isGeneratingSummary
                    )
                    2 -> AudioTab(
                        record = currentRecord
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 保存选项
                SaveOptionsSection(
                    options = saveOptions,
                    onOptionsChange = { viewModel.updateSaveOptions(it) }
                )
                
                // 保存按钮
                Button(
                    onClick = { viewModel.saveRecord() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存")
                }
            }
        }
    }
}

/**
 * 转写内容标签页
 */
@Composable
private fun TranscriptTab(
    transcript: String,
    onTranscriptChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "转写内容（可编辑）",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = transcript,
                onValueChange = onTranscriptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 200.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 会议纪要标签页
 */
@Composable
private fun SummaryTab(
    summary: String,
    onRegenerate: () -> Unit,
    isGenerating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "会议纪要",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            TextButton(
                onClick = onRegenerate,
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("重新生成")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = summary.ifBlank { "暂无会议纪要，请先生成" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 录音标签页
 */
@Composable
private fun AudioTab(
    record: RecordEntity?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (record?.audioFilePath != null) {
            // 音频播放控件（可扩展）
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "录音文件",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "时长: ${formatDuration(record.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "播放",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Text(
                text = "暂无录音文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 保存选项区域
 */
@Composable
private fun SaveOptionsSection(
    options: SaveOptions,
    onOptionsChange: (SaveOptions) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "选择保存内容",
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.saveAudio,
                        onCheckedChange = {
                            onOptionsChange(options.copy(saveAudio = it))
                        }
                    )
                    Text("录音")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.saveTranscript,
                        onCheckedChange = {
                            onOptionsChange(options.copy(saveTranscript = it))
                        }
                    )
                    Text("转写")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.saveSummary,
                        onCheckedChange = {
                            onOptionsChange(options.copy(saveSummary = it))
                        }
                    )
                    Text("纪要")
                }
            }
        }
    }
}

/**
 * 格式化时长
 */
private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}
