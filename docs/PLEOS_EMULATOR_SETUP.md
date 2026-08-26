# PLEOS Connect Emulator 설치 상태

## 결론

`pleosDebug` APK는 빌드되지만 현재 설치된 일반 Android Automotive AVD에서는 PLEOS Vehicle·NaviHelper 기능을 실행할 수 없습니다. PLEOS Connect system image에 포함된 Navi Service와 VHAL이 필요하기 때문입니다.

PLEOS는 보안 정책상 system image 업데이트 사이트 URL을 공개 문서에 제공하지 않습니다. 공식 안내에 따라 `partnership@pleos.ai`로 URL을 요청해야 합니다.

- [공식 Emulator 설치 문서](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator#installing-the-pleos-connect-emulator)
- [공식 개발환경 요구사항](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-application-development-environment)

## 2026-08-26 설치 시도 결과

| 항목 | 상태 |
|---|---|
| Android SDK Platform 34 | 설치 완료 |
| Android SDK Platform 36 | 설치 완료 |
| Android Emulator / Platform Tools | 설치 완료 |
| 일반 Automotive AVD `LAB_EV_Demo` | 생성 및 전체 UI 흐름 검증 완료 |
| PLEOS SDK Maven 의존성 | 다운로드 및 컴파일 완료 |
| `demoDebug` APK | 빌드·설치·실행 완료 |
| `pleosDebug` APK | 빌드 완료 |
| Pleos Connect SDK Update Site | 비공개 URL 미수령 |
| Pleos Connect API 34 system image | 미설치 |
| Pleos Connect v2.0 AVD | system image 수령 후 생성 가능 |

로그인된 LAB EV Playground 프로젝트 화면에서도 system image 다운로드 URL은 제공되지 않았습니다. 프로젝트 키, 클라이언트 시크릿, CRN 등의 값은 저장소에 기록하지 않습니다.

## 다운로드 URL 요청

아래 내용을 참고해 PLEOS 파트너 담당자에게 system image 업데이트 사이트 URL을 요청합니다.

```text
수신: partnership@pleos.ai
제목: Pleos Connect Emulator system image 다운로드 URL 요청

안녕하세요.
Pleos Playground에서 LAB EV 차량용 Android Automotive 앱을 개발하고 있습니다.
Vehicle SDK와 NaviHelper SDK의 실제 동작 검증을 위해 Pleos Connect Emulator
system image 다운로드 URL과 권장 SDK 대응 버전을 요청드립니다.

프로젝트명: LAB EV
사용 목적: Vehicle 상태 조회 및 NaviHelper 경로 안내 기능 개발·검증
```

## URL 수령 후 설치 절차

1. Android Studio에서 **Tools → SDK Manager**를 엽니다.
2. **SDK Update Sites** 탭에서 `Add`를 선택합니다.
3. 이름을 `Pleos Connect System Image`로 입력합니다.
4. PLEOS에서 전달받은 비공개 URL을 입력합니다.
5. **SDK Platforms**에서 Android API 34를 선택하고 **Show Package Details**를 켭니다.
6. `Pleos Connect system image`를 선택해 설치합니다.
7. Android Studio를 재시작합니다.
8. **Device Manager → Create Device → Automotive**로 이동합니다.
9. 하드웨어와 system image에서 각각 `Pleos Connect v2.0`을 선택합니다.
10. Graphics를 `Hardware`, Boot option을 `Cold boot`로 설정합니다.
11. LLM 테스트가 필요하면 공식 문서 이미지에 안내된 RAM 크기를 적용합니다.
12. AVD를 생성하고 실행합니다.

비공개 URL은 문서, 소스, Gradle 파일 또는 Git 커밋에 저장하지 않습니다. Android Studio의 로컬 SDK Update Site 설정에만 등록합니다.

## 앱 설치와 프로젝트 연결

PLEOS AVD가 실행된 뒤 다음 순서로 검증합니다.

```powershell
.\gradlew.bat assemblePleosDebug
android install --device=<PLEOS_DEVICE_ID> --apks="app\build\outputs\apk\pleos\debug\app-pleos-debug.apk"
```

Playground 프로젝트의 테스트 정보에서 CRN 설정 명령을 복사해 PLEOS devbox에 적용합니다. CRN 원문은 저장소에 커밋하지 않습니다. 앱 권한은 Playground 프로젝트와 Pleos App Market 앱을 연결한 뒤 승인 범위 안에서 사용해야 합니다.

## 실제 기능 검증 체크리스트

- [ ] `Vehicle.initialize()` 서비스 연결 성공
- [ ] EV 배터리 초기값 및 변경 callback 수신
- [ ] 기어 P/D 상태 callback 수신
- [ ] `NaviHelper.initialize()` 연결 callback 성공
- [ ] Site B `requestRoute()` 요청 성공
- [ ] 주행 거리와 ETA callback 수신
- [ ] 목적지 도착 callback으로 Field 단계 전환
- [ ] 앱 종료 시 listener 제거와 SDK `release()` 확인
- [ ] Gleo AI 권한 승인 후 LLM fallback을 실제 호출로 교체

## 현재 기능별 실행 가능 범위

| 기능 | 일반 AVD | PLEOS AVD 준비 후 |
|---|---|---|
| Mission UI와 상태 전이 | 실행 가능 | 실행 가능 |
| PM2.5·온도·습도 mock stream | 실행 가능 | 실행 가능 |
| 샘플 AI 분석과 보고서 | 실행 가능 | 실행 가능 |
| Vehicle 배터리·기어 SDK | 서비스 부재로 실행 불가 | 실행 검증 예정 |
| NaviHelper 경로 안내 SDK | Navi Service 부재로 실행 불가 | 실행 검증 예정 |
| Gleo AI 실제 호출 | 권한 미승인 | 승인 후 검증 가능 |
