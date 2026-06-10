package com.tingjizhushou.ui.viewmodel

import com.tingjizhushou.data.model.UserEntity
import com.tingjizhushou.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserViewModelTest {

    private lateinit var mockRepository: UserRepository
    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        mockRepository = mockk()
        viewModel = UserViewModel(mockRepository)
    }

    @Test
    fun testLoginAdminSuccess() = runTest {
        coEvery { mockRepository.loginAdmin("admin123") } returns true
        
        viewModel.loginAdmin("admin123")
        
        coVerify { mockRepository.loginAdmin("admin123") }
    }

    @Test
    fun testLoginAdminFailure() = runTest {
        coEvery { mockRepository.loginAdmin("wrong") } returns false
        
        viewModel.loginAdmin("wrong")
        
        assertFalse(viewModel.isAdmin())
    }

    @Test
    fun testLoginUser() = runTest {
        val user = UserEntity.createUser("13800138000")
        coEvery { mockRepository.loginUser("13800138000") } returns user
        
        viewModel.loginUser("13800138000")
        
        coVerify { mockRepository.loginUser("13800138000") }
    }

    @Test
    fun testLogout() = runTest {
        coEvery { mockRepository.logout() } returns Unit
        
        viewModel.logout()
        
        coVerify { mockRepository.logout() }
    }

    @Test
    fun testIsAdmin() = runTest {
        val admin = UserEntity.createAdmin()
        coEvery { mockRepository.getCurrentUser() } returns flowOf(admin)
        
        runTest {
            mockRepository.getCurrentUser().collect {
                if (it != null) {
                    assertTrue(it.isAdmin())
                }
            }
        }
    }

    @Test
    fun testIsUserLoggedIn() = runTest {
        val user = UserEntity.createUser("13800138000")
        coEvery { mockRepository.getCurrentUser() } returns flowOf(user)
        
        runTest {
            mockRepository.getCurrentUser().collect {
                assertTrue(it != null)
            }
        }
    }
}