# PLEOS SDK integration map — v0.1

앱은 `demo`와 `pleos` 제품 모드로 분리되어 있습니다. ViewModel은 네 repository 인터페이스만 알고, 제품 모드별 `RepositoryFactory`가 구현을 선택합니다. 인증 정보와 Playground 비밀키는 소스에 저장하지 않습니다.

| 앱 경계 | v0.1 | PLEOS adapter 책임 |
|---|---|---|
| `VehicleRepository` | 배터리·P·GPS·장비 상태 mock | `Vehicle(context)`의 EV 배터리·기어 조회/리스너를 StateFlow로 변환하고 `release()` |
| `NavigationRepository` | 8.4 km 경로 및 도착 버튼 | `NaviHelper(context)` → `initialize()` → `addListener()` → `requestRoute(RouteInfo)` → 주행/도착 callback → `removeListener()`/`release()` |
| `SensorRepository` | PM2.5·온도·습도 Flow | LAB EV 외부 장비의 USB/BLE/Ethernet adapter. PLEOS Vehicle SDK 데이터가 아님 |
| `AiRepository` | 안전한 고정 분석·보고 문구 | 현재 Playground Gleo AI 사용 승인이 필요하므로 PLEOS 모드에서도 mock fallback. 승인 후 `LLM(context)` 수명주기를 이 경계 안에 연결 |

## 현재 소스 위치

```text
app/src/main/.../data/Repositories.kt
app/src/demo/.../data/RepositoryFactory.kt
app/src/pleos/.../data/RepositoryFactory.kt
app/src/pleos/.../data/PleosRepositories.kt
```

Maven 저장소는 `https://nexus-playground.pleos.ai/repository/maven-releases/`이며 현재 고정한 버전은 Vehicle `2.0.3`, NaviHelper `2.0.3`, LLM `2.1.3.2`입니다. PLEOS 권한은 `src/pleos/AndroidManifest.xml`에만 들어가므로 일반 데모 APK에는 포함되지 않습니다.

## Playground 프로젝트 상태

- Vehicle Data: 요청한 10개 항목 사용 가능
- NaviHelper: 경로 정보/조회, 경로 탐색 설정 사용 가능
- Gleo AI LLM/STT/TTS: 사용 신청 필요
- 앱 연결과 PLEOS Connect 전용 시스템 이미지가 준비되기 전에는 `demoDebug`를 사용

PLEOS Connect 에뮬레이터 이미지는 공개 Android 시스템 이미지가 아니므로 파트너 제공 이미지가 필요합니다. 현재 설치된 `LAB_EV_Demo` AVD는 UI·상태 흐름을 검증하는 Android Automotive 형태의 일반 에뮬레이터입니다.

설치 시도 결과와 URL 수령 후 절차는 [PLEOS_EMULATOR_SETUP.md](PLEOS_EMULATOR_SETUP.md)에 정리했습니다.

## SDK 버전 주의사항

현재 프로젝트는 초기 공개 API 문서에 맞춰 Vehicle `2.0.3`, NaviHelper `2.0.3`으로 구현되어 있습니다. 최신 PLEOS Connect Emulator 개발환경 문서는 Vehicle `2.2.9`, NaviHelper `2.2.7`을 안내합니다. 전용 이미지 수령 후 이미지와 SDK의 대응 버전을 먼저 확인하고, 필요하면 adapter를 최신 API로 갱신해야 합니다.

## AI prompt guardrail

실제 `generateContent()` 프롬프트에는 측정값·시간·Site·현장 메모를 구조화해 넣고 다음 제약을 유지합니다.

> 관찰된 차이만 설명한다. 측정값과 현장 메모의 동시 관찰을 인과관계로 표현하지 않는다. 데이터가 부족하면 반복 측정을 권고한다.

## 공식 문서

- [PLEOS Connect SDK 개요](https://pleos.ai/playground/resources/en/api-reference/connect-sdk)
- [Vehicle SDK 소개](https://pleos.ai/playground/resources/api-reference/connect-sdk-pleos/Vehicle/intro)
- [NaviHelper SDK 소개](https://document.pleos.ai/en/api-reference/connect-sdk-pleos/NaviHelper/intro)
- [NaviHelper 첫 API 호출](https://document.pleos.ai/en/api-reference/connect-sdk-pleos/NaviHelper/make-the-first-api-call)
- [LLM SDK 소개](https://document.pleos.ai/api-reference/connect-sdk-pleos/LLM/intro)
- [LLM 첫 API 호출](https://pleos.ai/playground/resources/api-reference/connect-sdk/LLM/make-the-first-api-call)
- [PLEOS Connect Emulator 설치](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)
- [PLEOS 개발환경 요구사항](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-application-development-environment)
