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

`MAPS_API_KEY`는 노출 시 실제 과금이 발생할 수 있다. Google Cloud 콘솔에서
패키지명과 SHA-1 지문으로 사용 제한을 걸어두는 것을 권장한다.

## 빌드

```bash
./gradlew assembleDebug
```

JDK 17이 필요하다.
