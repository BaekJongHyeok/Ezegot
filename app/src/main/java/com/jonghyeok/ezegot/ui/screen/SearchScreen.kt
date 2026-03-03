package com.jonghyeok.ezegot.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.ui.theme.*
import com.jonghyeok.ezegot.viewModel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    val textState by viewModel.textState.collectAsState()
    val filteredStations by viewModel.filteredStations.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val allStations by viewModel.allStationsInfoList.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // ── Header ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy900, Navy800)))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TextOnDark)
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Navy700
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = SkyBlue400,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (textState.text.isEmpty()) {
                                Text(
                                    text = "지하철 역 이름 검색",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextHint
                                )
                            }
                            BasicTextField(
                                value = textState,
                                onValueChange = { v ->
                                    viewModel.onTextChange(v)
                                    // debounce 파이프라인이 filter 자동 처리
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextOnDark),
                                cursorBrush = SolidColor(SkyBlue400),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    keyboardController?.hide()
                                    val match = allStations.firstOrNull {
                                        it.stationName.equals(textState.text, ignoreCase = true)
                                    }
                                    if (match != null) {
                                        viewModel.saveRecentSearch(match.stationName, match.lineNumber)
                                        onStationClick(match.stationName, match.lineNumber)
                                    } else {
                                        Toast.makeText(context, "검색과 일치하는 역이 없습니다", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            )
                        }
                        if (textState.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onTextChange(TextFieldValue(""))
                                    // debounce가 빈 쿼리를 처리 → filteredStations 초기화
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "지우기", tint = TextHint)
                            }
                        }
                    }
                }
            }
        }

        // ── 검색 결과 or 최근검색 ────────────────────────────────
        if (textState.text.isNotEmpty() && filteredStations.isNotEmpty()) {
            SearchResultList(
                stations = filteredStations,
                onItemClick = { station ->
                    viewModel.saveRecentSearch(station.stationName, station.lineNumber)
                    onStationClick(station.stationName, station.lineNumber)
                }
            )
        } else if (textState.text.isEmpty()) {
            RecentSearchList(
                recentSearches = recentSearches,
                onItemClick = { item -> onStationClick(item.stationName, item.lineNumber) },
                onDelete = { item -> viewModel.deleteRecentSearch(item.stationName, item.lineNumber) }
            )
        }
    }
}

@Composable
fun SearchResultList(stations: List<StationInfo>, onItemClick: (StationInfo) -> Unit) {
    LazyColumn {
        items(stations) { station ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(station) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
                    Text(text = station.stationName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                }
                val lineColor = getSubwayLineColor(station.lineNumber)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = lineColor
                ) {
                    Text(
                        text = station.lineNumber.removePrefix("0"),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
fun RecentSearchList(
    recentSearches: List<BasicStationInfo>,
    onItemClick: (BasicStationInfo) -> Unit,
    onDelete: (BasicStationInfo) -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        if (recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 검색",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        LazyColumn {
            items(recentSearches) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
                        Column {
                            Text(text = item.stationName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = item.lineNumber.removePrefix("0"),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextHint
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "삭제", tint = TextHint, modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}
