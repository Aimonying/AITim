package com.tingjizhushou.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tingjizhushou.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE role = 'admin' LIMIT 1")
    suspend fun getAdminUser(): UserEntity?
    
    @Query("SELECT * FROM users ORDER BY lastLoginAt DESC LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Insert
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun clearAll()
}