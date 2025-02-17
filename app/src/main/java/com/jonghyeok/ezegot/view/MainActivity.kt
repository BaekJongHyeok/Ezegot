package com.jonghyeok.ezegot.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.modelFactory.MainViewModelFactory
import com.jonghyeok.ezegot.repository.LocationRepository
import com.jonghyeok.ezegot.repository.MainRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(MainRepository(RetrofitInstance.api2, SharedPreferenceManager(this)), LocationRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Egegot_mkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = App_Background_Color
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateFavoriteStation()   // 즐겨 찾기 Update
        viewModel.loadRealtimeArrival()     // 도착 정보 Load
        viewModel.updateCurrentLocation()   // 현재 위치 Update
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        TitleBar() // 타이틀 바
        Spacer(Modifier.height(20.dp))
        SearchBar() // 검색 바
        Spacer(Modifier.height(40.dp))
        FavoriteBar(viewModel) // 즐겨찾기 바
        Spacer(Modifier.height(30.dp))
        LineButtonBar() // 띠 바
        Spacer(Modifier.height(40.dp))
        NearbyBar(viewModel) // 근처 역 정보 바
    }
}

@Composable
fun TitleBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(color = Color(0xFFF5F5F5)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EZEGOT",
            style = TextStyle(fontSize = 28.sp,fontWeight = FontWeight.Black)
        )

        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_setting),
            contentDescription = "Setting Icon"
        )
    }
}

@Composable
fun SearchBar() {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SearchBox(
            modifier = Modifier.weight(1f),
            onClick = {
            val intent = Intent(context, SearchActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            context.startActivity(intent, options.toBundle())
        })

        Spacer(modifier = Modifier.width(8.dp))

        LocationButton(onClick = {
            context.startActivity(Intent(context, MapActivity::class.java))
        })
    }
}

@Composable
fun SearchBox(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.padding(start = 30.dp),
            text = "지하철 역 이름 검색",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFFBEC1CF), fontWeight = FontWeight.Medium)
        )

        Image(
            modifier = Modifier.padding(end = 20.dp),
            painter = painterResource(R.drawable.ic_search),
            contentDescription = "Search Icon",
        )
    }
}

@Composable
fun LocationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        Button(
            modifier = Modifier.size(50.dp).background(color = Color(0xFF7BAFF6)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            onClick = { onClick() }
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                contentDescription = "Location Icon",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun FavoriteBar(viewModel: MainViewModel) {
    val favoriteStationInfos by viewModel.favoriteStationList.collectAsState()
    val favoriteArrivalInfos by viewModel.realtimeArrivalInfo.collectAsState()

    FavoriteTitleBar(viewModel)

    if (favoriteStationInfos.isEmpty()) {
        EmptyFavoriteCard()
    } else {
        // 즐겨찾기 CardView를 동적으로 생성
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // 카드 간격 설정
        ) {
            items(favoriteStationInfos) { favoriteStation ->
                val lineId = SubwayLine.getLineId(favoriteStation.lineNumber)
                val arrivalInfo = favoriteArrivalInfos[favoriteStation.stationName] ?: return@items

                // 상행 / 하행 데이터 필터링
                val upArrivalInfo = arrivalInfo.filter { it.subwayId == lineId && it.updnLine == "상행" }
                val downArrivalInfo = arrivalInfo.filter { it.subwayId == lineId && it.updnLine != "상행" }

                // 중복 제거 (subwayId, updnLine, bstatnNm을 기준으로)
                val distinctUpArrivalInfo = upArrivalInfo.distinctBy { it.subwayId to it.updnLine to it.bstatnNm }
                val distinctDownArrivalInfo = downArrivalInfo.distinctBy { it.subwayId to it.updnLine to it.bstatnNm }

                // 중복을 제거한 후 각 리스트에서 최대 2개씩만 가져오기
                val upArrivalInfoLimited = distinctUpArrivalInfo.take(2)
                val downArrivalInfoLimited = distinctDownArrivalInfo.take(2)

                FavoriteCard(favoriteStation, upArrivalInfoLimited, downArrivalInfoLimited)
            }

        }

        PageIndicator(favoriteStationInfos.size)
    }
}

@Composable
fun FavoriteTitleBar(viewModel: MainViewModel) {
    // 실시간 시간 업데이트
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    val formatter = DateTimeFormatter.ofPattern("a h:mm")
    val formattedTime = currentTime.format(formatter)

    // 회전 애니메이션
    var rotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 500)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = "즐겨찾기",
            style = TextStyle(fontSize = 24.sp, color = Color(0xFF3A3A3A), fontWeight = FontWeight.SemiBold)
        )

        Row(
            modifier = Modifier
                .padding(end = 24.dp)
                .clickable {
                    currentTime = LocalTime.now()
                    viewModel.loadRealtimeArrival()
                    rotation += 360f
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedTime,
                style = TextStyle(fontSize = 14.sp, color = Color(0xFFB0B0B0), fontWeight = FontWeight.Medium)
            )

            Spacer(Modifier.width(8.dp))

            // 회전 애니메이션을 적용한 이미지
            Image(
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer(rotationZ = animatedRotation),
                painter = painterResource(id = R.drawable.ic_refresh),
                contentDescription = "Refresh Icon",
            )
        }
    }
}

