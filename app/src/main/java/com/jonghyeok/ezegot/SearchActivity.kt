package com.jonghyeok.ezegot

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jonghyeok.ezegot.api.StationInfoDTO
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme

lateinit var allStationsWithLine: List<StationInfoDTO>

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    SearchContent(modifier = Modifier.padding(paddingValues))
                }
            }
        }

        allStationsWithLine = getStationListFromPreferences()
    }

    private fun getStationListFromPreferences(): List<StationInfoDTO> {
        val sharedPreferences: SharedPreferences = getSharedPreferences("default", MODE_PRIVATE)
        val json = sharedPreferences.getString("stationList", null)

        return if (json != null) {
            val gson = Gson()
            val stationListType = object : TypeToken<List<StationInfoDTO>>() {}.type
            gson.fromJson(json, stationListType)
        } else {
            emptyList() // 예외 처리: 데이터가 없으면 빈 리스트 반환
        }
    }

    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
        // 뒤로가기 애니메이션 설정
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

@Composable
fun SearchContent(modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus() // 포커스를 자동으로 요청
        keyboardController?.show()  // 키보드를 표시
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(App_Background_Color),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchScreen(focusRequester = focusRequester)
    }
}

@Composable
fun SearchScreen(focusRequester: FocusRequester) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    val lifecycleOwner = rememberUpdatedState(LocalContext.current as androidx.lifecycle.LifecycleOwner)
    LaunchedEffect(lifecycleOwner.value.lifecycle) {
        lifecycleOwner.value.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                super.onResume(owner)
                textState = TextFieldValue("") // 돌아왔을 때 초기화
            }
        })
    }

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
            SearchTextBar(focusRequester = focusRequester, textState = textState, onTextChange = { textState = it })

            // 최근검색 리스트
            RecentSearchResult()
        }
    }
}

