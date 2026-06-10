package com.tingjizhushou.di

import android.app.Application
import com.tingjizhushou.service.AudioRecordService
import com.tingjizhushou.service.SpeechRecognizeService
import com.tingjizhushou.service.SummaryGeneratorService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing service dependencies.
 * Provides singleton instances of all services.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    /**
     * Provide AudioRecordService instance.
     * Manages audio recording with MediaRecorder.
     * @param application Application context
     * @return AudioRecordService instance
     */
    @Provides
    @Singleton
    fun provideAudioRecordService(application: Application): AudioRecordService {
        return AudioRecordService(application)
    }
    
    /**
     * Provide SpeechRecognizeService instance.
     * Handles speech-to-text recognition.
     * @param application Application context
     * @return SpeechRecognizeService instance
     */
    @Provides
    @Singleton
    fun provideSpeechRecognizeService(application: Application): SpeechRecognizeService {
        return SpeechRecognizeService(application)
    }
    
    /**
     * Provide SummaryGeneratorService instance.
     * Generates meeting summaries from transcription text.
     * @return SummaryGeneratorService instance
     */
    @Provides
    @Singleton
    fun provideSummaryGeneratorService(): SummaryGeneratorService {
        return SummaryGeneratorService()
    }
}
