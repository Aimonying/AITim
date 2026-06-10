package com.tingjizhushou.ui.state

/**
 * 转写状态数据类
 * 用于UI层观察语音识别服务的状态变化
 */
data class TranscribeState(
    val isTranscribing: Boolean = false,
    val partialResult: String = "",    // 部分识别结果（实时）
    val finalResult: String = "",      // 最终识别结果
    val progress: Float = 0f,         // 转写进度（0.0-1.0）
    val confidence: Float = 0f,      // 识别置信度（0.0-1.0）
    val language: String = "zh-CN",   // 当前语言
    val engine: String = "ONLINE",    // 识别引擎 ONLINE/OFFLINE
    val error: String? = null         // 错误信息
) {
    /**
     * 获取当前显示的文本（优先显示最终结果）
     */
    fun getDisplayText(): String {
        return finalResult.ifEmpty { partialResult }
    }
    
    /**
     * 检查是否有文本
     */
    fun hasText(): Boolean {
        return finalResult.isNotEmpty() || partialResult.isNotEmpty()
    }
    
    /**
     * 获取进度百分比
     */
    fun getProgressPercent(): Int {
        return (progress * 100).toInt()
    }
    
    /**
     * 检查是否正在进行转写
     */
    fun isWorking(): Boolean {
        return isTranscribing || hasText()
    }
    
    /**
     * 获取识别引擎名称
     */
    fun getEngineName(): String {
        return when (engine) {
            "ONLINE" -> "在线识别"
            "OFFLINE" -> "离线识别"
            else -> engine
        }
    }
}

/**
 * 语音识别引擎类型
 */
enum class SpeechEngine {
    ONLINE,   // 在线识别（Android SpeechRecognizer）
    OFFLINE   // 离线识别（Vosk等）
}

/**
 * 识别语言类型
 */
enum class RecognitionLanguage(val code: String, val displayName: String) {
    ZH_CN("zh-CN", "普通话"),
    EN_US("en-US", "英语")
}
