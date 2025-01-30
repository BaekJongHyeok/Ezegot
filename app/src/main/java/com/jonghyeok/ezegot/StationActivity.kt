package com.jonghyeok.ezegot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme

class StationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationName = intent.getStringExtra("station_name") ?: "역 정보 없음"
        val line = intent.getStringExtra("line") ?: "선 정보 없음"

        setContent {
            Egegot_mkTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    StationContent(
                        modifier = Modifier.padding(paddingValues),
                        stationName = stationName,
                        line = line
                    )
                }
            }
        }
    }
}

@Composable
fun StationContent(modifier: Modifier = Modifier, stationName: String, line: String) {
    val stationArrivalInfo = remember { mutableStateOf<List<Arrival>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api2.getStationArrivalInfo(stationName)
            stationArrivalInfo.value = response.arrivals // 데이터 저장
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(App_Background_Color),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StationScreen(
            stationName = stationName,
            line = line,
            stationArrivalInfo = stationArrivalInfo.value
        )
    }
}

@Composable
fun StationScreen(stationName: String, line: String, stationArrivalInfo: List<Arrival>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = App_Background_Color // 화면 배경 색상 설정
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StationTitleBar(stationName, line)

            Spacer(Modifier.height(20.dp))

            ButtonBar()

            Spacer(Modifier.height(20.dp))

            ArrivalCardRow(stationArrivalInfo)

            Spacer(Modifier.height(20.dp))

            RouteMapBar()

            Spacer(Modifier.height(20.dp))

            EnterInfoBar()

            Spacer(Modifier.height(20.dp))

            StationInfoBar()

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun StationTitleBar(stationName: String, line: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .background(color = Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stationName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = line,
            style = TextStyle(color = Color(0xFF868686), fontSize = 24.sp, fontWeight = FontWeight.Medium)
        )
    }

    Spacer(Modifier.height(12.dp))

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

        Image(
            modifier = Modifier.size(20.dp), // 이미지 크기 설정
            bitmap = ImageBitmap.imageResource(R.drawable.ic_line1),
            contentDescription = "Station Image",
        )
    }
}

@Composable
fun ButtonBar() {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_favorite_off),
                contentDescription = "Favorite Off Icon",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_notification_off),
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

        Column(
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

        // 구분선 추가
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp), // 구분선 두께
            color = Color(0xFFF1F1F1) // 구분선 색상
        )

        Column(
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
}

@Composable
fun ArrivalCardRow(stationArrivalInfo: List<Arrival>) {
    // 상행, 하행으로 분리
    val upboundArrivals = stationArrivalInfo.take(2)
    val downboundArrivals = stationArrivalInfo.drop(2).take(2)

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val availableWidth = maxWidth - 40.dp
        val cardWidth = (availableWidth - 12.dp) / 2 // 공백을 고려하여 두 개 배치

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 상행 카드에 두 개의 도착 정보 표시
            ArrivalCard(cardWidth, upboundArrivals, "상행")

            // 하행 카드에 두 개의 도착 정보 표시
            ArrivalCard(cardWidth, downboundArrivals, "하행")
        }
    }
}

@Composable
fun ArrivalCard(cardWidth: Dp, arrivals: List<Arrival>, direction: String) {
    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(206.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = direction,
                fontSize = 18.sp,
                color = Color(0xFF2F2F2F),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // 각 Arrival 정보 렌더링
            arrivals.forEach { arrival ->
                val trainLineName = arrival.trainLineName.takeWhile { it != '행' } + "행"
                val arrivalMsg = arrival.arrivalMessage.takeWhile { it != '(' }

                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = trainLineName,
                        fontSize = 14.sp,
                        color = Color(0xFF868686),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = arrivalMsg,
                        fontSize = 14.sp,
                        color = Color(0xFFEE4C4C),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(12.dp))
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