package com.jonghyeok.ezegot.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.modelFactory.StationViewModelFactory
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.StationViewModel

class StationActivity : ComponentActivity() {
    private val viewModel: StationViewModel by viewModels {
        StationViewModelFactory(StationRepository(RetrofitInstance.api2, SharedPreferenceManager(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationName = intent.getStringExtra("station_name") ?: "역 정보 없음"
        val line = intent.getStringExtra("line") ?: "선 정보 없음"

        viewModel.loadStationInfo(stationName, line)
        viewModel.loadArrivalInfo(stationName)

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
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 10.dp)
            .background(color = Color(0xFFF5F5F5)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(end = 12.dp),
            text = stationInfo?.stationName ?: "역 정보 없음",
            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        )

        Text(
            text = stationInfo?.lineNumber ?: "노선 정보 없음",
            style = TextStyle(fontSize = 32.sp, color = Color(0xFF868686), fontWeight = FontWeight.SemiBold)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(color = Color(0xFFF5F5F5)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(end = 12.dp),
            text = "환승역",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
        )

        if (subwayList.isNotEmpty()) {
            for (line in subwayList[0].subwayList.split(",")) {
                Image(
                    modifier = Modifier.padding(end = 8.dp).size(20.dp),
                    bitmap = ImageBitmap.imageResource(SubwayLine.getLineImageById(line)),
                    contentDescription = "Station Image",
                )
            }
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
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0)
            )
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        FavoriteButton(isFavorite, onFavoriteClick)
        DividerView()
        NotificationButton(isNotification, onNotificationClick)
        DividerView()
        CallButton()
        DividerView()
        ShareButton(stationInfo)
    }
}

@Composable
fun DividerView() {
    Divider(
        modifier = Modifier.width(1.dp).height(24.dp),
        color = Color(0xFFF1F1F1)
    )
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onFavoriteClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onFavoriteClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(bottom = 8.dp).size(20.dp),
            painter = painterResource(id = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off),
            contentDescription = "Favorite Icon",
        )

        Text(
            text = "저장",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun NotificationButton(isNotification: Boolean, onNotificationClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onNotificationClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(bottom = 8.dp).size(20.dp),
            painter = painterResource(id = if (isNotification) R.drawable.ic_notification_on else R.drawable.ic_notification_off),
            contentDescription = "Notification Off Icon",
        )

        Text(
            text = "알람",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
        )
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
            modifier = Modifier.padding(bottom = 8.dp).size(20.dp),
            painter = painterResource(id = R.drawable.ic_call),
            contentDescription = "Call Icon",
        )

        Text(
            text = "전화",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
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
            modifier = Modifier.padding(bottom = 8.dp).size(20.dp),
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "Share Icon",
        )

        Text(
            text = "공유",
            style = TextStyle(fontSize = 16.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
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

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArrivalCard(
            modifier = Modifier.weight(1f),
            arrivals = upArrivalInfo,
            dt = upDt,
        )

        ArrivalCard(
            modifier = Modifier.weight(1f),
            arrivals = downArrivalInfo,
            dt = dnDt,
        )
    }
}

@Composable
fun ArrivalCard(
    modifier: Modifier,
    arrivals: List<RealtimeArrival>,
    dt: String
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .padding(horizontal = 2.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF7090B0))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 24.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$dt 방면",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F2F2F))
            )

            Spacer(Modifier.height(20.dp))

            arrivals.forEach { arrival ->
                Row (
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = arrival.bstatnNm,
                        style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Medium)
                    )

                    Text(
                        text = arrival.arrivalMessage1.substringBefore("(").takeWhile { it != '(' }.trimEnd(),
                        style = TextStyle(fontSize = 14.sp, color = Color(0xFFEE4C4C), fontWeight = FontWeight.Medium)
                    )
                }
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
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "노선도",
                style = TextStyle(fontSize = 18.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
            )

            Text(
                text = "더보기",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFFAAAAAA), fontWeight = FontWeight.Light)
            )
        }

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
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = "출구 정보",
            style = TextStyle(fontSize = 18.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
        )

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
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF7090B0)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = "주소 및 전화 번호",
            style = TextStyle(fontSize = 18.sp, color = Color(0xFF2F2F2F), fontWeight = FontWeight.SemiBold)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "주소",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )

            Text(
                text = "경기도 수원시 팔달구 덕영대로 944",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "대표번호",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )

            Text(
                text = "1544-7788",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Text(
                modifier = Modifier.width(72.dp),
                text = "유실물센터",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )

            Text(
                text = "1544-7788",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF868686), fontWeight = FontWeight.Light)
            )
        }
    }
}