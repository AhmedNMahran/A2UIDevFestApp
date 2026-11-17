package com.example.a2uidevfest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.a2uidevfest.ui.DevFestMainScreen
import com.example.a2uidevfest.ui.theme.A2UIDevFestTheme
import com.example.a2uidevfest.viewmodel.DevFestA2uiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DevFestA2uiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A2UIDevFestTheme {
                DevFestMainScreen(viewModel = viewModel)
            }
        }
    }
}
