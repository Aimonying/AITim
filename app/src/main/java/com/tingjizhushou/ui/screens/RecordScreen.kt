package com.tingjizhushou.ui.screens

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileAudio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tingjizhushou.R
import com.tingjizhushou.ui.RecordViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 录音界面
 * 支持三种模式：实时录音、上传录音、输入文本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    navController: NavController,
    recordType: String
) {
    val viewModel: RecordViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var transcriptText by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isTranscribing by remember { mutableStateOf(false) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startRecording(context, viewModel)
            isRecording = true
        }
    }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleAudioFile(context, it) { result ->
                when (result) {
                    is AudioFileResult.Success -> {
                        selectedFileName = result.fileName
                        isTranscribing = true
                        // 模拟转写过程
                        coroutineScope.launch {
                            delay(2000)
                            transcriptText = "这是模拟的转写结果。\n\n音频文件已成功分析，转写内容将显示在这里。\n\n会议主题：项目进度汇报\n参会人员：张三、李四、王五\n会议内容：讨论了Q2季度的项目进度和下一步计划。"
                            isTranscribing = false
                        }
                    }
                    is AudioFileResult.Error -> {
                        errorMessage = result.message
                        showErrorDialog = true
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000L)
                recordingDuration++
                viewModel.updateUiState(
                    viewModel.uiState.value.copy(recordingDuration = recordingDuration)
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (recordType) {
                            "REALTIME" -> "实时录音"
                            "UPLOAD" -> "上传录音"
                            "TEXT" -> "输入文本"
                            else -> "录音"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (recordType) {
                "REALTIME" -> {
                    RealtimeRecordingContent(
                        isRecording = isRecording,
                        duration = recordingDuration,
                        transcriptText = transcriptText,
                        onStartRecording = {
                            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                arrayOf(Manifest.permission.RECORD_AUDIO)
                            } else {
                                arrayOf(
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            }
                            
                            val allGranted = permissions.all { permission ->
                                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                            }
                            
                            if (allGranted) {
                                startRecording(context, viewModel)
                                isRecording = true
                            } else {
                                permissionLauncher.launch(permissions)
                            }
                        },
                        onStopRecording = {
                            stopRecording(context, viewModel)
                            isRecording = false
                            showSaveDialog = true
                        }
                    )
                }
                
                "UPLOAD" -> {
                    UploadRecordingContent(
                        onUpload = {
                            filePickerLauncher.launch("audio/*")
                        },
                        transcriptText = transcriptText,
                        isTranscribing = isTranscribing,
                        selectedFileName = selectedFileName,
                        onGenerateMinutes = {
                            generateMeetingMinutes(transcriptText) { minutes ->
                                saveRecord(context, viewModel, transcriptText, minutes)
                                navController.popBackStack()
                            }
                        }
                    )
                }
                
                "TEXT" -> {
                    TextInputContent(
                        transcriptText = transcriptText,
                        onTextChange = { transcriptText = it },
                        onGenerateMinutes = {
                            generateMeetingMinutes(transcriptText) { minutes ->
                                saveRecord(context, viewModel, transcriptText, minutes)
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showSaveDialog) {
        SaveDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { saveOptions ->
                saveRecordWithOptions(context, viewModel, transcriptText, saveOptions)
                showSaveDialog = false
                navController.popBackStack()
            }
        )
    }
    
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("错误") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 音频文件处理结果
 */
sealed class AudioFileResult {
    data class Success(val fileName: String, val filePath: String) : AudioFileResult()
    data class Error(val message: String) : AudioFileResult()
}

/**
 * 处理音频文件
 */
private fun handleAudioFile(
    context: android.content.Context,
    uri: Uri,
    callback: (AudioFileResult) -> Unit
) {
    // 文件格式验证
    val supportedFormats = listOf("mp3", "wav", "amr", "aac", "m4a")
    val mimeType = context.contentResolver.getType(uri)
    
    if (mimeType == null || !mimeType.startsWith("audio/")) {
        callback(AudioFileResult.Error("不支持的文件类型，请选择音频文件"))
        return
    }
    
    val extension = getFileExtension(context, uri).toLowerCase()
    if (!supportedFormats.contains(extension)) {
        callback(AudioFileResult.Error("不支持的音频格式：$extension\n支持的格式：${supportedFormats.joinToString(", ")}"))
        return
    }
    
    // 文件大小检查（最大500MB）
    val fileSize = getFileSize(context, uri)
    val maxSize = 500 * 1024 * 1024 // 500MB
    
    if (fileSize > maxSize) {
        callback(AudioFileResult.Error("文件大小超过限制（最大500MB），当前文件大小：${formatFileSize(fileSize)}"))
        return
    }
    
    // 复制文件到应用私有目录
    try {
        val fileName = getFileName(context, uri) ?: "audio_${System.currentTimeMillis()}.$extension"
        val outputFile = File(context.filesDir, "uploads/$fileName")
        outputFile.parentFile?.mkdirs()
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        callback(AudioFileResult.Success(fileName, outputFile.absolutePath))
    } catch (e: Exception) {
        callback(AudioFileResult.Error("文件处理失败：${e.message}"))
    }
}

/**
 * 获取文件扩展名
 */
private fun getFileExtension(context: android.content.Context, uri: Uri): String {
    return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val mimeType = context.contentResolver.getType(uri)
        mimeType?.split("/")?.last() ?: ""
    } else {
        File(uri.path).extension
    }
}

/**
 * 获取文件名
 */
private fun getFileName(context: android.content.Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
        }
    }
    return null
}

/**
 * 获取文件大小
 */
private fun getFileSize(context: android.content.Context, uri: Uri): Long {
    val projection = arrayOf(MediaStore.MediaColumns.SIZE)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
        }
    }
    return 0
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

@Composable
fun RealtimeRecordingContent(
    isRecording: Boolean,
    duration: Int,
    transcriptText: String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isRecording) "录音中..." else "准备录音",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.displayLarge
        )
        
        Button(
            onClick = if (isRecording) onStopRecording else onStartRecording,
            modifier = Modifier.size(120.dp),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        
        if (transcriptText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "转写内容",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = transcriptText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun UploadRecordingContent(
    onUpload: () -> Unit,
    transcriptText: String,
    isTranscribing: Boolean,
    selectedFileName: String?,
    onGenerateMinutes: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "上传录音文件",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "支持格式：MP3, WAV, AMR, AAC, M4A\n最大文件大小：500MB",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onUpload,
            enabled = !isTranscribing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileAudio, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isTranscribing) "转写中..." else "选择文件")
        }
        
        selectedFileName?.let {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    if (isTranscribing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        
        if (transcriptText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "转写内容",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = transcriptText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Button(
                onClick = onGenerateMinutes,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("生成会议纪要")
            }
        }
    }
}

@Composable
fun TextInputContent(
    transcriptText: String,
    onTextChange: (String) -> Unit,
    onGenerateMinutes: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "输入文本内容",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "直接输入或粘贴文本，系统将自动生成会议纪要",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = transcriptText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 200.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                decorationBox = { innerTextField ->
                    if (transcriptText.isEmpty()) {
                        Text(
                            text = "请输入或粘贴文本内容...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )
        }
        
        Button(
            onClick = onGenerateMinutes,
            enabled = transcriptText.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成会议纪要")
        }
    }
}

@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    onSave: (SaveOptions) -> Unit
) {
    var saveAudio by remember { mutableStateOf(true) }
    var saveTranscript by remember { mutableStateOf(true) }
    var saveMinutes by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存选项") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveAudio,
                        onCheckedChange = { saveAudio = it }
                    )
                    Text("保存录音文件")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveTranscript,
                        onCheckedChange = { saveTranscript = it }
                    )
                    Text("保存转写文字")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveMinutes,
                        onCheckedChange = { saveMinutes = it }
                    )
                    Text("保存会议纪要")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(SaveOptions(saveAudio, saveTranscript, saveMinutes))
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

data class SaveOptions(
    val saveAudio: Boolean,
    val saveTranscript: Boolean,
    val saveMinutes: Boolean
)

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

private fun startRecording(context: android.content.Context, viewModel: RecordViewModel) {
}

private fun stopRecording(context: android.content.Context, viewModel: RecordViewModel) {
}

private fun generateMeetingMinutes(text: String, callback: (String) -> Unit) {
    val minutes = """# 会议纪要

## 基本信息
- **时间**：${java.util.Date()}
- **类型**：自动生成

## 会议内容
$text

## 总结
会议已完成，以上是会议的主要内容。
"""
    callback(minutes)
}

private fun saveRecord(context: android.content.Context, viewModel: RecordViewModel, transcript: String, minutes: String) {
}

private fun saveRecordWithOptions(context: android.content.Context, viewModel: RecordViewModel, transcript: String, options: SaveOptions) {
}