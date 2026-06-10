package com.tingjizhushou.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tingjizhushou.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscription_status WHERE id = 'default'")
    fun getSubscriptionStatus(): Flow<SubscriptionStatus?>
    
    @Query("SELECT * FROM subscription_status WHERE id = 'default'")
    suspend fun getSubscriptionStatusSync(): SubscriptionStatus?
    
    @Insert
    suspend fun insertSubscriptionStatus(status: SubscriptionStatus)
    
    @Update
    suspend fun updateSubscriptionStatus(status: SubscriptionStatus)
    
    @Query("DELETE FROM subscription_status")
    suspend fun clearAll()
}