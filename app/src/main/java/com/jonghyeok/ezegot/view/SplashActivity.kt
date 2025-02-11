package com.jonghyeok.ezegot.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.modelFactory.SplashViewModelFactory
import com.jonghyeok.ezegot.repository.SplashRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.SplashViewModel

class SplashActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(SplashRepository(SharedPreferenceManager(this)))
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { showSplashView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionCheck();
    }

    private fun permissionCheck() {
        // 위치 권한 확인
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showSplashView()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showSplashView() {
        setContent {
            Egegot_mkTheme {
                SplashScreen(viewModel)
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: SplashViewModel) {
    val context = LocalContext.current
    val isLoading = viewModel.loadingState.collectAsState().value
    val errorState = viewModel.errorState.collectAsState().value

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as SplashActivity).finish()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = App_Background_Color
    ) { }
}
