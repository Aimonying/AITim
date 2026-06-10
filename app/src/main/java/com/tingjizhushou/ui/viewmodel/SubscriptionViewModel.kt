package com.tingjizhushou.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.model.SubscriptionStatus
import com.tingjizhushou.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubscriptionViewModel(private val subscriptionRepository: SubscriptionRepository) : ViewModel() {
    
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.createDefault())
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadSubscriptionStatus()
    }
    
    private fun loadSubscriptionStatus() {
        viewModelScope.launch {
            subscriptionRepository.getSubscriptionStatus().collectLatest { status ->
                _subscriptionStatus.value = status ?: SubscriptionStatus.createDefault()
            }
        }
    }
    
    fun consumeFreeUse() {
        viewModelScope.launch {
            subscriptionRepository.consumeFreeUse()
        }
    }
    
    fun consumeMinutes(minutes: Int) {
        viewModelScope.launch {
            subscriptionRepository.consumeMinutes(minutes)
        }
    }
    
    fun purchaseLightSubscription() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                subscriptionRepository.purchaseLightSubscription()
                _purchaseSuccess.value = true
                clearPurchaseSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun purchaseProSubscription() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                subscriptionRepository.purchaseProSubscription()
                _purchaseSuccess.value = true
                clearPurchaseSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun purchaseLifetimeSubscription() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                subscriptionRepository.purchaseLifetimeSubscription()
                _purchaseSuccess.value = true
                clearPurchaseSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun restorePurchase(subscriptionType: String) {
        viewModelScope.launch {
            subscriptionRepository.restorePurchase(subscriptionType)
        }
    }
    
    fun clearPurchaseSuccess() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _purchaseSuccess.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun canUseService(): Boolean {
        return _subscriptionStatus.value.canUseService()
    }
    
    fun getRemainingUses(): Int {
        return _subscriptionStatus.value.remainingFreeUses
    }
    
    fun getSubscriptionType(): String {
        return _subscriptionStatus.value.subscriptionType
    }
    
    fun enableDeveloperMode() {
        viewModelScope.launch {
            subscriptionRepository.enableDeveloperMode()
        }
    }
    
    fun disableDeveloperMode() {
        viewModelScope.launch {
            subscriptionRepository.disableDeveloperMode()
        }
    }
    
    fun isDeveloperMode(): Boolean {
        return _subscriptionStatus.value.isDeveloperMode
    }
}