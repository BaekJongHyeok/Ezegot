package com.jonghyeok.ezegot.viewModel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.MainRepository
import com.jonghyeok.ezegot.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val mainRepository: MainRepository
) : BaseViewModel() {

    /** 최근 검색 – Room Flow 자동 갱신 */
    val recentSearches: StateFlow<List<BasicStationInfo>> = searchRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── 검색어 상태 ───────────────────────────────────────────────
    private val _textState = MutableStateFlow(TextFieldValue(""))
    val textState: StateFlow<TextFieldValue> = _textState.asStateFlow()

    // ── 필터링 결과 ───────────────────────────────────────────────
    private val _filteredStations = MutableStateFlow<List<StationInfo>>(emptyList())
    val filteredStations: StateFlow<List<StationInfo>> = _filteredStations.asStateFlow()

    // ── 내부 검색어 String Flow (debounce 적용) ───────────────────
    /**
     * 사용자가 빠르게 타이핑할 때 매 글자마다 filter를 실행하는 것을 방지한다.
     *
     * debounce(200ms): 200ms 동안 추가 입력이 없으면 filter 실행
     * distinctUntilChanged: 이전과 동일한 검색어면 filter 스킵
     *
     * 예: "강남역" 빠르게 입력 → "강", "강남", "강남역" 중
     *     200ms 내 연속 입력이면 마지막 "강남역"만 filter
     */
    private val _searchQuery = MutableStateFlow("")

    init {
        loadAllStations()

        // debounce 파이프라인 설정
        viewModelScope.launch {
            _searchQuery
                .debounce(200L)
                .distinctUntilChanged()
                .collect { query -> runFilter(query) }
        }
    }

    private fun loadAllStations() {
        viewModelScope.launch {
            // MainRepository @Singleton 캐시 – 첫 호출 이후 API 없음
            setAllStations(mainRepository.getAllStations())
        }
    }

    /** 텍스트 변경 시 호출 – debounce가 실제 filter를 지연 처리 */
    fun onTextChange(newText: TextFieldValue) {
        _textState.value = newText
        _searchQuery.value = newText.text   // → debounce 파이프라인으로 전달
    }

    private fun runFilter(query: String) {
        _filteredStations.value = if (query.isBlank()) emptyList()
        else allStationsInfoList.value.filter { it.stationName.contains(query, ignoreCase = true) }
    }

    fun saveRecentSearch(stationName: String, lineNumber: String) {
        viewModelScope.launch { searchRepository.saveRecentSearch(stationName, lineNumber) }
    }

    fun deleteRecentSearch(stationName: String, lineNumber: String) {
        viewModelScope.launch { searchRepository.deleteRecentSearch(stationName, lineNumber) }
    }
}