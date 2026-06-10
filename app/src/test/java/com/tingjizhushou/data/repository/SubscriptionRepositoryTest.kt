package com.tingjizhushou.data.repository

import com.tingjizhushou.data.dao.SubscriptionDao
import com.tingjizhushou.data.model.SubscriptionStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SubscriptionRepositoryTest {

    private val mockSubscriptionDao: SubscriptionDao = mockk()
    private val repository = SubscriptionRepository(mockSubscriptionDao)

    @Test
    fun testGetSubscriptionStatus() = runTest {
        val status = SubscriptionStatus.createDefault()
        coEvery { mockSubscriptionDao.getSubscriptionStatus() } returns flowOf(status)
        
        val result = repository.getSubscriptionStatusSync()
        
        assertEquals(status.subscriptionType, result.subscriptionType)
        assertEquals(status.remainingFreeUses, result.remainingFreeUses)
    }

    @Test
    fun testConsumeFreeUse() = runTest {
        val status = SubscriptionStatus.createDefault()
        coEvery { mockSubscriptionDao.getSubscriptionStatusSync() } returns status
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.consumeFreeUse()
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testPurchaseLightSubscription() = runTest {
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.purchaseLightSubscription()
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testPurchaseProSubscription() = runTest {
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.purchaseProSubscription()
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testPurchaseLifetimeSubscription() = runTest {
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.purchaseLifetimeSubscription()
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testEnableDeveloperMode() = runTest {
        val status = SubscriptionStatus.createDefault()
        coEvery { mockSubscriptionDao.getSubscriptionStatusSync() } returns status
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.enableDeveloperMode()
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testRestorePurchase() = runTest {
        coEvery { mockSubscriptionDao.updateSubscriptionStatus(any()) } returns Unit
        
        repository.restorePurchase(SubscriptionStatus.TYPE_PRO)
        
        coVerify { mockSubscriptionDao.updateSubscriptionStatus(any()) }
    }

    @Test
    fun testInitializeDefaultStatus() = runTest {
        coEvery { mockSubscriptionDao.getSubscriptionStatusSync() } returns null
        coEvery { mockSubscriptionDao.insertSubscriptionStatus(any()) } returns Unit
        
        repository.initializeDefaultStatus()
        
        coVerify { mockSubscriptionDao.insertSubscriptionStatus(any()) }
    }
}