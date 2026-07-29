package com.jonghyeok.ezegot.viewModel

/**
 * 현재 위치 조회 상태.
 *
 * 위치를 못 얻었을 때 화면이 기본 좌표에 조용히 머무르지 않도록,
 * "확인 중"과 "실패"를 값으로 구분해 UI가 표현할 수 있게 한다.
 */
sealed interface LocationState {

    /** 조회 중. 아직 성공도 실패도 아니다. */
    data object Loading : LocationState

    /** 위치 확보. */
    data class Available(val latitude: Double, val longitude: Double) : LocationState

    /**
     * 위치를 얻지 못함.
     *
     * 권한이 없거나, GPS가 꺼져 있거나, 제한 시간 안에 첫 fix가 오지 않은 경우다.
     * 셋을 구분하지 않는 이유는 사용자가 취할 행동(설정 확인 후 재시도)이 같기 때문이다.
     */
    data object Unavailable : LocationState
}
