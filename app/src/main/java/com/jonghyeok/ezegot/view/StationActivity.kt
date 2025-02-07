package com.jonghyeok.ezegot.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.viewModel.StationViewModel
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.modelFactory.StationViewModelFactory
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme

class StationActivity : ComponentActivity() {
    private val viewModel: StationViewModel by viewModels {
        StationViewModelFactory(StationRepository(RetrofitInstance.api2, SharedPreferenceManager(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationName = intent.getStringExtra("station_name") ?: "역 정보 없음"
        val line = intent.getStringExtra("line") ?: "선 정보 없음"

        viewModel.fetchStationInfo(stationName, line)
        viewModel.fetchRealtimeArrival(stationName)

        setContent {
            Egegot_mkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = App_Background_Color
                ) {
                    StationScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun StationScreen(viewModel: StationViewModel) {
    val stationInfo: BasicStationInfo? by viewModel.stationInfo.collectAsState()
    val arrivalInfo: List<RealtimeArrival> by viewModel.arrivalInfo.collectAsState()
    val isFavorite: Boolean by viewModel.isFavorite.collectAsState()
    val isNotification: Boolean by viewModel.isNotification.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StationTitleBar(stationInfo, arrivalInfo)          // 타이틀 바

        Spacer(Modifier.height(20.dp))

        ButtonBar(isFavorite, { viewModel.toggleFavorite() }, isNotification, { viewModel.toggleNotification() }, stationInfo)       // 버튼 바

        Spacer(Modifier.height(20.dp))

        stationInfo?.let { ArrivalCardRow(arrivalInfo, it.lineNumber) }    // 도착 정보

        Spacer(Modifier.height(20.dp))

        RouteMapBar()                               // 노선도

        Spacer(Modifier.height(20.dp))

        EnterInfoBar()                              // 출입구 정보

        Spacer(Modifier.height(20.dp))

        StationInfoBar()                            // 지하철 역 정보

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun StationTitleBar(stationInfo: BasicStationInfo?, subwayList: List<RealtimeArrival>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .background(color = Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stationInfo?.stationName ?: "역 정보 없음",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = stationInfo?.lineNumber ?: "노선 정보 없음",
            style = TextStyle(
                color = Color(0xFF868686),
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold)
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "환승역",
            fontSize = 16.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(12.dp))

        if (subwayList.isEmpty()) return
        for (line in subwayList[0].subwayList.split(",")) {
            Image(
                modifier = Modifier.size(20.dp), // 이미지 크기 설정
                bitmap = ImageBitmap.imageResource(SubwayLine.getLineImageById(line)),
                contentDescription = "Station Image",
            )

            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun ButtonBar(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    isNotification: Boolean,
    onNotificationClick: () -> Unit,
    stationInfo: BasicStationInfo?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0),
                clip = false
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            modifier = Modifier.clickable { onFavoriteClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off),
                contentDescription = "Favorite Icon",
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "저장",
                fontSize = 16.sp,
                color = Color(0xFF2F2F2F),
                fontWeight = FontWeight.SemiBold
            )
        }

        // 구분선 추가
        Divider(

            modifier = Modifier
                .width(1.dp)
                .height(24.dp), // 구분선 두께
            color = Color(0xFFF1F1F1) // 구분선 색상
        )

        Column(
            modifier = Modifier.clickable { onNotificationClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = if (isNotification) R.drawable.ic_notification_on else R.drawable.ic_notification_off),
                contentDescription = "Notification Off Icon",
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "알람",
                fontSize = 16.sp,
                color = Color(0xFF2F2F2F),
                fontWeight = FontWeight.SemiBold
            )
        }

        // 구분선 추가
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp), // 구분선 두께
            color = Color(0xFFF1F1F1) // 구분선 색상
        )

        CallButton()

        // 구분선 추가
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp), // 구분선 두께
            color = Color(0xFFF1F1F1) // 구분선 색상
        )

        ShareButton(stationInfo)
    }
}

@Composable
fun CallButton() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:15447788")
                }
                context.startActivity(intent)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_call),
            contentDescription = "Call Icon",
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "전화",
            fontSize = 16.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ShareButton(stationInfo: BasicStationInfo?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable {
                val shareText = "${stationInfo?.stationName} ${stationInfo?.lineNumber}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText) // 공유할 텍스트와 링크
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "공유하기"))
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "Share Icon",
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "공유",
            fontSize = 16.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ArrivalCardRow(stationArrivalInfo: List<RealtimeArrival>, line: String) {
    val lineId = SubwayLine.getLineId(line)

    // 상행 / 하행 데이터 필터링
    val upArrivalInfo = stationArrivalInfo.filter { it.subwayId == lineId && it.updnLine == "상행" }
    val downArrivalInfo = stationArrivalInfo.filter { it.subwayId == lineId && it.updnLine != "상행" }

    val upDt = upArrivalInfo.firstOrNull()?.bstatnNm ?: ""
    val dnDt = downArrivalInfo.firstOrNull()?.bstatnNm ?: ""

    val itemCnt = maxOf(upArrivalInfo.size, downArrivalInfo.size)

    val totalSpacing = 44.dp // 전체 여백
    val titleSize = 18.dp // 타이틀 사이즈
    val textSpacing = 12.dp // 텍스트와 아이템 사이의 여백
    val itemSize = 28.dp // 아이템 사이즈

    // 아이템 수에 맞는 높이 계산 (아이템의 수에 따라 텍스트 크기와 여백을 고려)
    val dynamicHeight = totalSpacing + titleSize + textSpacing + itemSize * itemCnt


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArrivalCard(
            modifier = Modifier.weight(1f),
            cardHeight = dynamicHeight,
            arrivals = upArrivalInfo,
            dt = upDt,
        )

        ArrivalCard(
            modifier = Modifier.weight(1f),
            cardHeight = dynamicHeight,
            arrivals = downArrivalInfo,
            dt = dnDt,
        )
    }
}

@Composable
fun ArrivalCard(
    modifier: Modifier,
    cardHeight: Dp,
    arrivals: List<RealtimeArrival>,
    dt: String
) {
    Card(
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$dt 방면",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2F2F))
            )

            Spacer(Modifier.height(12.dp))

            // 각 Arrival 정보 렌더링
            arrivals.forEach { arrival ->
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = arrival.trainLineName.substringBefore("행"),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF868686))
                    )

                    Text(
                        text = arrival.arrivalMessage1.substringBefore("("),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEE4C4C))
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun RouteMapBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0),
                clip = false
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "노선도",
                fontSize = 18.sp,
                color = Color(0xFF2F2F2F),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "더보기",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.height(10.dp))

        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.route_test),
            contentDescription = "Route",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EnterInfoBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0),
                clip = false
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            text = "출구 정보",
            fontSize = 18.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(10.dp))

        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.map_test),
            contentDescription = "Enter Map Icon",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun StationInfoBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0),
                clip = false
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
            .height(IntrinsicSize.Min) // 내부 크기에 맞춰 Column 크기 증가
    ) {
        Text(
            text = "주소 및 전화 번호",
            fontSize = 18.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "주소",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )

            Text(
                text = "경기도 수원시 팔달구 덕영대로 944",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "대표번호",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )

            Text(
                text = "1544-7788",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "유실물센터",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )

            Text(
                text = "1544-7788",
                fontSize = 14.sp,
                color = Color(0xFF868686),
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StationPreview() {
    Egegot_mkTheme {
    }
}