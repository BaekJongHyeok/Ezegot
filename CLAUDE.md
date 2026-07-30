# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고하는 프로젝트 가이드다.

## 프로젝트 개요

Ezegot은 서울 지하철 실시간 도착 정보 Android 앱이다.
개인 포트폴리오 프로젝트이며, Android 개발자 경력직 지원 시 제출할 목적으로 만들어졌다.

주요 기능: 즐겨찾기 역의 실시간 도착 정보, 근처 역 지도 표시, 역 검색,
역 상세(도착 정보 / 첫차·막차 시간표 / 위치), 열차별 도착 알람, 홈 화면 위젯.

## 기술 스택 (임의로 변경하지 말 것)

- Kotlin, Jetpack Compose + Material3, Navigation Compose
- Hilt (DI), Room (로컬 DB), Retrofit + OkHttp (네트워크)
- Coroutines + StateFlow
- Glance AppWidget + WorkManager
- Google Maps Compose
- Version Catalog (`gradle/libs.versions.toml`)
- 단일 모듈 (`:app`), compileSdk 35 / minSdk 33 / JVM 17
- Kotlin 1.9.10, Compose Compiler Extension 1.5.3, AGP 8.6.0, Gradle 8.7

## 디렉터리 구조

```
app/src/main/java/com/jonghyeok/ezegot/
├─ MyApplication.kt      # @HiltAndroidApp, WorkManager Configuration.Provider
├─ SubwayLine.kt         # 호선 ID ↔ 이름 ↔ 아이콘 매핑
├─ alarm/                # SubwayAlarmManager, SubwayAlarmWorker
├─ api/                  # Retrofit 인터페이스 및 응답 DTO
├─ db/                   # Room Entity / DAO / AppDatabase
├─ di/AppModule.kt       # Hilt 모듈 (Retrofit·Room 제공)
├─ dto/                  # 도메인 모델
├─ navigation/NavGraph.kt
├─ repository/           # Main, Station, Favorite, Search, Location
├─ ui/screen/            # Compose 화면 (Home, Station, Search, Map, Splash)
├─ ui/theme/             # Color, Theme, Type
├─ util/                 # ArrivalEstimator, NotificationHelper
├─ view/MainActivity.kt
├─ viewModel/            # Base, Main, Station, Search, Splash
└─ widget/               # Glance 위젯 + WorkManager 갱신
```

## 아키텍처 규칙

- **단방향 흐름**: Screen(Compose) → ViewModel(StateFlow) → Repository → API/Room
- ViewModel은 `BaseViewModel`을 상속하고 `@HiltViewModel`로 주입받는다.
- Repository는 `@Singleton`이며 생성자 주입으로 API 서비스와 DAO를 받는다.
- 상태는 `MutableStateFlow`(private) + `StateFlow`(public) 쌍으로 노출한다.
- 네트워크 호출은 `runCatching { withContext(Dispatchers.IO) { ... } }`로 감싸
  실패 시 빈 값을 반환한다 (UI가 죽지 않도록).

### 외부 API

같은 `SubwayApiService` 인터페이스를 서로 다른 baseUrl의 Retrofit 인스턴스에
붙여 쓰고, `@Named` 한정자로 구분한다. `di/AppModule.kt` 참고.

| `@Named` | baseUrl | 형식 | 용도 |
|---|---|---|---|
| `stationInfoApi` | `openapi.seoul.go.kr:8088` | XML | 전체 역 목록, 시간표 |
| `realtimeArrivalApi` | `swopenapi.seoul.go.kr` | XML | 실시간 도착 정보 |
| `stationLocationApi` | `t-data.seoul.go.kr` | JSON | 역 위경도 |
| `extendedApi` | `apis.data.go.kr` | JSON | TAGO 시간표 폴백 |

### 캐시 전략

- `MainRepository`: 전체 역 목록·위경도 목록을 `@Volatile` 필드에 인메모리 캐시
  (정적 데이터, 앱 생명주기 동안 유지). `StationRepository`가 이 캐시를 위임받아 쓴다.
- `StationRepository`: 실시간 도착 정보를 역 이름별 10초 캐시.
- 실시간 도착 API는 **일일 1,000건 제한**이 있다. 호출 횟수를 늘리는 변경은 신중히 할 것.

### 알려진 특이 케이스

- 실시간 도착 API는 `"서울역"`이 아니라 `"서울"`로 조회해야 데이터를 준다.
- 2호선은 상하행 대신 `"내선"` / `"외선"` 값이 온다. 방향 필터에는 두 표기를 모두 넣어야 한다.
- `barvlDt`(남은 초)가 비어 오는 경우가 많아, `RealtimeArrival.getFormattedMessage()`가
  도착 메시지 문자열을 파싱하고 `util/ArrivalEstimator`로 소요 시간을 추정한다.

### 위젯

`FavoriteRepository`가 즐겨찾기 변경 시 2단계로 위젯을 갱신한다.

1. **Phase 1** — `ArrivalWidget.writeSnapshot()`으로 역 이름만 SharedPrefs에 즉시 기록
   (suspend 아님, 로딩 표시). 곧바로 AppWidget 브로드캐스트 발송.
2. **Phase 2** — `ArrivalWidgetReceiver.triggerImmediateUpdate()`로 WorkManager를 깨워
   API 호출 후 도착 정보를 채우고 다시 브로드캐스트.

주기 갱신은 15분(`PeriodicWorkRequest`). 위젯은 최대 `MAX_FAVORITES`(3)개만 표시한다.

## 빌드

```bash
./gradlew assembleDebug
```

## 작업 원칙 (반드시 지킬 것)

1. **새 기능을 추가하지 않는다.** 현재 작업은 정리와 품질 개선이 목적이다.
2. **기존 동작을 바꾸지 않는다.** 리팩터링 전후로 화면 동작이 동일해야 한다.
   동작이 바뀌는 변경(버그 수정 포함)은 먼저 설명하고 승인을 받는다.
3. 아키텍처, 라이브러리, 패턴을 임의로 바꾸지 않는다. 지시된 것만 수행한다.
4. 변경이 큰 파일은 수정 전에 **무엇을 어떻게 바꿀지 먼저 설명하고 승인을 받는다.**
5. 각 Phase 종료 시 `./gradlew assembleDebug`가 통과해야 한다.
6. 커밋 메시지는 한국어로, 무엇을 왜 바꿨는지 드러나게 쓴다.

## 주석·문서 스타일

- 주석은 한국어로 쓴다.
- 섹션 구분에 `// ── 제목 ─────` 형태의 구분선을 쓰는 관례가 있다. 유지할 것.
- 비자명한 결정(캐시 전략, 두 단계 위젯 갱신, debounce 등)에는 이유를 적는다.
