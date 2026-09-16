package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripping.app.R
import com.tripping.app.data.response.CurrentTripSummaryResponse
import com.tripping.app.data.response.PopularPlaceResponse
import com.tripping.app.data.response.PopularTripResponse
import com.tripping.app.ui.model.HomeCourseCard
import com.tripping.app.ui.model.HomeKeyword
import com.tripping.app.viewmodel.HomeViewModel
import com.tripping.app.viewmodel.PlaceCategory

// ===== 색상 (피그마 파일 MRryaRzdtZqvbGJf07TqMk, node 19:2 / 108:793 / 74:3178 / 88:3521 에서 추출) =====
// 다른 화면 파일들처럼 이 파일에서만 쓰는 색은 로컬로 선언함.
// 단, HomeCourseCardView 등 일부는 인기 키워드/내 주변 코스 화면에서도 재사용하므로 private을 붙이지 않음.
internal val HomeAccentBlue = Color(0xFF0074CE)     // 버튼/링크/포인트 텍스트
internal val HomeBorderBlue = Color(0xFF5AA2D9)     // 카드 테두리, 아웃라인 칩 테두리
internal val HomeChipFillLight = Color(0xFFDEF1FF)  // 여행 없을 때 홈 - 키워드 칩 배경
internal val HomeChipFillSelected = Color(0xFF5AA2D9) // 선택된 키워드 칩(#바다) 배경
internal val HomeGrayText = Color(0xFF818181)
internal val HomePageBg = Color(0xFFF8F8FC)         // 인기 키워드 화면 본문 배경
internal val HomeDividerGray = Color(0xFFDFDFDF)
internal val HomeAvatarGray = Color(0xFFD9D9D9)
internal val HomeTextPrimary = Color(0xFF000000)
private val HomeCardGradientTop = Color(0xFFE3F3FF)
private val HomeCardGradientBottom = Color(0xFFFFFFFF)

private val HomeScreenHorizontalPadding = 28.dp

