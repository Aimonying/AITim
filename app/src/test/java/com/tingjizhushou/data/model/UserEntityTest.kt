package com.tingjizhushou.data.model

import org.junit.Assert.*
import org.junit.Test

class UserEntityTest {

    @Test
    fun testAdminRole() {
        val admin = UserEntity.createAdmin()
        
        assertEquals("admin", admin.id)
        assertNull(admin.phoneNumber)
        assertEquals(UserEntity.ROLE_ADMIN, admin.role)
        assertEquals("管理员", admin.nickname)
        assertTrue(admin.isAdmin())
        assertFalse(admin.isUser())
        assertEquals("管理员", admin.getRoleText())
    }

    @Test
    fun testUserRole() {
        val user = UserEntity.createUser("13800138000")
        
        assertEquals("13800138000", user.id)
        assertEquals("13800138000", user.phoneNumber)
        assertEquals(UserEntity.ROLE_USER, user.role)
        assertNull(user.nickname)
        assertFalse(user.isAdmin())
        assertTrue(user.isUser())
        assertEquals("普通用户", user.getRoleText())
    }

    @Test
    fun testUnknownRole() {
        val unknownUser = UserEntity(
            id = "test",
            phoneNumber = null,
            role = "unknown",
            nickname = null,
            createdAt = 0,
            lastLoginAt = null
        )
        
        assertEquals("未知", unknownUser.getRoleText())
        assertFalse(unknownUser.isAdmin())
        assertFalse(unknownUser.isUser())
    }
}