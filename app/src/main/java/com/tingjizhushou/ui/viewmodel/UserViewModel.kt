package com.tingjizhushou.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.model.UserEntity
import com.tingjizhushou.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collectLatest { user ->
                _currentUser.value = user
            }
        }
    }
    
    fun loginAdmin(password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.loginAdmin(password)
                if (success) {
                    _loginSuccess.value = true
                    clearLoginSuccess()
                } else {
                    _errorMessage.value = "密码错误"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loginUser(phoneNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.loginUser(phoneNumber)
                _loginSuccess.value = true
                clearLoginSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
    
    fun clearLoginSuccess() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _loginSuccess.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun isAdmin(): Boolean {
        return _currentUser.value?.isAdmin() ?: false
    }
    
    fun isUserLoggedIn(): Boolean {
        return _currentUser.value != null
    }
    
    fun getCurrentRole(): String {
        return _currentUser.value?.role ?: UserEntity.ROLE_USER
    }
}