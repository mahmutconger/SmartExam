package com.anlarsinsoftware.denecoz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anlarsinsoftware.denecoz.ui.theme.DeneCozTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeneCozTheme {
                AppNavigation()
            }
        }
    }
}