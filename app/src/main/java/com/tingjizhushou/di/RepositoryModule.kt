package com.tingjizhushou.di

import com.tingjizhushou.data.repository.RecordRepository
import com.tingjizhushou.data.repository.RecordRepositoryImpl
import com.tingjizhushou.data.repository.SummaryRepository
import com.tingjizhushou.data.repository.SummaryRepositoryImpl
import com.tingjizhushou.data.repository.TranscriptRepository
import com.tingjizhushou.data.repository.TranscriptRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository bindings.
 * Binds repository implementations to their interfaces for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Bind RecordRepositoryImpl to RecordRepository.
     */
    @Binds
    @Singleton
    abstract fun bindRecordRepository(
        recordRepositoryImpl: RecordRepositoryImpl
    ): RecordRepository
    
    /**
     * Bind TranscriptRepositoryImpl to TranscriptRepository.
     */
    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(
        transcriptRepositoryImpl: TranscriptRepositoryImpl
    ): TranscriptRepository
    
    /**
     * Bind SummaryRepositoryImpl to SummaryRepository.
     */
    @Binds
    @Singleton
    abstract fun bindSummaryRepository(
        summaryRepositoryImpl: SummaryRepositoryImpl
    ): SummaryRepository
}
