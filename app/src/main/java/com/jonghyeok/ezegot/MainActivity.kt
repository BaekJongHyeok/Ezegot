package com.jonghyeok.ezegot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.view.SearchActivity
import com.jonghyeok.ezegot.view.StationActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val favoriteStations = listOf(
        FavoriteStation("수원", listOf("왕십리", "왕십리", "고색", "인천"), listOf("5분", "17분", "4분", "11분"), R.drawable.suinline),
        FavoriteStation("수원", listOf("청량리", "동묘앞", "천안", "신창"), listOf("2분", "8분", "4분", "13분"), R.drawable.ic_line1),
        FavoriteStation("가산디지털단지", listOf("수원", "동인천", "가산", "시청"), listOf("2분", "12분", "8분", "6분"), R.drawable.ic_line1),
    )

    val stationNames = listOf("수원", "가산디지털단지", "매탄권선", "수원시청", "금정", "동인천")

    val currentPage = remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5) // 화면 배경 색상 설정
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀 바
            TitleBar()

            // 검색바
            SearchBar()

            Spacer(Modifier.height(40.dp))

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

            Spacer(Modifier.height(10.dp))

            // 즐겨찾기 CardView를 동적으로 생성
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // 전체 여백 설정
            ) {
                items(favoriteStations) { station ->
                    FavoriteCard(station)
                }
            }

            // 페이지 인디케이터
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(favoriteStations.size
                ) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .background(
                                if (currentPage.value == index) Color(0xFFA5A5A5) else Color(0xFFD9D9D9),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

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
                    .background(color = Color(0xFF7BAFF6)) // 배경색
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center),
                    text = "기차 / KTX 운행 정보 보러가기",
                    fontSize = 14.sp,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 24.dp),
                    text = "근처 역 정보",
                    fontSize = 24.sp,
                    color = Color(0xFF3A3A3A),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(5.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 한 줄에 3개의 아이템
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp), // 전체 여백 설정
                horizontalArrangement = Arrangement.spacedBy(6.dp), // 수평 간격
            ) {
                items(stationNames) { stationName -> // stationNames 리스트를 사용
                    Card(
                        modifier = Modifier
                            .fillMaxWidth() // 정사각형 형태 유지
                            .padding(vertical = 5.dp)
                            .height(56.dp)
                            .shadow( // 그림자 추가
                                elevation = 1.dp, // 그림자 크기
                                shape = RoundedCornerShape(12.dp), // 둥근 모서리
                                spotColor = Color(0xFF7090B0),
                                clip = false // 그림자 밖의 요소를 클립하지 않음
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { // 클릭 이벤트 처리
                                val intent = Intent(context, StationActivity::class.java)
                                context.startActivity(intent)
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
                                text = stationName, // 리스트에서 가져온 역 이름
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(5.dp))

        }
    }
}

@Composable
fun TitleBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF5F5F5))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EZEGOT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = R.drawable.ic_setting),
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
                .background(color = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(color = Color(0xFFFFFFFF))
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
                    painter = painterResource(id = R.drawable.ic_search),
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
                onClick = {
                    val intent = Intent(context, MapActivity::class.java)
                    context.startActivity(intent)
                }
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
fun FavoriteCard(station: FavoriteStation) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(168.dp)
            .height(206.dp)
            .padding(start = 6.dp, end = 6.dp) // 각 카드 사이 간격
            .clickable { // 클릭 이벤트 처리
                val intent = Intent(context, StationActivity::class.java)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp), // CardElevation으로 설정
        colors = CardDefaults.cardColors(containerColor = Color.White) // 배경색 설정
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 카드 안 여백 설정
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.stationName,
                    fontSize = 16.sp,
                    color = Color(0xFF2F2F2F),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.width(12.dp))

                Image(
                    modifier = Modifier.size(20.dp), // 이미지 크기 설정
                    painter = painterResource(id = station.imageRes),
                    contentDescription = "Station Image",
                )
            }

            Spacer(Modifier.height(10.dp))

            // 첫 번째 두 방면과 시간 표시
            for (i in 0..1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = station.directions[i],
                        fontSize = 14.sp,
                        color = Color(0xFF868686),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = station.times[i],
                        fontSize = 14.sp,
                        color = Color(0xFFEE4C4C),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp)) // 각 항목 간 간격
            }

            // 구분선 추가
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp), // 구분선 두께
                color = Color(0xFFF1F1F1) // 구분선 색상
            )

            Spacer(Modifier.height(8.dp)) // 구분선 후 간격

            // 두 번째 두 방면과 시간 표시
            for (i in 2..3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = station.directions[i],
                        fontSize = 14.sp,
                        color = Color(0xFF868686),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = station.times[i],
                        fontSize = 14.sp,
                        color = Color(0xFFEE4C4C),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp)) // 각 항목 간 간격
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingCardPreview() {
    Egegot_mkTheme {
        MainScreen()
    }
}

