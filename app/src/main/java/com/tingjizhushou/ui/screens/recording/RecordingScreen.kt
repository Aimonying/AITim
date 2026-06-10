package com.tingjizhushou.ui.screens.recording

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tingjizhushou.R
import com.tingjizhushou.ui.components.RecordButton
import com.tingjizhushou.ui.components.RecordButtonState
import com.tingjizhushou.ui.components.WaveformView
import com.tingjizhushou.ui.theme.PausedOrange
import com.tingjizhushou.ui.theme.RecordingRed
import kotlinx.coroutines.launch

/**
 * Recording screen composable with full functionality.
 * Provides UI for starting, pausing, and stopping audio recording with real-time feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = hiltViewModel(),
    onRecordingComplete: (Long) -> Unit = {}
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val transcribeState by viewModel.transcribeState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val showSizeWarning by viewModel.showSizeWarning.collectAsState()
    
    var hasPermissions by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.RECORD_AUDIO] == true
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.RECORD_AUDIO)
        )
    }
    
    // Determine button state
    val buttonState = when {
        !recordingState.isRecording -> RecordButtonState.IDLE
        recordingState.isPaused -> RecordButtonState.PAUSED
        else -> RecordButtonState.RECORDING
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            recordingState.isActive -> RecordingRed.copy(alpha = 0.1f)
            recordingState.isPaused -> PausedOrange.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_recording),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Language selector
                    Box {
                        TextButton(onClick = { showLanguageMenu = true }) {
                            Text(
                                text = when (selectedLanguage) {
                                    "zh-CN" -> stringResource(R.string.chinese)
                                    "en-US" -> stringResource(R.string.english)
                                    else -> selectedLanguage
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.chinese)) },
                                onClick = {
                                    viewModel.setLanguage("zh-CN")
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.english)) },
                                onClick = {
                                    viewModel.setLanguage("en-US")
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // File size warning
            if (showSizeWarning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = if (recordingState.isSizeLimitExceeded()) {
                            stringResource(R.string.file_size_limit_reached)
                        } else {
                            stringResource(R.string.file_size_warning)
                        },
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration display
            Text(
                text = recordingState.getFormattedDuration(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    recordingState.isActive -> RecordingRed
                    recordingState.isPaused -> PausedOrange
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recording status text
            Text(
                text = when {
                    recordingState.isActive -> stringResource(R.string.recording_in_progress)
                    recordingState.isPaused -> stringResource(R.string.recording_paused)
                    else -> ""
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // File size display
            if (recordingState.isRecording) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.file_size),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = recordingState.getFormattedFileSize(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = if (selectedLanguage == "zh-CN") 
                                    stringResource(R.string.chinese) 
                                else 
                                    stringResource(R.string.english),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Waveform visualization
            WaveformView(
                amplitude = recordingState.amplitude,
                isRecording = recordingState.isActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 32.dp),
                barCount = 50,
                barColor = if (recordingState.isActive) RecordingRed else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transcription preview
            if (transcribeState.hasResult || transcribeState.isTranscribing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.transcribing),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = transcribeState.getDisplayText().take(500),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 6
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button (visible when recording)
                if (recordingState.isRecording) {
                    OutlinedButton(
                        onClick = { viewModel.cancelRecording() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.cancel),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                // Stop button (visible when recording)
                if (recordingState.isRecording) {
                    FilledIconButton(
                        onClick = {
                            coroutineScope.launch {
                                val recordId = viewModel.stopRecording()
                                if (recordId != null) {
                                    onRecordingComplete(recordId)
                                }
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = stringResource(R.string.stop_recording),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Main record/pause button
                RecordButton(
                    state = buttonState,
                    onClick = {
                        when (buttonState) {
                            RecordButtonState.IDLE -> viewModel.startRecording()
                            RecordButtonState.RECORDING -> viewModel.pauseRecording()
                            RecordButtonState.PAUSED -> viewModel.resumeRecording()
                        }
                    },
                    size = 96.dp,
                    enabled = hasPermissions
                )
            }
            
            // Hint text
            Text(
                text = when (buttonState) {
                    RecordButtonState.IDLE -> stringResource(R.string.start_recording_hint)
                    RecordButtonState.RECORDING -> stringResource(R.string.pause_recording)
                    RecordButtonState.PAUSED -> stringResource(R.string.resume_recording)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