// 하단 네비게이션 바는 AppNavigation.kt에서 공통으로 관리함 (여기서 직접 안 그림)
@Composable
fun HomeScreen(
    onStartRouteClick: () -> Unit = {},
    onViewRouteClick: () -> Unit = {},
    onPingClick: () -> Unit = {},
    onSeeAllPopularRoutes: () -> Unit = {},
    onSeeAllPopularPlaces: () -> Unit = {},
    // 키워드 칩을 직접 눌렀으면 그 키워드가, "이번 주 인기 키워드" 제목을 눌렀으면 null이 넘어옴
    onSeeAllKeywords: (String?) -> Unit = {},
    onSeeAllNearbyCourses: () -> Unit = {},
    onCourseClick: (Int) -> Unit = {},
    onPopularRouteClick: (Long) -> Unit = {},
    onPlaceClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    // [1번 섹션] 진행 중인 여행 - 실제 API(GET /trips/current-summary) 연동됨
    // [2번 섹션] 인사말 닉네임 - 실제 API(GET /auth/me) 연동됨
    // [3번 섹션] 이번 주 인기 루트 - 실제 API(GET /trips/popular) 연동됨
    // [4번 섹션] 이번 주 인기 키워드 - 실제 API(GET /keyword/popular) 연동됨.
    //   Ping 후기 등록 시 같이 남기는 해시태그를 최근 7일 기준으로 집계함.
    val currentTrip by viewModel.currentTrip.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val popularTrips by viewModel.popularTrips.collectAsState()
    val popularKeywords by viewModel.popularKeywords.collectAsState()
    val popularPlaces by viewModel.popularPlaces.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadCurrentTrip()
        viewModel.loadNickname()
        viewModel.loadPopularTrips()
        viewModel.loadPopularKeywords()
        viewModel.loadPopularPlaces(limit = 4)
    }

    // 👈 수정: mock 데이터 대신 "마지막으로 찍은 핑" 주변의 실제 공개 루트로 교체
    // (currentTrip.lastPingLatitude/Longitude가 있을 때만 채워짐 - loadCurrentTrip()에서 같이 불러옴)
    val nearbyTripsRaw by viewModel.nearbyCourses.collectAsState()
    val nearbyCourses = remember(nearbyTripsRaw) {
        nearbyTripsRaw.map { trip ->
            HomeCourseCard(
                id = trip.routeId.toInt(),
                authorName = null, // 내 주변 코스 카드는 원래부터 작성자 행을 숨기는 디자인
                courseName = trip.title ?: "여행 루트",
                stops = trip.stopNames,
                tags = trip.tags.map { HomeKeyword(text = "#$it") },
                pingCount = trip.pingCount.toInt(),
                distanceKm = trip.distanceKm ?: 0.0,
                bookmarkCount = trip.savedCount.toInt()
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item { HomeHeader(userName = nickname ?: "회원", hasActiveTrip = currentTrip != null) }
        item { Spacer(modifier = Modifier.height(31.dp)) }

        item {
            val trip = currentTrip
            if (trip != null) {
                ActiveTripCard(
                    trip = trip,
                    onViewRouteClick = onViewRouteClick,
                    // 👈 수정: 예전엔 Ping 탭으로 이동만 시켰는데, 이제 눌렀을 때 바로 계획된
                    // 다음 장소를 핑으로 찍고(큐), 홈 화면에서 바로 카운트/스테퍼가 갱신되는 걸
                    // 볼 수 있게 함(화면 이동 없음). "루트보기" 버튼은 그대로 Ping 탭으로 이동.
                    onPingClick = {
                        viewModel.confirmNextPing { message, _ ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                StartTripCard(onStartRouteClick = onStartRouteClick)
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }

        item { SectionHeader(title = "이번 주 인기 루트", showSeeAll = true, onSeeAllClick = onSeeAllPopularRoutes) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            val topRoute = popularTrips.firstOrNull()
            if (topRoute != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = HomeScreenHorizontalPadding, vertical = 4.dp)
                        .clickable { onPopularRouteClick(topRoute.routeId) }
                ) {
                    PopularRouteCard(topRoute)
                }
            } else {
                Text(
                    text = "아직 이번 주 인기 루트가 없어요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeGrayText,
                    modifier = Modifier.padding(horizontal = HomeScreenHorizontalPadding, vertical = 4.dp)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }

        item { SectionHeader(title = "지금 떠오르는 인기 장소", showSeeAll = true, onSeeAllClick = onSeeAllPopularPlaces) }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { PopularPlacesRow(places = popularPlaces, onPlaceClick = onPlaceClick) }
        item { Spacer(modifier = Modifier.height(22.dp)) }

        item {
            SectionHeader(
                title = "이번 주 인기 키워드",
                showSeeAll = false,
                onSeeAllClick = { onSeeAllKeywords(null) },
                rowClickable = popularKeywords.isNotEmpty()
            )
        }
        item { Spacer(modifier = Modifier.height(9.dp)) }
        item {
            if (popularKeywords.isNotEmpty()) {
                KeywordChipsRow(
                    keywords = popularKeywords.map { HomeKeyword(text = "#${it.keyword}") },
                    outlined = currentTrip != null,
                    onKeywordClick = { onSeeAllKeywords(it.text.removePrefix("#")) }
                )
            } else {
                Text(
                    text = "아직 이번 주 인기 키워드가 없어요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeGrayText,
                    modifier = Modifier.padding(horizontal = HomeScreenHorizontalPadding)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(23.dp)) }

        item {
            // 👈 수정: 진행 중인 여행에서 핑을 하나라도 찍었으면 "마지막 핑 주변 코스"로 제목을 바꿔서
            // 지금 보여주는 코스 목록이 기기 현재 위치가 아니라 마지막으로 찍은 핑 기준이라는 걸 알려줌.
            val nearbySectionTitle = if (currentTrip?.lastPingLatitude != null) "마지막 핑 주변 코스" else "내 주변 코스"
            SectionHeader(title = nearbySectionTitle, showSeeAll = true, onSeeAllClick = onSeeAllNearbyCourses)
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        if (nearbyCourses.isEmpty()) {
            item {
                Text(
                    text = if (currentTrip?.lastPingLatitude != null) "마지막 핑 주변에 아직 등록된 코스가 없어요"
                           else "Ping을 찍으면 그 주변 코스를 보여드려요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeGrayText,
                    modifier = Modifier.padding(horizontal = HomeScreenHorizontalPadding)
                )
            }
        } else {
            // 👈 수정: 홈에는 미리보기로 3개까지만 보여주고, 전체 목록은 "더보기"(NearbyCoursesScreen)에서 봄
            items(nearbyCourses.take(3)) { course ->
                Box(modifier = Modifier.padding(horizontal = HomeScreenHorizontalPadding)) {
                    HomeCourseCardView(
                        course = course,
                        showArrows = currentTrip != null,
                        titleFontSize = 15.sp,
                        onClick = { onCourseClick(course.id) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeHeader(userName: String, hasActiveTrip: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeScreenHorizontalPadding)
            .padding(top = 25.dp)
    ) {
        Text(text = "홈", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = HomeTextPrimary)
        Spacer(modifier = Modifier.height(31.dp))
        Text(text = "안녕하세요, $userName 님!", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = HomeTextPrimary)
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (hasActiveTrip) "오늘의 여행을 이어가볼까요?" else "오늘은 어디로 떠나볼까요?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = HomeTextPrimary
        )
    }
}

// ===== 여행 없을 때 카드 (피그마 node 19:2) =====
@Composable
private fun StartTripCard(onStartRouteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = HomeScreenHorizontalPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(HomeCardGradientTop, HomeCardGradientBottom)))
            .border(2.dp, HomeBorderBlue, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 21.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "새로운 여행을 시작해볼까요?",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HomeAccentBlue,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(17.dp))
            Text(
                text = "원하는 조건을 선택하면 여행 루트를 추천해드려요.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HomeTextPrimary,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(22.dp))
            FilledButtonPill(text = "루트 만들러 가기  →", onClick = onStartRouteClick, modifier = Modifier.wrapContentWidth())
        }
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(R.drawable.home_mascot_large),
            contentDescription = null,
            modifier = Modifier.size(width = 92.dp, height = 97.dp)
        )
    }
}

// ===== 여행 중일 때 카드 (피그마 node 108:793) =====
// trip.actualRouteId 등은 실제 API(GET /trips/current-summary) 응답. DB에 "여행 제목" 컬럼
// 자체가 없어서 제목은 고정 문구 "진행 중인 여행"으로 둠(팀 논의 결과) - tripName처럼 없는 데이터를
// 지어내지 않음. pingCount/visitedPlaceNames는 실제 WidgetPing 데이터임.
@Composable
private fun ActiveTripCard(
    trip: CurrentTripSummaryResponse,
    onViewRouteClick: () -> Unit,
    onPingClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = HomeScreenHorizontalPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(HomeCardGradientTop, HomeCardGradientBottom)))
            .border(2.dp, HomeBorderBlue, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 17.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "진행 중인 여행", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = HomeAccentBlue)
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Text(text = "현재 ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                Text(text = "${trip.pingCount}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD95A5A))
                Text(text = "개의 Ping을 남겼어요", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.home_mascot_small),
                contentDescription = null,
                modifier = Modifier.size(width = 51.dp, height = 54.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (trip.visitedPlaceNames.isNotEmpty()) {
                TripStepper(
                    stops = trip.visitedPlaceNames,
                    confirmedCount = trip.confirmedCount,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "아직 등록된 Ping이 없어요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeGrayText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(19.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButtonPill(text = "루트보기", modifier = Modifier.weight(1f), onClick = onViewRouteClick)
            FilledButtonPill(text = "Ping 찍기", modifier = Modifier.weight(1f), onClick = onPingClick)
        }
    }
}

// 정거장 점(파란 링 + 흰 중심) + 연결선.
// 👈 수정: 이름이 길면(예: "Seoul Metropolitan Government") 한 글자씩 세로로 줄바꿈되던 문제
// 수정 - 정거장 하나당 고정 너비 칸에 한 줄 + 말줄임(...)으로 표시하고, 전체는 가로 스크롤 가능하게 함
// (HomeCourseCardView의 스탑 목록과 같은 패턴). 또한 confirmedCount 기준으로 이미 찍은 핑은
// 파란색 글씨로, 다음에 찍을 핑은 굵게 강조(포커스)해서 보여줌.
private val TripStepperStopWidth = 76.dp

@Composable
private fun TripStepper(stops: List<String>, confirmedCount: Long = 0, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Top
    ) {
        stops.forEachIndexed { index, stop ->
            val isConfirmed = index < confirmedCount
            val isNext = index.toLong() == confirmedCount
            Column(
                modifier = Modifier.width(TripStepperStopWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepDot(highlighted = isNext)
                    if (index != stops.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(HomeAccentBlue)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stop,
                    fontSize = 13.sp,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isConfirmed) HomeAccentBlue else HomeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StepDot(highlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (highlighted) 31.dp else 27.dp)
            .clip(CircleShape)
            .background(HomeAccentBlue)
            .then(
                if (highlighted) Modifier.border(2.dp, Color(0xFFD95A5A), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun FilledButtonPill(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(64.dp))
            .background(HomeAccentBlue)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlinedButtonPill(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(64.dp))
            .background(Color.White)
            .border(1.dp, HomeAccentBlue, RoundedCornerShape(64.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = HomeAccentBlue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showSeeAll: Boolean,
    onSeeAllClick: () -> Unit,
    rowClickable: Boolean = !showSeeAll
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeScreenHorizontalPadding)
            .clickable(enabled = rowClickable) { onSeeAllClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary, modifier = Modifier.weight(1f))
        if (showSeeAll) {
            Text(
                text = "더보기 >",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeTextPrimary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

// ===== "이번 주 인기 루트" 카드: 사진 위에 정거장 점 오버레이 + 사진 하단과 겹치는 흰색 정보 패널 =====
// route.stopNames/photoUrl은 실제 API(GET /trips/popular) 응답. 소요시간(durationLabel)은 DB에
// 저장되는 데이터가 없어서(팀 논의 결과) 라벨 자체를 뺐음 - 지어내지 않음.
@Composable
private fun PopularRouteCard(route: PopularTripResponse) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }

    Box(modifier = Modifier.fillMaxWidth().height(164.dp)) {
        val photoModifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .align(Alignment.TopStart)
            .clip(RoundedCornerShape(8.dp))
        if (!route.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = route.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = photoModifier
            )
        } else {
            Image(
                painter = painterResource(R.drawable.home_popular_route_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = photoModifier
            )
        }

        // 사진 위 정거장 점 + 연결선 - 점 위치는 0%/50%/100%로 고정(대칭 유지, 이름 길이와 무관).
        if (route.stopNames.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 60.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                // Ping이 1개뿐이면 이어줄 다음 정거장이 없으니 연결선을 그리지 않음
                if (route.stopNames.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(HomeAccentBlue)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (route.stopNames.size > 1) Arrangement.SpaceBetween else Arrangement.Center
                ) {
                    repeat(route.stopNames.size) { StepDot() }
                }
            }
        }

        // 사진 하단과 겹치는 흰색 정보 패널
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 94.dp)
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 9.dp, vertical = 17.dp)
        ) {
            if (route.stopNames.isNotEmpty()) {
                // 이 영역만 좌우 6dp를 더 줘서(기본 9dp + 6dp = 15dp) 위 점 행과 동일한 가로 기준으로 정렬함.
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                    val availableWidthPx = with(density) { maxWidth.roundToPx() }
                    // 점은 고정 위치(0/50/100%)이므로, 이름은 그 고정 위치를 기준으로 한 슬롯 폭에
                    // 맞게 "단어(공백) 단위로만" 줄임 - 글자 중간을 "..."으로 자르지 않고, 다 안
                    // 들어가는 마지막 단어는 통째로 버림 (예: "광장시장 마약김밥" -> "광장시장").
                    val displayLabels = remember(route.stopNames, availableWidthPx) {
                        wordTruncatedStopLabels(route.stopNames, availableWidthPx, textMeasurer, labelStyle)
                    }
                    FixedCenterLabelsRow(labels = displayLabels, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Text(
                    text = "등록된 Ping 기록이 없어요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeGrayText
                )
            }
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = formatPopularityLabel(route.savedCount),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeAccentBlue,
                modifier = if (route.stopNames.size > 1) Modifier else Modifier.fillMaxWidth(),
                textAlign = if (route.stopNames.size > 1) TextAlign.Start else TextAlign.Center
            )
        }
    }
}

// 정거장 이름을, 위 점 행과 동일한 고정 중심 위치(0%/50%/100%) 기준 슬롯 폭에 맞춰 "단어 단위"로
// 줄인 문자열 리스트로 미리 계산함. 슬롯에 다 안 들어가면 뒤 단어부터 통째로 버리고(글자 중간을
// 안 자름), 단어가 하나뿐이라 더 줄일 경계가 없으면 원본을 그대로 둬서 Text의 ellipsis(안전망)에 맡김.
private fun wordTruncatedStopLabels(
    labels: List<String>,
    availableWidthPx: Int,
    textMeasurer: TextMeasurer,
    style: TextStyle
): List<String> {
    if (labels.isEmpty() || availableWidthPx <= 0) return labels
    val count = labels.size
    val fairShare = availableWidthPx / count.coerceAtLeast(1)

    fun targetCenterX(index: Int): Float =
        if (count <= 1) availableWidthPx / 2f else availableWidthPx * index / (count - 1).toFloat()

    fun measureWidth(text: String): Int =
        textMeasurer.measure(text = text, style = style, maxLines = 1).size.width

    return labels.mapIndexed { index, label ->
        val center = targetCenterX(index)
        // 이 점을 기준으로 좌우 대칭을 유지한 채 들어갈 수 있는 최대 폭. 가장자리 점(첫/끝)은 이 값이
        // 작아지므로, 정거장 개수만큼 공평하게 나눈 폭(fairShare) 아래로는 안 줄어들게 최소치를 보장함.
        val symmetricMax = 2f * minOf(center, availableWidthPx - center)
        val maxWidthPx = maxOf(symmetricMax.toInt(), fairShare)

        if (measureWidth(label) <= maxWidthPx) {
            label
        } else {
            val words = label.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.size <= 1 || measureWidth(words[0]) > maxWidthPx) {
                label
            } else {
                var result = words[0]
                for (i in 1 until words.size) {
                    val candidate = "$result ${words[i]}"
                    if (measureWidth(candidate) <= maxWidthPx) result = candidate else break
                }
                result
            }
        }
    }
}

// 이미 슬롯 폭에 맞게 준비된 이름들을, 점 행과 동일한 고정 중심 위치(0%/50%/100%)에 맞춰 배치하는
// 커스텀 레이아웃 - Row+SpaceBetween은 글자 폭이 다 달라서 "중심"이 안 맞으므로 직접 계산함.
@Composable
private fun FixedCenterLabelsRow(labels: List<String>, modifier: Modifier = Modifier) {
    Layout(
        modifier = modifier,
        content = {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    ) { measurables, constraints ->
        val count = measurables.size
        val totalWidth = constraints.maxWidth

        fun targetCenterX(index: Int) =
            if (count <= 1) totalWidth / 2f else totalWidth * index / (count - 1).toFloat()

        val placeables = measurables.map { it.measure(Constraints(maxWidth = totalWidth)) }
        val height = placeables.maxOfOrNull { it.height } ?: 0

        layout(totalWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val center = targetCenterX(index)
                val x = (center - placeable.width / 2f)
                    .coerceIn(0f, (totalWidth - placeable.width).toFloat().coerceAtLeast(0f))
                placeable.placeRelative(x.toInt(), 0)
            }
        }
    }
}

// 백엔드는 저장(찜) 개수(savedCount)만 내려줘서, 표시용 문구("3.2만명 인기" / "12명 인기")로 프론트에서 포맷팅함.
private fun formatPopularityLabel(savedCount: Long): String {
    return if (savedCount >= 10000) {
        "%.1f만명 인기".format(savedCount / 10000.0)
    } else {
        "${savedCount}명 인기"
    }
}

// 저장 수 순위(1위부터) 그대로 나열 - /places/popular가 이미 저장 수 내림차순으로 내려줌.
// 👈 수정: 이제 백엔드가 저장 많이 된 루트의 이 장소 후기 사진(photoUrl)을 내려줌 - 있으면 그 실제
// 사진을, 아직 사진 등록된 후기가 없는 장소면(photoUrl == null) 예전처럼 자리별 고정 이미지로 대체함.
@Composable
private fun PopularPlacesRow(places: List<PopularPlaceResponse>, onPlaceClick: (Long) -> Unit = {}) {
    val fallbackImages = listOf(
        R.drawable.home_place_haeundae,
        R.drawable.home_place_cafe_street,
        R.drawable.home_place_yeouido,
        R.drawable.home_place_namsan
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = HomeScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        items(places.size) { index ->
            val place = places[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(78.dp).clickable { onPlaceClick(place.spotId) }
            ) {
                if (place.photoUrl != null) {
                    AsyncImage(
                        model = place.photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(78.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(fallbackImages.getOrElse(index) { R.drawable.home_place_haeundae }),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(78.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 주소가 있으면 지역명(예: "서울"), 주소가 없는 장소는 카테고리로 대체 - 항상 둘째 줄이 비지 않게 함
                val subLabel = regionOf(place.address) ?: place.category?.let { PlaceCategory.labelOf(it) }
                if (subLabel != null) {
                    Text(text = subLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                }
            }
        }
    }
}

// 주소 문자열(예: "서울 종로구 사직로 161")의 맨 앞 토큰을 지역명으로 사용. 주소가 없으면 표시 안 함.
private fun regionOf(address: String?): String? =
    address?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }

@Composable
private fun KeywordChipsRow(
    keywords: List<HomeKeyword>,
    outlined: Boolean,
    onKeywordClick: (HomeKeyword) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = HomeScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(keywords) { keyword ->
            HomeKeywordChip(
                keyword = keyword,
                forceOutlined = outlined,
                onClick = { onKeywordClick(keyword) }
            )
        }
    }
}

// forceOutlined=true인 화면(여행 중 홈)에서는 selected 여부 상관없이 전부 아웃라인으로 표시(피그마 기준)
@Composable
internal fun HomeKeywordChip(keyword: HomeKeyword, forceOutlined: Boolean = false, onClick: () -> Unit = {}) {
    val filled = keyword.selected && !forceOutlined
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(56.dp))
            .background(if (filled) HomeChipFillSelected else if (forceOutlined) Color.White else HomeChipFillLight)
            .then(
                if (forceOutlined || keyword.selected) Modifier.border(1.dp, HomeBorderBlue, RoundedCornerShape(56.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(text = keyword.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeAccentBlue)
    }
}

// "내 주변 코스" / "인기 키워드" 화면에서 공통으로 쓰는 코스 카드 (피그마: 흰 배경 + 그림자, 테두리 없음)
@Composable
internal fun HomeCourseCardView(
    course: HomeCourseCard,
    showArrows: Boolean,
    titleFontSize: TextUnit = 13.sp,
    showAvatar: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        if (course.authorName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showAvatar) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(HomeAvatarGray))
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(text = course.authorName, fontSize = if (showAvatar) 15.sp else 10.sp, fontWeight = FontWeight.Bold, color = HomeTextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.home_course_map_thumb),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(83.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = course.courseName, fontWeight = FontWeight.SemiBold, fontSize = titleFontSize, color = HomeTextPrimary, modifier = Modifier.weight(1f))
                    BookmarkIcon()
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = course.bookmarkCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (showArrows) {
                    // 실데이터는 장소 이름이 길 수 있어서(예: "인천 계양구 감성 소품샵 카페 1501호점"),
                    // 줄바꿈 없는 Row를 그냥 두면 좁은 카드 폭에 짓눌려 레이아웃이 깨짐 -> 가로 스크롤 처리
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        course.stops.forEachIndexed { index, stop ->
                            Text(
                                text = stop,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HomeTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (index != course.stops.lastIndex) {
                                Text(text = "  →  ", fontSize = 13.sp, color = HomeTextPrimary)
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        course.stops.forEach { stop ->
                            Text(text = stop, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Ping 개수 : ${course.pingCount}개", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                    Text(text = "${course.distanceKm} km", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                }
                if (course.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        course.tags.forEach { tag -> HomeKeywordChip(keyword = tag) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BookmarkIcon(filled: Boolean = false, modifier: Modifier = Modifier) {
    Icon(
        imageVector = if (filled) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
        contentDescription = null,
        tint = if (filled) HomeAccentBlue else Color(0xFF2E2E2E),
        modifier = modifier.size(20.dp)
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun HomeScreenNoTripPreview() {
    HomeScreen()
}

// HomeScreen 자체는 이제 ViewModel에서 직접 데이터를 받아오므로, "여행 중" 상태는
// ActiveTripCard 하나만 따로 프리뷰함 (디자인 확인용 샘플 데이터 - 실제 화면 로직과 무관).
@Preview(showBackground = true)
@Composable
private fun ActiveTripCardPreview() {
    ActiveTripCard(
        trip = CurrentTripSummaryResponse(
            actualRouteId = 1,
            companionType = "FRIEND",
            transport = "WALK",
            memberCount = 2,
            travelDate = "2026-09-04",
            status = "IN_PROGRESS",
            pingCount = 3,
            visitedPlaceNames = listOf("광안리", "해운대", "청사포")
        ),
        onViewRouteClick = {},
        onPingClick = {}
    )
}
