package com.tingjizhushou.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "subscription_status")
data class SubscriptionStatus(
    @PrimaryKey val id: String = "default",
    val subscriptionType: String,
    val remainingFreeUses: Int,
    val totalFreeUses: Int,
    val monthlyMinutes: Int,
    val remainingMinutes: Int,
    val expiresAt: Long?,
    val purchaseDate: Long,
    val isPurchased: Boolean,
    val isDeveloperMode: Boolean = false
) {
    companion object {
        const val TYPE_FREE = "free"
        const val TYPE_LIGHT = "light"
        const val TYPE_PRO = "pro"
        const val TYPE_LIFETIME = "lifetime"
        
        const val DEFAULT_FREE_USES = 10
        
        fun createDefault(): SubscriptionStatus {
            return SubscriptionStatus(
                subscriptionType = TYPE_FREE,
                remainingFreeUses = DEFAULT_FREE_USES,
                totalFreeUses = DEFAULT_FREE_USES,
                monthlyMinutes = 0,
                remainingMinutes = 0,
                expiresAt = null,
                purchaseDate = Date().time,
                isPurchased = false
            )
        }
        
        fun createLightSubscription(): SubscriptionStatus {
            return SubscriptionStatus(
                subscriptionType = TYPE_LIGHT,
                remainingFreeUses = 0,
                totalFreeUses = DEFAULT_FREE_USES,
                monthlyMinutes = 100,
                remainingMinutes = 100,
                expiresAt = Date().time + 30L * 24 * 60 * 60 * 1000,
                purchaseDate = Date().time,
                isPurchased = true
            )
        }
        
        fun createProSubscription(): SubscriptionStatus {
            return SubscriptionStatus(
                subscriptionType = TYPE_PRO,
                remainingFreeUses = 0,
                totalFreeUses = DEFAULT_FREE_USES,
                monthlyMinutes = Int.MAX_VALUE,
                remainingMinutes = Int.MAX_VALUE,
                expiresAt = Date().time + 30L * 24 * 60 * 60 * 1000,
                purchaseDate = Date().time,
                isPurchased = true
            )
        }
        
        fun createLifetimeSubscription(): SubscriptionStatus {
            return SubscriptionStatus(
                subscriptionType = TYPE_LIFETIME,
                remainingFreeUses = 0,
                totalFreeUses = DEFAULT_FREE_USES,
                monthlyMinutes = Int.MAX_VALUE,
                remainingMinutes = Int.MAX_VALUE,
                expiresAt = null,
                purchaseDate = Date().time,
                isPurchased = true
            )
        }
    }
    
    fun canUseService(): Boolean {
        if (isDeveloperMode) return true
        return when (subscriptionType) {
            TYPE_FREE -> remainingFreeUses > 0
            TYPE_LIGHT -> remainingMinutes > 0 && !isExpired()
            TYPE_PRO -> !isExpired()
            TYPE_LIFETIME -> true
            else -> false
        }
    }
    
    fun isExpired(): Boolean {
        return expiresAt != null && expiresAt < Date().time
    }
    
    fun getStatusText(): String {
        if (isDeveloperMode) return "开发者模式"
        return when (subscriptionType) {
            TYPE_FREE -> "免费体验"
            TYPE_LIGHT -> "轻量版"
            TYPE_PRO -> "专业版"
            TYPE_LIFETIME -> "永久版"
            else -> "未知"
        }
    }
    
    fun getUsageText(): String {
        if (isDeveloperMode) return "所有功能无限使用"
        return when (subscriptionType) {
            TYPE_FREE -> "剩余 $remainingFreeUses/$totalFreeUses 次"
            TYPE_LIGHT -> if (isExpired()) "已过期" else "剩余 $remainingMinutes 分钟"
            TYPE_PRO -> if (isExpired()) "已过期" else "无限时长"
            TYPE_LIFETIME -> "永久使用"
            else -> ""
        }
    }
}