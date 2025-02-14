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

    // loadingState가 false로 바뀔 때까지 기다린 후 MainActivity로 이동
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            // MainActivity로 이동
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as SplashActivity).finish()
        }
    }

    // 에러 상태 처리 (optional)
    LaunchedEffect(errorState) {
        if (errorState != null) {
            // 에러 메시지가 있을 경우 처리
            Log.e("SplashActivity", "Error: $errorState")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = App_Background_Color
    ) { }
}
