package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SpeedPaintMainScreen
import com.example.ui.SpeedPaintViewModel
import com.example.ui.theme.SpeedPaintTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SpeedPaintViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedPaintTheme(darkTheme = true) {
                SpeedPaintMainScreen(viewModel = viewModel)
            }
        }
    }
}

