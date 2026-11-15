package com.ticktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ticktracker.ui.AppRoot
import com.ticktracker.ui.theme.TickTrackerTheme

/** The only activity. Every screen is a Compose destination inside [AppRoot]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TickTrackerTheme {
                AppRoot()
            }
        }
    }
}
