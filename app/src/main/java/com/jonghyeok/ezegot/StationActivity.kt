package com.jonghyeok.ezegot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme

class StationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationName = intent.getStringExtra("station_name") ?: "역 정보 없음"
        val line = intent.getStringExtra("line") ?: "선 정보 없음"

        val viewModel: StationViewModel by viewModels()

        // API 호출을 Activity에서 한 번만 수행
        viewModel.fetchStationArrivalInfo(stationName)
        viewModel.checkIfStationIsSaved(stationName)

        setContent {
            Egegot_mkTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    StationContent(
                        modifier = Modifier.padding(paddingValues),
                        stationName = stationName,
                        line = line,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun StationContent(modifier: Modifier = Modifier, stationName: String, line: String, viewModel: StationViewModel) {
    val stationArrivalInfo by viewModel.stationArrivalInfo.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

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
            stationArrivalInfo = stationArrivalInfo,
            isSaved = isSaved,
            onFavoriteClick = { viewModel.toggleStationFavorite(stationName) }
        )
    }
}

@Composable
fun StationScreen(stationName: String, line: String, stationArrivalInfo: List<Arrival>, isSaved: Boolean, onFavoriteClick: () -> Unit) {
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

            ButtonBar(isSaved, onFavoriteClick)

            Spacer(Modifier.height(20.dp))

            ArrivalCardRow(stationArrivalInfo, line)

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
fun ButtonBar(isSaved: Boolean, onFavoriteClick: () -> Unit) {
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
                painter = painterResource(
                    id = if (isSaved) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off
                ),
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
fun ArrivalCardRow(stationArrivalInfo: List<Arrival>, line: String) {
    val lineId = SubwayLine.getLineId(line)

    var upDt = "";
    var dnDt = "";
    val upArrivalInfo = mutableListOf<Arrival>()
    val downArrivalInfo = mutableListOf<Arrival>()

    stationArrivalInfo.forEach { arrival ->
        if (arrival.subwayId != lineId) {
            return@forEach
        }

        if (arrival.updnLine == "상행") {
            upArrivalInfo.add(arrival)
            upDt = arrival.bstatnNm
        } else {
            downArrivalInfo.add(arrival)
            dnDt = arrival.bstatnNm
        }
    }

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
            ArrivalCard(cardWidth, upArrivalInfo, upDt)

            // 하행 카드에 두 개의 도착 정보 표시
            ArrivalCard(cardWidth, downArrivalInfo, dnDt)
        }
    }
}

@Composable
fun ArrivalCard(cardWidth: Dp, arrivals: List<Arrival>, dt: String) {
    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(206.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$dt 방면",
                fontSize = 18.sp,
                color = Color(0xFF2F2F2F),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // 각 Arrival 정보 렌더링
            arrivals.forEach { arrival ->
                val trainLineName = arrival.trainLineName.takeWhile { it != '행' }
                val arrivalMsg = arrival.arrivalMessage1.takeWhile { it != '(' }

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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