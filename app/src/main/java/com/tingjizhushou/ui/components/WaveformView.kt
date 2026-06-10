package com.tingjizhushou.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Audio waveform visualization component.
 * Displays real-time amplitude as animated bars.
 * 
 * @param amplitude Current audio amplitude (0-32767)
 * @param isRecording Whether currently recording
 * @param barCount Number of bars to display
 * @param barColor Color of the bars
 * @param modifier Modifier
 */
@Composable
fun WaveformView(
    amplitude: Int,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 40,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 100.dp
) {
    // Normalize amplitude to 0-1 range
    val normalizedAmplitude = min(amplitude / 32767f, 1f)
    
    // Smooth animation for amplitude changes
    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isRecording) normalizedAmplitude else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "amplitude"
    )
    
    // Generate bar heights based on amplitude with some variation
    val barHeights = remember(animatedAmplitude, barCount) {
        generateBarHeights(animatedAmplitude, barCount)
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val barWidth = size.width / (barCount * 2f)
        val maxBarHeight = size.height * 0.9f
        val centerY = size.height / 2
        
        barHeights.forEachIndexed { index, heightRatio ->
            val barHeight = maxBarHeight * heightRatio
            val x = index * barWidth * 2 + barWidth / 2
            
            // Draw bar from center (symmetric)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

/**
 * Generate bar heights with some variation for visual appeal.
 */
private fun generateBarHeights(amplitude: Float, barCount: Int): List<Float> {
    return List(barCount) { index ->
        // Create wave-like pattern
        val centerIndex = barCount / 2
        val distanceFromCenter = kotlin.math.abs(index - centerIndex) / centerIndex.toFloat()
        
        // Height decreases towards edges
        val edgeFalloff = 1f - (distanceFromCenter * 0.5f)
        
        // Add some randomness based on position
        val randomFactor = 0.7f + 0.3f * kotlin.math.sin(index * 0.5f)
        
        // Final height
        val height = amplitude * edgeFalloff * randomFactor
        
        // Minimum height when recording
        if (amplitude > 0) kotlin.math.max(height, 0.1f) else 0f
    }
}

/**
 * Simple waveform for display only (no animation).
 */
@Composable
fun StaticWaveformView(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 60.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (waveformData.isEmpty()) return@Canvas
        
        val barWidth = size.width / (waveformData.size * 2f)
        val maxBarHeight = size.height * 0.9f
        val centerY = size.height / 2
        
        waveformData.forEachIndexed { index, amplitude ->
            val barHeight = maxBarHeight * amplitude.coerceIn(0f, 1f)
            val x = index * barWidth * 2 + barWidth / 2
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

/**
 * Recording indicator with pulsing animation.
 */
@Composable
fun RecordingIndicator(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Red
) {
    val alpha by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(500),
        label = "indicatorAlpha"
    )
    
    Canvas(modifier = modifier) {
        if (alpha > 0) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = size.minDimension / 2
            )
        }
    }
}
