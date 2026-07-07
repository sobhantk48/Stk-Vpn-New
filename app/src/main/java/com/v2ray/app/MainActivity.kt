package com.v2ray.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.v2ray.app.bg.V2RayService
import com.v2ray.app.navigation.AppNavigation
import com.v2ray.app.ui.theme.V2rayAppTheme
import com.v2ray.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by hiltViewModel()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            // مجوز رد شد
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setActivity(this)
        setContent {
            V2rayAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 200)
        }
    }

    fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            viewModel.onVpnPermissionGranted()
        }
    }

    fun startVpnService(config: String, profileId: String) {
        val intent = Intent(this, V2RayService::class.java).apply {
            action = V2RayService.ACTION_CONNECT
            putExtra(V2RayService.EXTRA_CONFIG, config)
            putExtra(V2RayService.EXTRA_PROFILE_ID, profileId)
        }
        startService(intent)
    }

    fun stopVpnService() {
        val intent = Intent(this, V2RayService::class.java).apply {
            action = V2RayService.ACTION_DISCONNECT
        }
        startService(intent)
    }
}
