# Ezegot

기존에 만들었던 realtime 지하철 어플을 기반으로 하여 새로 개발한 대중교통 알림 앱

## 실행 준비

이 프로젝트는 공공 API 키를 소스에 두지 않는다. 빌드 전에 프로젝트 루트의
`local.properties`에 아래 키를 설정해야 한다. (`local.properties`는 `.gitignore` 대상)

`local.properties.example`을 `local.properties`로 복사한 뒤 값을 채우면 된다.

| 키 | 발급처 | 용도 |
|---|---|---|
| `SEOUL_OPEN_API_KEY` | [서울 열린데이터광장](https://data.seoul.go.kr) | 전체 역 목록, 실시간 도착 정보 |
| `SEOUL_TIMETABLE_API_KEY` | [서울 열린데이터광장](https://data.seoul.go.kr) | 역별 시간표 (첫차·막차) |
| `TAIMS_API_KEY` | [서울 교통 데이터](https://t-data.seoul.go.kr) | 역 위경도 |
| `DATA_GO_KR_SERVICE_KEY` | [공공데이터포털](https://www.data.go.kr) TAGO_지하철정보 | 시간표 폴백 (코레일 등) |
| `MAPS_API_KEY` | [Google Cloud Console](https://console.cloud.google.com) Maps SDK for Android | 지도 표시 |

```properties
SEOUL_OPEN_API_KEY=발급받은_키
SEOUL_TIMETABLE_API_KEY=발급받은_키
TAIMS_API_KEY=발급받은_키
DATA_GO_KR_SERVICE_KEY=발급받은_Encoding_키
MAPS_API_KEY=발급받은_키
```

키 값은 `app/build.gradle.kts`에서 주입된다. 공공 API 키는 `BuildConfig` 필드로,
`MAPS_API_KEY`는 지도 SDK가 매니페스트에서 직접 읽으므로 `manifestPlaceholders`로 들어간다.
키가 비어 있어도 빌드는 통과하지만 해당 기능은 동작하지 않는다.

`DATA_GO_KR_SERVICE_KEY`는 공공데이터포털이 주는 **Encoding 키**를 그대로 넣는다
(`%2F`, `%2B`, `%3D` 포함). Retrofit이 `encoded = true`로 전달하므로 재인코딩하지 않는다.

## 보안

### API 키 관리

모든 API 키는 저장소에 커밋하지 않는다. 프로젝트 루트의 `local.properties`
(`.gitignore` 대상)에 두고 `app/build.gradle.kts`가 빌드 시점에 주입한다.

| 대상 | 주입 경로 | 코드에서 읽는 곳 |
|---|---|---|
| 공공 API 키 4종 | `local.properties` → `buildConfigField` → `BuildConfig` | `di/AppModule`의 `provideApiKeys()` 한 곳 |
| `MAPS_API_KEY` | `local.properties` → `manifestPlaceholders` → `AndroidManifest` | 지도 SDK가 매니페스트 `meta-data`에서 직접 읽음 |

`BuildConfig`를 참조하는 코드는 `AppModule` 하나뿐이다. Repository는 키 문자열
대신 `ApiKeys` 객체를 주입받으므로, 인증 정보가 데이터 계층 시그니처에
노출되지 않는다.

빌드에 필요한 키 목록과 발급처는 `local.properties.example`에 정리되어 있다.
키가 비어 있어도 빌드는 통과하며, 해당 기능만 동작하지 않는다.

### 사용 중인 Google API

`MAPS_API_KEY`가 실제로 필요한 것은 **Maps SDK for Android** 하나다.
위치 조회에 쓰는 `FusedLocationProviderClient`와 주소 변환에 쓰는
`android.location.Geocoder`는 Android 프레임워크/Play 서비스 API라
이 키를 사용하지 않는다.

## 빌드

```bash
./gradlew assembleDebug
```

JDK 17이 필요하다.
