TripPing-App
📁 프로젝트 구조
![프로젝트 구조](./docs/tripping_app_folder-structure.png)
```
com.tripping.app/
├── auth/               # 로그인, 회원가입, 인증(토큰) 관련 코드
├── data/
│   ├── api/            # 서버 API 통신 인터페이스 (Retrofit)
│   ├── request/        # 서버에 보내는 요청 데이터 클래스 (DTO)
│   └── response/       # 서버에서 받는 응답 데이터 클래스 (DTO)
├── ui/
│   ├── screen/          # 화면 단위 Composable (기존 Fragment 역할)
│   ├── component/       # 여러 화면에서 재사용하는 UI 조각 (버튼, 카드 등)
│   └── theme/           # 앱 전체 색상, 폰트, 테마 설정
├── viewmodel/           # 화면 상태 관리 및 클릭 이벤트 처리 로직
└── MainActivity.kt      # 앱 진입점
```
## 📌 폴더별 작업 가이드

### `auth/` — 로그인, 회원가입
로그인 화면, 회원가입 화면, 그리고 로그인 후 받은 토큰을 저장/관리하는 코드를 여기에 넣어요.

```kotlin
// auth/LoginScreen.kt
@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit) {
    // 이메일, 비밀번호 입력받는 화면
}
```

---

### `data/api/` — 서버 주소 정의
"어떤 주소로, 어떤 방식(GET/POST)으로, 뭘 보내고 뭘 받을지"를 정의하는 곳이에요. Retrofit이 이 정의를 보고 실제 통신을 대신 해줘요.

```kotlin
// data/api/AuthApi.kt
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
```

---

### `data/request/` — 서버로 보낼 데이터 모양
서버에 뭔가 요청할 때, "이런 형태로 데이터를 보낼게요"라고 정의하는 곳이에요.

```kotlin
// data/request/LoginRequest.kt
data class LoginRequest(
    val email: String,
    val password: String
)
```

---

### `data/response/` — 서버가 보내줄 데이터 모양
반대로, 서버가 "이런 형태로 응답을 돌려줄게요"라고 약속한 걸 정의하는 곳이에요.

```kotlin
// data/response/LoginResponse.kt
data class LoginResponse(
    val message: String
)
```

---

### `ui/screen/` — 화면 하나하나
사용자가 실제로 보는 화면들이에요. 홈 화면, 마이페이지, 검색 화면처럼 "화면 단위"는 다 여기 넣어요.

```kotlin
// ui/screen/HomeScreen.kt
@Composable
fun HomeScreen() {
    // 홈 화면 전체 레이아웃
}
```

---

### `ui/component/` — 여러 화면에서 재사용하는 작은 조각
버튼, 카드, 프로필 사진처럼 "여러 화면에서 반복해서 쓰는 UI 부품"을 여기 모아두면, 나중에 디자인 바꿀 때 한 곳만 고치면 돼요.

```kotlin
// ui/component/PlaceCard.kt
@Composable
fun PlaceCard(name: String, imageUrl: String) {
    // 장소 하나를 보여주는 카드 UI
}
```

---

### `ui/theme/` — 앱 전체 디자인 톤
앱 전체에서 쓰는 색상, 폰트 크기, 다크모드 여부 같은 걸 관리해요. (안드로이드 스튜디오가 프로젝트 만들 때 자동으로 만들어줘요)

---

### `viewmodel/` — 화면에서 일어나는 일 처리
버튼을 눌렀을 때 뭘 할지, 서버에서 데이터를 받아와서 화면에 어떻게 뿌려줄지 같은 "동작"을 여기서 관리해요. 화면(`ui/screen`)은 그냥 보여주기만 하고, 실제 로직은 여기서 처리하는 게 원칙이에요.

```kotlin
// viewmodel/HomeViewModel.kt
class HomeViewModel : ViewModel() {
    fun onPlaceClick(place: Place) {
        // 장소 카드를 눌렀을 때 할 일
    }
}
```
## 🛠 기술 스택
Kotlin + Jetpack Compose
Retrofit (서버 통신)
(팀원분들이 추가로 사용하는 라이브러리 있으면 여기 추가해주세요)
🚀 시작하기
```bash
git clone https://github.com/TriponPing/TripPing-App.git
```
안드로이드 스튜디오에서 프로젝트 열고 Gradle Sync 후 실행
## 🔧 서버 연결 시 주의사항

로컬에서 실행 중인 백엔드 서버(`localhost:8080`)에 앱에서 접속하려면, 테스트하는 환경에 따라 주소를 다르게 써야 해요. `localhost`를 그대로 쓰면 둘 다 연결 안 됩니다!

### 1. 에뮬레이터로 테스트할 때

`localhost` 대신 `10.0.2.2`를 사용하세요. (구글이 정해놓은 에뮬레이터 전용 특수 주소로, "에뮬레이터를 실행 중인 내 PC"를 가리켜요)

```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"
```

별도 설정 없이 그대로 쓰면 됩니다.

### 2. 실제 스마트폰으로 테스트할 때

1. **PC와 스마트폰을 같은 Wi-Fi에 연결**하세요. (다른 네트워크면 절대 안 됩니다)
2. PC에서 터미널(PowerShell)을 열고 아래 명령어를 입력해서 내 PC의 IP 주소를 확인하세요:
```bash
   ipconfig
```
3. 결과에서 **무선 LAN 어댑터 Wi-Fi** 항목 아래 **IPv4 주소**를 찾으세요. (예: `192.168.0.15`)
4. 이 주소를 `BASE_URL`에 넣으세요:
```kotlin
   const val BASE_URL = "http://192.168.0.15:8080/"  // 본인 IPv4 주소로 교체
```

> ⚠️ Wi-Fi를 재연결하거나 PC를 재부팅하면 IP가 바뀔 수 있어요. 연결이 갑자기 안 되면 `ipconfig`로 IP를 다시 확인해보세요.

### 요약

| 테스트 환경 | 써야 할 주소 |
|---|---|
| 에뮬레이터 | `10.0.2.2` (고정, 항상 이 값) |
| 실제 스마트폰 | 내 PC의 IPv4 주소 (`ipconfig`로 매번 확인) |


👥 브랜치 전략
`main`: 배포/안정 브랜치
`feat/기능이름`: 기능별 개발 브랜치
작업 전 `git pull origin main`으로 최신 내용 받아오기