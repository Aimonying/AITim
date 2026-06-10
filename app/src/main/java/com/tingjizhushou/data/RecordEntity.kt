package com.tingjizhushou.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 记录实体类
 * 对应数据库中的records表
 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 记录标题
    val title: String,
    
    // 记录类型：REALTIME(实时录音), UPLOAD(上传录音), TEXT(文本输入)
    val type: String,
    
    // 创建时间
    val createdAt: Date,
    
    // 录音文件路径（可选）
    val audioPath: String? = null,
    
    // 录音文件大小（字节）
    val audioSize: Long = 0,
    
    // 录音时长（秒）
    val duration: Int = 0,
    
    // 转写文字内容
    val transcriptText: String? = null,
    
    // 会议纪要内容
    val meetingMinutes: String? = null,
    
    // 识别语言：zh-CN(普通话), en-US(英语)
    val language: String = "zh-CN",
    
    // 是否收藏
    val isFavorite: Boolean = false,
    
    // 标签（逗号分隔）
    val tags: String? = null,
    
    // 更新时间
    val updatedAt: Date = Date()
) {
    companion object {
        const val TYPE_REALTIME = "REALTIME"
        const val TYPE_UPLOAD = "UPLOAD"
        const val TYPE_TEXT = "TEXT"
        
        const val LANG_CHINESE = "zh-CN"
        const val LANG_ENGLISH = "en-US"
    }
}