@Composable
fun EmptyFavoriteCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "즐겨찾기 없음", // 카드 내부에 텍스트 추가
            style = TextStyle(fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun FavoriteCard(
    station: BasicStationInfo,
    upArrivalInfos: List<RealtimeArrival>,
    downArrivalInfos: List<RealtimeArrival>
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight()
            .padding(horizontal = 2.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                val intent = Intent(context, StationActivity::class.java).apply {
                    putExtra("station_name", station.stationName)
                    putExtra("line", station.lineNumber)
                }
                context.startActivity(intent)
            }
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = station.stationName,
                    style = TextStyle(fontSize = 20.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
                )

                Spacer(Modifier.width(10.dp))

                Image(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(SubwayLine.getLineImage(station.lineNumber)),
                    contentDescription = "Station Image",
                )
            }

            Spacer(Modifier.height(20.dp))

            // 상행 정보 표시
            Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                // 데이터가 있을 때 반복문
                for (i in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stationInfo = upArrivalInfos.getOrNull(i)

                        // stationInfo가 null인 경우 기본 값을 "정보없음"으로 처리
                        val stationName = stationInfo?.bstatnNm ?: "정보없음"
                        val arrivalMessage = stationInfo?.arrivalMessage1?.takeWhile { it != '(' }?.trimEnd() ?: "정보없음"

                        // 색상 결정
                        val color1 = if (stationInfo?.bstatnNm == null) Color.White else Color(0xFF868686)
                        val color2 = if (stationInfo?.arrivalMessage1 == null) Color.White else Color(0xFFEE4C4C)

                        Text(
                            text = stationName,
                            style = TextStyle(fontSize = 14.sp, color = color1, fontWeight = FontWeight.Medium)
                        )

                        Text(
                            text = arrivalMessage,
                            style = TextStyle(fontSize = 14.sp, color = color2, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            // 구분선 추가
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = Color(0xFFF1F1F1) // 구분선 색상
            )

            Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                // 하행 정보 표시
                for (i in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stationInfo = downArrivalInfos.getOrNull(i)

                        // stationInfo가 null인 경우 기본 값을 "정보없음"으로 처리
                        val stationName = stationInfo?.bstatnNm ?: "정보없음"
                        val arrivalMessage = stationInfo?.arrivalMessage1?.takeWhile { it != '(' }?.trimEnd() ?: "정보없음"

                        // 색상 결정
                        val color1 = if (stationInfo?.bstatnNm == null) Color.White else Color(0xFF868686)
                        val color2 = if (stationInfo?.arrivalMessage1 == null) Color.White else Color(0xFFEE4C4C)

                        Text(
                            text = stationName,
                            style = TextStyle(fontSize = 14.sp, color = color1, fontWeight = FontWeight.Medium)
                        )

                        Text(
                            text = arrivalMessage,
                            style = TextStyle(fontSize = 14.sp, color = color2, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageIndicator(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0B0B0))
            )
        }
    }
}


@Composable
fun LineButtonBar() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF7BAFF6))
            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.letskorail.com/"))) }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center).padding(vertical = 18.dp),
            text = "기차 / KTX 승차권 예매 하기",
            style = TextStyle(fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun NearbyBar(viewModel: MainViewModel) {
    val context = LocalContext.current

    var isPermissionGranted = false
    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        isPermissionGranted = true
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    Column {
        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = "근처 역 정보",
            style = TextStyle(fontSize = 24.sp, color = Color(0xFF3A3A3A), fontWeight = FontWeight.SemiBold),
        )

        if (isPermissionGranted && isGPSOn) {
            // 위치 권한과 GPS가 허용되어 있다면 근처 역 정보 표시
            NearbyStationViews(viewModel)
        } else {
            // 위치 권한 또는 GPS 허용 안내
            PermissionGuide(isPermissionGranted)
        }
    }
}

@Composable
fun NearbyStationViews(viewModel: MainViewModel) {
    val nearbyStations: List<BasicStationInfo> by viewModel.nearbyStationList.collectAsState()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        columns = GridCells.Fixed(3), // 한 줄에 3개의 아이템
        horizontalArrangement = Arrangement.spacedBy(6.dp), // 수평 간격
    ) {
        items(nearbyStations) { station -> NearbyStationView(station) }
    }
}

@Composable
fun NearbyStationView(station: BasicStationInfo) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                val intent = Intent(context, StationActivity::class.java).apply {
                    putExtra("station_name", station.stationName)
                    putExtra("line", station.lineNumber)
                }
                context.startActivity(intent)
            }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center).padding(16.dp),
            text = station.stationName,
            style = TextStyle(fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun PermissionGuide(isPermissionGranted: Boolean) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                val intent = if (!isPermissionGranted) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                } else { Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) }
                context.startActivity(intent)
            }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center).padding(20.dp),
            text = if (!isPermissionGranted) { "기능 사용을 위해 위치 권한을 허용해 주세요" } else { "기능 사용을 위해 GPS를 켜주세요" },
            style = TextStyle(fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold),
        )
    }
}
