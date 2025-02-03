package com.jonghyeok.ezegot.view

import android.content.Intent
import android.os.Build
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.RecentSearchItem
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.TitleBar
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.modelFactory.SearchViewModelFactory
import com.jonghyeok.ezegot.repository.SearchRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.SearchViewModel

class SearchActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(SearchRepository(SharedPreferenceManager(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = App_Background_Color
                ) {
                    SearchScreen(viewModel)
                }
            }
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
fun SearchScreen(viewModel: SearchViewModel) {
    val context = LocalContext.current
    val recentSearches: List<RecentSearchItem> by viewModel.recentSearches.collectAsState()
    val filteredStations by viewModel.filteredStations.collectAsState()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus() // 포커스를 자동으로 요청
        keyboardController?.show()  // 키보드를 표시
        viewModel.loadRecentSearches()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 타이틀 바
        TitleBar()

        // 검색바
        SearchTextBar(viewModel,
            textState = textState,
            onTextChange = { textState = it },
            filteredStations = filteredStations,
            focusRequester = focusRequester,
            keyboardController = keyboardController
        )

        // 최근검색 리스트
        RecentSearchResult(
            recentSearches,
            onDelete = { searchItem -> viewModel.removeRecentSearch(searchItem) },
            onSearchItemClick = { searchItem ->
                val intent = Intent(context, StationActivity::class.java).apply {
                    putExtra("station_name", searchItem.stationName)
                    putExtra("line", searchItem.lineNumber)
                }
                context.startActivity(intent)
                viewModel.saveRecentSearch(searchItem)
            }
        )
    }
}

@Composable
fun SearchTextBar(
    viewModel: SearchViewModel,
    textState: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    filteredStations: List<StationInfo>,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
) {
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
                    onValueChange = { newText ->
                        onTextChange(newText)
                        viewModel.filterStations(newText.text)
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .focusRequester(focusRequester), // FocusRequester 적용,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF868686)),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.weight(1f)) {
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
                        val intent = Intent(context, StationActivity::class.java).apply {
                            putExtra("station_name", searchItem.stationName)
                            putExtra("line", searchItem.lineNumber)
                        }
                        context.startActivity(intent)
                        viewModel.saveRecentSearch(RecentSearchItem(searchItem.stationName, searchItem.lineNumber, getCurrentDate()))
                    }
                )
            }
        }
    }
}

@Composable
fun SearchItemRow(searchItem: StationInfo, onClick: () -> Unit) {
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

@Composable
fun RecentSearchResult(recentSearches: List<RecentSearchItem>, onDelete: (RecentSearchItem) -> Unit, onSearchItemClick: (RecentSearchItem) -> Unit) {
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
                    onDelete = { onDelete(searchItem) },
                    onClick = { onSearchItemClick(searchItem) }
                )
            }
        }
    }
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
                text = recentSearchItem.lineNumber,
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

fun getCurrentDate(): String {
    val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return currentDate.format(java.util.Date())
}

