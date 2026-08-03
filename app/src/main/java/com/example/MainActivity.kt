package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SpeedPaintMainScreen
import com.example.ui.SpeedPaintViewModel
import com.example.ui.theme.SpeedPaintTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedPaintTheme(darkTheme = true) {
                val vm: SpeedPaintViewModel = viewModel()
                SpeedPaintMainScreen(viewModel = vm)
            }
        }
    }
}

