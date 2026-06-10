package com.tingjizhushou.data.repository

import com.tingjizhushou.data.dao.SubscriptionDao
import com.tingjizhushou.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    
    fun getSubscriptionStatus(): Flow<SubscriptionStatus?> {
        return subscriptionDao.getSubscriptionStatus()
    }
    
    suspend fun getSubscriptionStatusSync(): SubscriptionStatus {
        return subscriptionDao.getSubscriptionStatusSync() 
            ?: SubscriptionStatus.createDefault().also {
                subscriptionDao.insertSubscriptionStatus(it)
            }
    }
    
    suspend fun initializeDefaultStatus() {
        val existing = subscriptionDao.getSubscriptionStatusSync()
        if (existing == null) {
            subscriptionDao.insertSubscriptionStatus(SubscriptionStatus.createDefault())
        }
    }
    
    suspend fun consumeFreeUse() {
        val status = getSubscriptionStatusSync()
        if (status.subscriptionType == SubscriptionStatus.TYPE_FREE && status.remainingFreeUses > 0) {
            val updated = status.copy(remainingFreeUses = status.remainingFreeUses - 1)
            subscriptionDao.updateSubscriptionStatus(updated)
        }
    }
    
    suspend fun consumeMinutes(minutes: Int) {
        val status = getSubscriptionStatusSync()
        when (status.subscriptionType) {
            SubscriptionStatus.TYPE_LIGHT -> {
                val newRemaining = Math.max(0, status.remainingMinutes - minutes)
                val updated = status.copy(remainingMinutes = newRemaining)
                subscriptionDao.updateSubscriptionStatus(updated)
            }
            SubscriptionStatus.TYPE_PRO -> {
                // 专业版无限时长，无需扣除
            }
            SubscriptionStatus.TYPE_LIFETIME -> {
                // 永久版无限时长，无需扣除
            }
        }
    }
    
    suspend fun purchaseLightSubscription() {
        val newStatus = SubscriptionStatus.createLightSubscription()
        subscriptionDao.updateSubscriptionStatus(newStatus)
    }
    
    suspend fun purchaseProSubscription() {
        val newStatus = SubscriptionStatus.createProSubscription()
        subscriptionDao.updateSubscriptionStatus(newStatus)
    }
    
    suspend fun purchaseLifetimeSubscription() {
        val newStatus = SubscriptionStatus.createLifetimeSubscription()
        subscriptionDao.updateSubscriptionStatus(newStatus)
    }
    
    suspend fun restorePurchase(subscriptionType: String) {
        val newStatus = when (subscriptionType) {
            SubscriptionStatus.TYPE_LIGHT -> SubscriptionStatus.createLightSubscription()
            SubscriptionStatus.TYPE_PRO -> SubscriptionStatus.createProSubscription()
            SubscriptionStatus.TYPE_LIFETIME -> SubscriptionStatus.createLifetimeSubscription()
            else -> SubscriptionStatus.createDefault()
        }
        subscriptionDao.updateSubscriptionStatus(newStatus)
    }
    
    suspend fun resetFreeTrial() {
        val defaultStatus = SubscriptionStatus.createDefault()
        subscriptionDao.updateSubscriptionStatus(defaultStatus)
    }
    
    suspend fun enableDeveloperMode() {
        val status = getSubscriptionStatusSync()
        val updated = status.copy(isDeveloperMode = true)
        subscriptionDao.updateSubscriptionStatus(updated)
    }
    
    suspend fun disableDeveloperMode() {
        val status = getSubscriptionStatusSync()
        val updated = status.copy(isDeveloperMode = false)
        subscriptionDao.updateSubscriptionStatus(updated)
    }
}