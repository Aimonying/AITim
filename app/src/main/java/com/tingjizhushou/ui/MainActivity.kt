package com.tingjizhushou.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tingjizhushou.ui.navigation.TingJiNavHost
import com.tingjizhushou.ui.theme.TingJiZhuShouTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the TingJiZhuShou application.
 * Uses Jetpack Compose for UI and Hilt for dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TingJiZhuShouTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TingJiNavHost()
                }
            }
        }
    }
}
