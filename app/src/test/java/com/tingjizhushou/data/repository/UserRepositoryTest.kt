package com.tingjizhushou.data.repository

import com.tingjizhushou.data.dao.UserDao
import com.tingjizhushou.data.model.UserEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UserRepositoryTest {

    private val mockUserDao: UserDao = mockk()
    private val repository = UserRepository(mockUserDao)

    @Test
    fun testLoginAdminSuccess() = runTest {
        coEvery { mockUserDao.getAdminUser() } returns null
        coEvery { mockUserDao.insertUser(any()) } returns Unit
        
        val result = repository.loginAdmin("admin123")
        
        assertTrue(result)
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun testLoginAdminFailure() = runTest {
        val result = repository.loginAdmin("wrongpassword")
        
        assertFalse(result)
        coVerify(exactly = 0) { mockUserDao.insertUser(any()) }
    }

    @Test
    fun testLoginUserNew() = runTest {
        coEvery { mockUserDao.getUserByPhone("13800138000") } returns null
        coEvery { mockUserDao.insertUser(any()) } returns Unit
        
        val user = repository.loginUser("13800138000")
        
        assertEquals("13800138000", user.id)
        assertEquals("13800138000", user.phoneNumber)
        assertEquals(UserEntity.ROLE_USER, user.role)
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun testLoginUserExisting() = runTest {
        val existingUser = UserEntity.createUser("13800138000")
        coEvery { mockUserDao.getUserByPhone("13800138000") } returns existingUser
        coEvery { mockUserDao.updateUser(any()) } returns Unit
        
        val user = repository.loginUser("13800138000")
        
        assertEquals("13800138000", user.id)
        coVerify { mockUserDao.updateUser(any()) }
    }

    @Test
    fun testIsAdminLoggedIn() = runTest {
        val admin = UserEntity.createAdmin()
        coEvery { mockUserDao.getCurrentUser() } returns flowOf(admin)
        
        val result = repository.isAdminLoggedIn()
        
        assertTrue(result)
    }

    @Test
    fun testIsUserLoggedIn() = runTest {
        val user = UserEntity.createUser("13800138000")
        coEvery { mockUserDao.getCurrentUser() } returns flowOf(user)
        
        val result = repository.isUserLoggedIn()
        
        assertTrue(result)
    }

    @Test
    fun testNotLoggedIn() = runTest {
        coEvery { mockUserDao.getCurrentUser() } returns flowOf(null)
        
        assertFalse(repository.isAdminLoggedIn())
        assertFalse(repository.isUserLoggedIn())
        assertEquals(UserEntity.ROLE_USER, repository.getCurrentRole())
    }
}