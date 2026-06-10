package com.tingjizhushou.data.repository

import com.tingjizhushou.data.dao.UserDao
import com.tingjizhushou.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class UserRepository(private val userDao: UserDao) {
    
    private val ADMIN_PASSWORD = "admin123"
    
    fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getCurrentUser()
    }
    
    suspend fun getCurrentUserSync(): UserEntity? {
        return userDao.getCurrentUser().first()
    }
    
    suspend fun loginAdmin(password: String): Boolean {
        if (password == ADMIN_PASSWORD) {
            val existingAdmin = userDao.getAdminUser()
            val adminUser = if (existingAdmin != null) {
                existingAdmin.copy(lastLoginAt = Date().time)
            } else {
                UserEntity.createAdmin()
            }
            userDao.insertUser(adminUser)
            return true
        }
        return false
    }
    
    suspend fun loginUser(phoneNumber: String): UserEntity {
        var user = userDao.getUserByPhone(phoneNumber)
        if (user == null) {
            user = UserEntity.createUser(phoneNumber)
            userDao.insertUser(user)
        } else {
            user = user.copy(lastLoginAt = Date().time)
            userDao.updateUser(user)
        }
        return user
    }
    
    suspend fun logout() {
        val currentUser = getCurrentUserSync()
        if (currentUser != null) {
            val updatedUser = currentUser.copy(lastLoginAt = null)
            userDao.updateUser(updatedUser)
        }
    }
    
    suspend fun isAdminLoggedIn(): Boolean {
        val user = getCurrentUserSync()
        return user?.isAdmin() ?: false
    }
    
    suspend fun isUserLoggedIn(): Boolean {
        val user = getCurrentUserSync()
        return user?.isUser() ?: false
    }
    
    suspend fun getCurrentRole(): String {
        val user = getCurrentUserSync()
        return user?.role ?: UserEntity.ROLE_USER
    }
}