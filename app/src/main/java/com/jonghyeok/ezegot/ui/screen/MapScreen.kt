package com.jonghyeok.ezegot.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.jonghyeok.ezegot.ui.theme.*
import com.jonghyeok.ezegot.viewModel.LocationState
import com.jonghyeok.ezegot.viewModel.MainViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val locationState by viewModel.locationState.collectAsState()
    var mapInitialized by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.retryLocation()
    }

    LaunchedEffect(Unit) {
        hasPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.updateCurrentLocation()
        }
    }

    // 첫 위치를 얻었을 때 한 번만 카메라를 옮긴다. 이후 갱신으로 사용자의 조작을 덮지 않는다.
    LaunchedEffect(locationState) {
        val loc = locationState as? LocationState.Available ?: return@LaunchedEffect
        if (!mapInitialized) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f)
            )
            mapInitialized = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            // 위치를 못 얻으면 기본 좌표 지도를 그대로 두지 않고 상태를 보여준다
            when (locationState) {
                is LocationState.Loading -> LocationLoadingCard()
                is LocationState.Unavailable -> LocationUnavailableCard(onRetry = { viewModel.retryLocation() })
                is LocationState.Available -> GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "위치 권한이 필요합니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        // 오버레이 – 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy900.copy(alpha = 0.9f), Navy900.copy(alpha = 0f))))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = TextOnDark)
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            onClickLabel = "검색 화면 열기",
                            role = Role.Button
                        ) { onSearchClick?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "지하철 역 이름 검색",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextHint
                        )
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
