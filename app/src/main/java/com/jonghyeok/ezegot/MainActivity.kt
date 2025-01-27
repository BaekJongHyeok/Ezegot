package com.jonghyeok.ezegot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    MainContent(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val itemCount = 10 // The number of items
    val currentPage = remember { mutableStateOf(0) } // Track cu

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5) // 화면 배경 색상 설정
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5)
                    )
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

            // 검색바
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // 양쪽 끝으로 요소 정렬
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = 8.dp, // 그림자 크기
                            shape = RoundedCornerShape(12.dp), // 둥근 모서리
                            spotColor = Color(0xFF7090B0),
                            clip = false // 그림자를 클립하지 않음
                        )
                        .clip(RoundedCornerShape(12.dp)) // 둥근 모서리를 적용
                        .background(color = Color.White) // 배경색
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // 남은 공간을 차지하도록 설정
                            .height(48.dp)
                            .background(color = Color(0xFFFFFFFF)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 35.dp),
                            text = "지하철 역 이름 검색",
                            fontSize = 16.sp,
                            color = Color(0xFFBEC1CF),
                            fontWeight = FontWeight.Medium,
                        )

                        Image(
                            modifier = Modifier
                                .padding(end = 20.dp),
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search Icon",
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp, // 그림자 크기
                            shape = RoundedCornerShape(12.dp), // 둥근 모서리
                            clip = false // 그림자를 클립하지 않음
                        )
                        .clip(RoundedCornerShape(12.dp)) // 둥근 모서리를 적용
                        .background(color = Color.White) // 배경색
                ) {
                    Button(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF87CEEB), // 버튼 색상 설정
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { }
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_mylocation), // 위치 아이콘
                            contentDescription = "Location Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
                    text = "즐겨찾기",
                    fontSize = 24.sp,
                    color = Color(0xFF3A3A3A),
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    modifier = Modifier
                        .padding(end = 20.dp),
                    text = "오전 10:23",
                    fontSize = 14.sp,
                    color = Color(0xFF868686),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // 전체 여백 설정
            ) {
                items(10) { index -> // 항목의 수는 10으로 가정 (필요에 맞게 수정)
                    Card(
                        modifier = Modifier
                            .width(168.dp)
                            .height(194.dp)
                            .padding(start = 6.dp, end = 6.dp), // 각 카드 사이 간격
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
                            Text(
                                text = "오전 10:23",
                                fontSize = 14.sp,
                                color = Color(0xFF868686),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }



            // 페이지 인디케이터
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            ) {
                // 페이지 인디케이터 추가 (간단한 예)
                for (index in 0 until 10) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(8.dp)
                            .background(
                                if (currentPage.value == index) Color(0xFF87CEEB) else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
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

