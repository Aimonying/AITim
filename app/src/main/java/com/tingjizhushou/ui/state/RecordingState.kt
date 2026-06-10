package com.tingjizhushou.ui.state

/**
 * 录音状态数据类
 * 用于UI层观察录音服务的状态变化
 */
data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val duration: Long = 0L,          // 录音时长（秒）
    val fileSize: Long = 0L,          // 文件大小（字节）
    val amplitude: Int = 0,          // 当前振幅
    val audioFilePath: String? = null, // 音频文件路径
    val error: String? = null,        // 错误信息
    val language: String = "zh-CN"    // 当前语言
) {
    /**
     * 获取格式化的时长字符串
     */
    fun getFormattedDuration(): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * 获取格式化的文件大小字符串
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> String.format("%.1f KB", fileSize / 1024.0)
            else -> String.format("%.1f MB", fileSize / (1024.0 * 1024.0))
        }
    }
    
    /**
     * 检查是否达到500MB限制
     */
    fun isSizeLimitReached(): Boolean {
        return fileSize >= 500 * 1024 * 1024
    }
    
    /**
     * 获取振幅级别（0.0-1.0）
     */
    fun getAmplitudeLevel(): Float {
        return (amplitude.toFloat() / 32767f).coerceIn(0f, 1f)
    }
}

/**
 * 录音状态枚举
 */
enum class RecordingStatus {
    IDLE,      // 空闲状态
    RECORDING, // 录音中
    PAUSED,    // 暂停
    COMPLETED, // 完成
    ERROR      // 错误
}