@Composable
fun SearchTextBar(focusRequester: FocusRequester, textState: TextFieldValue, onTextChange: (TextFieldValue) -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 입력된 텍스트로 필터링된 지하철역 리스트
    val filteredStations = allStationsWithLine.filter {
        it.stationName.contains(textState.text, ignoreCase = true)
    }

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
                .clickable {
                    focusRequester.requestFocus() // 포커스 요청
                    keyboardController?.show() // 키보드 띄우기
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(color = Color(0xFFFFFFFF)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = textState, // 현재 상태를 value로 전달
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .focusRequester(focusRequester), // FocusRequester 적용
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // 키보드 내리기 (옵션)
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF868686)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (textState.text.isEmpty()) {
                                Text(
                                    text = "지하철 역 이름 검색",
                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFFB0B0B0))
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search Icon",
                )
            }
        }
    }

    // 입력된 텍스트가 없을 때는 리스트를 표시하지 않음
    if (textState.text.isNotEmpty() && filteredStations.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clickable {}
        ) {
            items(filteredStations) { searchItem ->
                SearchItemRow(
                    searchItem = searchItem,
                    onClick = {
                        // 아이템 클릭 시 StationActivity로 이동
                        val intent = Intent(context, StationActivity::class.java).apply {
                            putExtra("station_name", searchItem.stationName)
                            putExtra("line", searchItem.lineNumber)
                        }
                        // 최근 검색에 추가
                        val recentSearchItem = RecentSearchItem(
                            stationName = searchItem.stationName,
                            line = searchItem.lineNumber,
                            date = getCurrentDate()
                        )
                        saveRecentSearch(recentSearchItem, context) // 최근 검색 저장 함수 호출
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun SearchItemRow(searchItem: StationInfoDTO, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 역 이름과 호선은 왼쪽에 배치
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = searchItem.stationName,
                style = TextStyle(color = Color(0xFF868686), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = searchItem.lineNumber,
                style = TextStyle(color = Color(0xFF868686), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}

fun getCurrentDate(): String {
    val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return currentDate.format(java.util.Date())
}

fun saveRecentSearch(newSearchItem: RecentSearchItem, context: Context) {
    val sharedPreferences = context.getSharedPreferences("RecentSearchPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()

    // 저장된 최근 검색 목록 가져오기
    val recentSearchJson = sharedPreferences.getString("recentSearchList", "[]")
    val recentSearchListType = object : TypeToken<MutableList<RecentSearchItem>>() {}.type
    val recentSearchList: MutableList<RecentSearchItem> = gson.fromJson(recentSearchJson, recentSearchListType)

    // 기존 리스트에서 중복된 항목 제거 (중복 방지)
    recentSearchList.removeAll { it.stationName == newSearchItem.stationName && it.line == newSearchItem.line }

    // 새로운 검색 기록을 리스트의 가장 앞에 추가
    recentSearchList.add(0, newSearchItem)

    // 리스트를 다시 저장
    val updatedJson = gson.toJson(recentSearchList)
    editor.putString("recentSearchList", updatedJson)
    editor.apply()
}


@Composable
fun RecentSearchResult() {
    val context = LocalContext.current
    val recentSearches = remember { mutableStateListOf<RecentSearchItem>() }

    fun refreshRecentSearches() {
        recentSearches.clear()
        recentSearches.addAll(loadRecentSearches(context))
    }

    val lifecycleOwner = rememberUpdatedState(LocalContext.current as androidx.lifecycle.LifecycleOwner)
    LaunchedEffect(lifecycleOwner.value.lifecycle) {
        lifecycleOwner.value.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                super.onResume(owner)
                refreshRecentSearches()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = "최근검색",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(recentSearches) { searchItem ->
                RecentSearchItemRow(
                    recentSearchItem = searchItem,
                    onDelete = {
                        recentSearches.remove(searchItem)
                        saveRecentSearchList(recentSearches, context)
                    },
                    onClick = {
                        // StationActivity로 이동
                        val intent = Intent(context, StationActivity::class.java).apply {
                            putExtra("station_name", searchItem.stationName)
                            putExtra("line", searchItem.line)
                        }
                        // 최근 검색에 추가
                        val recentSearchItem = RecentSearchItem(
                            stationName = searchItem.stationName,
                            line = searchItem.line,
                            date = getCurrentDate()
                        )
                        saveRecentSearch(recentSearchItem, context) // 최근 검색 저장 함수 호출
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

fun loadRecentSearches(context: Context): List<RecentSearchItem> {
    val sharedPreferences = context.getSharedPreferences("RecentSearchPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val recentSearchJson = sharedPreferences.getString("recentSearchList", "[]")
    val recentSearchListType = object : TypeToken<List<RecentSearchItem>>() {}.type
    val recentSearchList: List<RecentSearchItem> = gson.fromJson(recentSearchJson, recentSearchListType)

    // 최근 검색 기록을 최신순으로 반환
    return recentSearchList
}

fun saveRecentSearchList(updatedList: List<RecentSearchItem>, context: Context) {
    val sharedPreferences = context.getSharedPreferences("RecentSearchPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val updatedJson = gson.toJson(updatedList)
    editor.putString("recentSearchList", updatedJson)
    editor.apply()
}

@Composable
fun RecentSearchItemRow(recentSearchItem: RecentSearchItem, onDelete: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(20.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 역 이름과 호선은 왼쪽에 배치
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = recentSearchItem.stationName,
                style = TextStyle(color = Color(0xFF868686), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = recentSearchItem.line,
                style = TextStyle(color = Color(0xFF868686), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }

        // 검색일은 오른쪽에 배치
        Row(
            modifier = Modifier
                .padding(end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End // 오른쪽에 배치
        ) {
            Text(
                text = recentSearchItem.date,
                style = TextStyle(color = Color(0xFF868686), fontSize = 14.sp, fontWeight = FontWeight.Light)
            )
        }

        // 삭제 버튼 (X 이미지)
        Image(
            modifier = Modifier
                .size(14.dp)
                .clickable { onDelete() },
            painter = painterResource(id = R.drawable.ic_x), // "X" 이미지 리소스
            contentDescription = "Delete"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    Egegot_mkTheme {
        SearchScreen(focusRequester = remember { FocusRequester() })
    }
}
