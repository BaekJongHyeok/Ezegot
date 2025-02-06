package com.jonghyeok.ezegot.view

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.jonghyeok.ezegot.MapActivity
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.modelFactory.MainViewModelFactory
import com.jonghyeok.ezegot.repository.MainRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(MainRepository(RetrofitInstance.api2, SharedPreferenceManager(this)))
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

        viewModel.getFavoriteStationList()
        viewModel.fetchRealtimeArrival()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val favoriteStations: List<BasicStationInfo> by viewModel.favoriteStationList.collectAsState()
    val favoriteArrivalInfos: Map<String, List<RealtimeArrival>> by viewModel.realtimeArrivalInfo.collectAsState()
    val nearbyStations: List<BasicStationInfo> by viewModel.nearbyStationList.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleBar() // 타이틀 바
        SearchBar() // 검색 바
        Spacer(Modifier.height(40.dp))
        FavoriteBar(favoriteStations, favoriteArrivalInfos) // 즐겨찾기 바
        Spacer(Modifier.height(40.dp))
        LineButtonBar() // 띠 바
        Spacer(Modifier.height(40.dp))
        NearbyBar(nearbyStations) // 근처 역 정보 바
        Spacer(Modifier.height(5.dp))
    }
}

@Composable
fun TitleBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EZEGOT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(R.drawable.ic_setting),
            contentDescription = "Setting Icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SearchBar() {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color(0xFF7090B0),
                    clip = false
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFFFFFFFF))
                    .clickable {
                        val intent = Intent(context, SearchActivity::class.java)
                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            context,
                            android.R.anim.fade_in, // 새로운 액티비티가 나타날 때의 애니메이션
                            android.R.anim.fade_out // 기존 액티비티가 사라질 때의 애니메이션
                        )
                        context.startActivity(intent, options.toBundle())
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.padding(start = 35.dp),
                    text = "지하철 역 이름 검색",
                    fontSize = 16.sp,
                    color = Color(0xFFBEC1CF),
                    fontWeight = FontWeight.Medium,
                )

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search Icon",
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(12.dp))
                .background(color = Color.White)
        ) {
            Button(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = Color(0xFF7BAFF6)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                onClick = { context.startActivity(Intent(context, MapActivity::class.java)) }
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
}

@Composable
fun FavoriteBar(
    favoriteStations: List<BasicStationInfo>,
    favoriteArrivalInfos: Map<String, List<RealtimeArrival>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = "즐겨찾기",
            fontSize = 24.sp,
            color = Color(0xFF3A3A3A),
            fontWeight = FontWeight.Bold,
        )

        Text(
            modifier = Modifier.padding(end = 20.dp),
            text = "오전 10:23",
            fontSize = 14.sp,
            color = Color(0xFFB0B0B0),
            fontWeight = FontWeight.Medium
        )
    }

    if (favoriteStations.isEmpty()) {
        EmptyFavoriteCard()
    } else {
        // 즐겨찾기 CardView를 동적으로 생성
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // 카드 간격 설정
        ) {
            items(favoriteStations) { favoriteStation ->
                val lineId = SubwayLine.getLineId(favoriteStation.lineNumber)

                val upArrivalInfo = mutableListOf<RealtimeArrival>()
                val downArrivalInfo = mutableListOf<RealtimeArrival>()

                val arrivalInfo = favoriteArrivalInfos[favoriteStation.stationName] ?: return@items
                arrivalInfo.forEach { arrival ->
                    if (arrival.subwayId != lineId) {
                        return@forEach
                    }

                    if (arrival.updnLine == "상행") {
                        upArrivalInfo.add(arrival)
                    } else {
                        downArrivalInfo.add(arrival)
                    }
                }

                FavoriteCard(favoriteStation, upArrivalInfo, downArrivalInfo)
            }
        }

        // 페이지 인디케이터 (모든 인디케이터가 동일한 색상으로 표시)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(favoriteStations.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color = Color(0xFFB0B0B0))
                )
            }
        }
    }
}

@Composable
fun EmptyFavoriteCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.cardColors(Color.White) // 배경색 설정
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "즐겨찾기 없음", // 카드 내부에 텍스트 추가
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FavoriteCard(
    station: BasicStationInfo,
    upArrivalInfos: MutableList<RealtimeArrival>,
    downArrivalInfos: MutableList<RealtimeArrival>
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(168.dp)
            .wrapContentHeight()
            .padding(start = 6.dp, end = 6.dp)
            .clickable {
                val intent = Intent(context, StationActivity::class.java).apply {
                    putExtra("station_name", station.stationName)
                    putExtra("line", station.lineNumber)
                }
                context.startActivity(intent)
             },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.cardColors(Color.White) // 배경색 설정
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.stationName,
                    fontSize = 20.sp,
                    color = Color(0xFF2F2F2F),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.width(10.dp))

                Image(
                    modifier = Modifier.size(18.dp), // 이미지 크기 설정
                    painter = painterResource(SubwayLine.getLineImage(station.lineNumber)),
                    contentDescription = "Station Image",
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth().heightIn(64.dp)) {
                // 상행 정보 표시
                for (i in 0..<upArrivalInfos.size) {
                    var bstatnNm = ""
                    var arrivalMsg = ""

                    if (upArrivalInfos.isNotEmpty()) {
                        bstatnNm = upArrivalInfos[i].bstatnNm
                        arrivalMsg = upArrivalInfos[i].arrivalMessage1.takeWhile { it != '(' }.trimEnd()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bstatnNm ,
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
                }
            }

            // 구분선 추가
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Color(0xFFF1F1F1) // 구분선 색상
            )

            Column(modifier = Modifier.fillMaxWidth().heightIn(64.dp)) {
                // 하행 정보 표시
                for (i in 0..<downArrivalInfos.size) {
                    var bstatnNm = ""
                    var arrivalMsg = ""

                    if (downArrivalInfos.isNotEmpty()) {
                        bstatnNm = downArrivalInfos[i].bstatnNm
                        arrivalMsg = downArrivalInfos[i].arrivalMessage1.takeWhile { it != '(' }.trimEnd()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bstatnNm,
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
                }
            }
        }
    }
}

@Composable
fun LineButtonBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 4.dp, // 그림자 크기
                shape = RoundedCornerShape(12.dp), // 둥근 모서리
                clip = false // 그림자를 클립하지 않음
            )
            .clip(RoundedCornerShape(12.dp)) // 둥근 모서리를 적용
            .background(Color(0xFF7BAFF6)) // 배경색
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "기차 / KTX 운행 정보 보러가기",
            fontSize = 14.sp,
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun NearbyBar(nearbyStations: List<BasicStationInfo>) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = "근처 역 정보",
            fontSize = 24.sp,
            color = Color(0xFF3A3A3A),
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(5.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 한 줄에 3개의 아이템
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), // 수평 간격
        ) {
            items(nearbyStations) { station ->// stationNames 리스트를 사용
                Card(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .height(56.dp)
                        .shadow( // 그림자 추가
                            elevation = 1.dp, // 그림자 크기
                            shape = RoundedCornerShape(12.dp), // 둥근 모서리
                            spotColor = Color(0xFF7090B0),
                            clip = false // 그림자 밖의 요소를 클립하지 않음
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            context.startActivity(
                                Intent(
                                    context,
                                    StationActivity::class.java
                                )
                            )
                        }, // 둥근 모서리 설정
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp), // 그림자 효과
                    colors = CardDefaults.cardColors(containerColor = Color.White) // 배경색 설정
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp), // 카드 내부 여백
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = station.stationName,
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
