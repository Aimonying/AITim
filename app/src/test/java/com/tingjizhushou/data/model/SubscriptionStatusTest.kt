package com.tingjizhushou.data.model

import org.junit.Assert.*
import org.junit.Test

class SubscriptionStatusTest {

    @Test
    fun testFreeSubscription() {
        val free = SubscriptionStatus.createDefault()
        
        assertEquals(SubscriptionStatus.TYPE_FREE, free.subscriptionType)
        assertEquals(10, free.remainingFreeUses)
        assertEquals(10, free.totalFreeUses)
        assertEquals(0, free.monthlyMinutes)
        assertEquals(0, free.remainingMinutes)
        assertNull(free.expiresAt)
        assertFalse(free.isPurchased)
        assertFalse(free.isDeveloperMode)
        assertEquals("免费体验", free.getStatusText())
        assertEquals("剩余 10/10 次", free.getUsageText())
        assertTrue(free.canUseService())
    }

    @Test
    fun testLightSubscription() {
        val light = SubscriptionStatus.createLightSubscription()
        
        assertEquals(SubscriptionStatus.TYPE_LIGHT, light.subscriptionType)
        assertEquals(0, light.remainingFreeUses)
        assertEquals(10, light.totalFreeUses)
        assertEquals(100, light.monthlyMinutes)
        assertEquals(100, light.remainingMinutes)
        assertNotNull(light.expiresAt)
        assertTrue(light.isPurchased)
        assertFalse(light.isDeveloperMode)
        assertEquals("轻量版", light.getStatusText())
        assertEquals("剩余 100 分钟", light.getUsageText())
        assertTrue(light.canUseService())
    }

    @Test
    fun testProSubscription() {
        val pro = SubscriptionStatus.createProSubscription()
        
        assertEquals(SubscriptionStatus.TYPE_PRO, pro.subscriptionType)
        assertEquals(Int.MAX_VALUE, pro.monthlyMinutes)
        assertEquals(Int.MAX_VALUE, pro.remainingMinutes)
        assertTrue(pro.isPurchased)
        assertEquals("专业版", pro.getStatusText())
        assertEquals("无限时长", pro.getUsageText())
        assertTrue(pro.canUseService())
    }

    @Test
    fun testLifetimeSubscription() {
        val lifetime = SubscriptionStatus.createLifetimeSubscription()
        
        assertEquals(SubscriptionStatus.TYPE_LIFETIME, lifetime.subscriptionType)
        assertNull(lifetime.expiresAt)
        assertTrue(lifetime.isPurchased)
        assertEquals("永久版", lifetime.getStatusText())
        assertEquals("永久使用", lifetime.getUsageText())
        assertTrue(lifetime.canUseService())
    }

    @Test
    fun testDeveloperMode() {
        val developer = SubscriptionStatus.createDefault().copy(isDeveloperMode = true)
        
        assertTrue(developer.isDeveloperMode)
        assertEquals("开发者模式", developer.getStatusText())
        assertEquals("所有功能无限使用", developer.getUsageText())
        assertTrue(developer.canUseService())
    }

    @Test
    fun testFreeUsesDepletion() {
        val free = SubscriptionStatus.createDefault()
        assertTrue(free.canUseService())
        
        val depleted = free.copy(remainingFreeUses = 0)
        assertFalse(depleted.canUseService())
        assertEquals("剩余 0/10 次", depleted.getUsageText())
    }

    @Test
    fun testExpiredSubscription() {
        val expired = SubscriptionStatus.createLightSubscription().copy(
            expiresAt = System.currentTimeMillis() - 1000
        )
        
        assertTrue(expired.isExpired())
        assertEquals("已过期", expired.getUsageText())
        assertFalse(expired.canUseService())
    }
}