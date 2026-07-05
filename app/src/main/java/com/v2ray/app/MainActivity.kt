package com.v2ray.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.v2ray.app.navigation.AppNavigation
import com.v2ray.app.repository.ProfileRepository
import com.v2ray.app.ui.theme.V2rayAppTheme
import com.v2ray.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val repository = ProfileRepository()
    private val viewModel by lazy { MainViewModel(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            V2rayAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    AppNavigation(
        navController = navController,
        viewModel = viewModel
    )
}
