package com.tingjizhushou.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String?,
    val role: String,
    val nickname: String?,
    val createdAt: Long,
    val lastLoginAt: Long?
) {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
        
        fun createAdmin(id: String = "admin"): UserEntity {
            return UserEntity(
                id = id,
                phoneNumber = null,
                role = ROLE_ADMIN,
                nickname = "管理员",
                createdAt = Date().time,
                lastLoginAt = Date().time
            )
        }
        
        fun createUser(phoneNumber: String): UserEntity {
            return UserEntity(
                id = phoneNumber,
                phoneNumber = phoneNumber,
                role = ROLE_USER,
                nickname = null,
                createdAt = Date().time,
                lastLoginAt = Date().time
            )
        }
    }
    
    fun isAdmin(): Boolean {
        return role == ROLE_ADMIN
    }
    
    fun isUser(): Boolean {
        return role == ROLE_USER
    }
    
    fun getRoleText(): String {
        return when (role) {
            ROLE_ADMIN -> "管理员"
            ROLE_USER -> "普通用户"
            else -> "未知"
        }
    }
}