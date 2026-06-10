package com.tingjizhushou.ui.viewmodel

import com.tingjizhushou.data.model.SubscriptionStatus
import com.tingjizhushou.data.repository.SubscriptionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SubscriptionViewModelTest {

    private lateinit var mockRepository: SubscriptionRepository
    private lateinit var viewModel: SubscriptionViewModel

    @Before
    fun setup() {
        mockRepository = mockk()
        viewModel = SubscriptionViewModel(mockRepository)
    }

    @Test
    fun testPurchaseLightSubscription() = runTest {
        coEvery { mockRepository.purchaseLightSubscription() } returns Unit
        
        viewModel.purchaseLightSubscription()
        
        coVerify { mockRepository.purchaseLightSubscription() }
    }

    @Test
    fun testPurchaseProSubscription() = runTest {
        coEvery { mockRepository.purchaseProSubscription() } returns Unit
        
        viewModel.purchaseProSubscription()
        
        coVerify { mockRepository.purchaseProSubscription() }
    }

    @Test
    fun testPurchaseLifetimeSubscription() = runTest {
        coEvery { mockRepository.purchaseLifetimeSubscription() } returns Unit
        
        viewModel.purchaseLifetimeSubscription()
        
        coVerify { mockRepository.purchaseLifetimeSubscription() }
    }

    @Test
    fun testEnableDeveloperMode() = runTest {
        coEvery { mockRepository.enableDeveloperMode() } returns Unit
        
        viewModel.enableDeveloperMode()
        
        coVerify { mockRepository.enableDeveloperMode() }
    }

    @Test
    fun testConsumeFreeUse() = runTest {
        coEvery { mockRepository.consumeFreeUse() } returns Unit
        
        viewModel.consumeFreeUse()
        
        coVerify { mockRepository.consumeFreeUse() }
    }

    @Test
    fun testCanUseService() = runTest {
        val status = SubscriptionStatus.createDefault()
        coEvery { mockRepository.getSubscriptionStatus() } returns flowOf(status)
        
        assertTrue(viewModel.canUseService())
    }

    @Test
    fun testGetRemainingUses() = runTest {
        val status = SubscriptionStatus.createDefault()
        coEvery { mockRepository.getSubscriptionStatus() } returns flowOf(status)
        
        assertEquals(10, viewModel.getRemainingUses())
    }
}