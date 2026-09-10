package com.tripping.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember

import com.tripping.app.ui.component.AppBottomNavBar
import com.tripping.app.ui.component.AppBottomNavTab
import com.tripping.app.ui.component.RouteMenuSheet
import com.tripping.app.ui.screen.RouteRecommendScreen
import com.tripping.app.ui.screen.RouteDetailScreen
import com.tripping.app.viewmodel.RouteCreateViewModel

import com.tripping.app.ui.screen.CourseDetailScreen
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.MyMapDetailScreen
import com.tripping.app.ui.screen.MyPageScreen
import com.tripping.app.ui.screen.NearbyCoursesScreen
import com.tripping.app.ui.screen.PingCourseDetailScreen
import com.tripping.app.ui.screen.PingHistoryScreen
import com.tripping.app.ui.screen.PingLogDetailScreen
import com.tripping.app.ui.screen.PingLogReviewScreen
import com.tripping.app.ui.screen.PingPlaceSearchScreen
import com.tripping.app.ui.screen.PingScreen
import com.tripping.app.ui.screen.PlaceSearchScreen
import com.tripping.app.ui.screen.PopularKeywordsScreen
import com.tripping.app.ui.screen.PopularPlacesScreen
import com.tripping.app.ui.screen.PopularRoutesScreen
import com.tripping.app.ui.screen.RouteCreateScreen
import com.tripping.app.ui.screen.SettingsScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.ui.screen.TripStartScreen
import com.tripping.app.ui.screen.RouteMapAppliedScreen
import com.tripping.app.ui.screen.RouteMapEditScreen
import com.tripping.app.ui.screen.RoutePlaceSearchScreen
import com.tripping.app.ui.screen.RecommendedRoute
import com.tripping.app.ui.screen.PlaceDetailScreen
import com.tripping.app.ui.screen.RegionPingRegisterScreen
import com.tripping.app.data.response.PlaceSearchResponse


import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel
import com.tripping.app.viewmodel.CommunityViewModel


private const val COURSE_DETAIL_ROUTE = "course_detail/{tripId}?title={title}"
private const val TRIP_START_ROUTE = "trip_start/{tripId}?title={title}"

private const val PING_COURSE_DETAIL_ROUTE =
    "ping_course_detail/{routeId}?title={title}"

private const val PING_ADD_PLACE_ROUTE =
    "ping_add_place/{routeId}"

// 👈 새로 추가: 진행중인 여행 "+" 버튼 -> 이 라우트로 이동해서 장소 검색 후 실시간 핑 등록
private const val PING_ADD_ONGOING_PLACE_ROUTE =
    "ping_add_ongoing_place/{routeId}"

// 👈 새로 추가: "핑로그 기록/수정 하러가기" -> 후기 작성 화면
private const val PING_LOG_REVIEW_ROUTE =
    "ping_log_review/{pingId}?placeName={placeName}"

private const val PING_HISTORY_ROUTE =
    "ping_history"

// 👈 새로 추가: 로그 커뮤니티 카드 클릭 -> 로그 상세보기(경유지 + 후기)
private const val PING_LOG_DETAIL_ROUTE =
    "ping_log_detail/{routeId}"

// 👈 새로 추가: "지역핑 등록하기" -> 장소 선택(지도) 화면 (PingPlaceSearchScreen 재사용)
private const val REGION_PING_PLACE_ROUTE =
    "region_ping_place/{regionId}?regionName={regionName}"

// 👈 새로 추가: 장소 선택 후 -> 평점/후기 입력 화면
private const val REGION_PING_FORM_ROUTE =
    "region_ping_form/{spotId}/{spotName}/{address}/{latitude}/{longitude}?regionName={regionName}"

private const val ROUTE_CREATE_ROUTE =
    "route_create"


// 바텀바를 보여줄 라우트와 현재 선택된 탭을 연결
private val bottomBarRoutes = mapOf(
    "home" to AppBottomNavTab.HOME,

    "placeSearch" to AppBottomNavTab.SEARCH,
    "placeSearch" to AppBottomNavTab.SEARCH,
    "route_detail/{routeId}" to AppBottomNavTab.SEARCH,
    "place_detail/{spotId}" to AppBottomNavTab.SEARCH,

    ROUTE_CREATE_ROUTE to AppBottomNavTab.ROUTE,

    "ping" to AppBottomNavTab.PING,
    PING_COURSE_DETAIL_ROUTE to AppBottomNavTab.PING,
    PING_ADD_PLACE_ROUTE to AppBottomNavTab.PING,
    PING_ADD_ONGOING_PLACE_ROUTE to AppBottomNavTab.PING, // 👈 새로 추가
    PING_LOG_REVIEW_ROUTE to AppBottomNavTab.PING, // 👈 새로 추가
    PING_HISTORY_ROUTE to AppBottomNavTab.PING,
    PING_LOG_DETAIL_ROUTE to AppBottomNavTab.PING, // 👈 새로 추가
    REGION_PING_PLACE_ROUTE to AppBottomNavTab.PING, // 👈 새로 추가
    REGION_PING_FORM_ROUTE to AppBottomNavTab.PING, // 👈 새로 추가

    "mypage" to AppBottomNavTab.MY,
    "trip_history" to AppBottomNavTab.MY,
    "my_map_detail" to AppBottomNavTab.MY,
    COURSE_DETAIL_ROUTE to AppBottomNavTab.MY,
    TRIP_START_ROUTE to AppBottomNavTab.MY,
    "settings" to AppBottomNavTab.MY,

    "popular_keywords?keyword={keyword}" to AppBottomNavTab.HOME,
    "nearby_courses" to AppBottomNavTab.HOME,
    "popular_routes" to AppBottomNavTab.HOME,
    "popular_places" to AppBottomNavTab.HOME
)


// "다녀온 여행" → 코스 상세
private fun navigateToCourseDetail(
    navController: NavController,
    tripId: Long,
    title: String
) {
    navController.navigate(
        "course_detail/$tripId?title=${Uri.encode(title)}"
    )
}


// "여행 바로 시작하기"
private fun navigateToTripStart(
    navController: NavController,
    tripId: Long,
    title: String
) {
    navController.navigate(
        "trip_start/$tripId?title=${Uri.encode(title)}"
    )
}


// Ping 탭 → 코스 상세
private fun navigateToPingCourseDetail(
    navController: NavController,
    routeId: Long,
    title: String
) {
    navController.navigate(
        "ping_course_detail/$routeId?title=${Uri.encode(title)}"
    )
}


// Ping "로그" 탭 카드 → 로그 상세보기
private fun navigateToPingLogDetail(
    navController: NavController,
    routeId: Long
) {
    navController.navigate(
        "ping_log_detail/$routeId"
    )
}


// Ping 코스 상세 → 장소 추가
private fun navigateToPingAddPlace(
    navController: NavController,
    routeId: Long
) {
    navController.navigate(
        "ping_add_place/$routeId"
    )
}


// 👈 새로 추가: Ping 진행중인 여행 → 장소 추가(실시간 핑)
private fun navigateToPingAddOngoingPlace(
    navController: NavController,
    routeId: Long
) {
    navController.navigate(
        "ping_add_ongoing_place/$routeId"
    )
}


// 👈 새로 추가: "핑로그 기록/수정 하러가기" → 후기 작성 화면
private fun navigateToPingLogReview(
    navController: NavController,
    pingId: Long,
    placeName: String
) {
    navController.navigate(
        "ping_log_review/$pingId?placeName=${Uri.encode(placeName)}"
    )
}


// 👈 새로 추가: "지역핑 등록하기" → 장소 선택 화면
private fun navigateToRegionPingPlace(
    navController: NavController,
    regionId: String,
    regionName: String
) {
    navController.navigate(
        "region_ping_place/$regionId?regionName=${Uri.encode(regionName)}"
    )
}

// 👈 새로 추가: 장소 선택 완료 → 평점/후기 입력 화면
private fun navigateToRegionPingForm(
    navController: NavController,
    regionName: String,
    place: com.tripping.app.data.response.PlaceSearchResponse
) {
    // 방금 새로 등록한 장소는 서버가 address를 null로 내려줄 수 있음(요청 DTO에 주소가 없음) ->
    // PlaceSearchResponse.address 타입은 String(non-null)이지만 Gson은 null을 그대로 넣어버려서
    // 방어적으로 null 처리 안 하면 네비게이션 인자 번들 생성 중 IllegalArgumentException으로 앱이 죽음.
    val safeName = place.name ?: ""
    val safeAddress = place.address ?: ""
    navController.navigate(
        "region_ping_form/${place.spotId}/${Uri.encode(safeName)}/${Uri.encode(safeAddress)}/${place.latitude}/${place.longitude}?regionName=${Uri.encode(regionName)}"
    )
}


// Ping → 다녀온 여행
private fun navigateToPingHistory(
    navController: NavController
) {
    navController.navigate(PING_HISTORY_ROUTE)
}


// 하단바 탭 이동
private fun navigateToTab(
    navController: NavController,
    route: String
) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}


@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

    val currentTab =
        bottomBarRoutes[currentRoute]


    // ⭐ 루트 메뉴 표시 여부
    var showRouteMenu by remember {
        mutableStateOf(false)
    }

    val routeCreateViewModel: RouteCreateViewModel = viewModel()

    Scaffold(
        containerColor = Color.White,

        bottomBar = {

            if (currentTab != null) {

                AppBottomNavBar(

                    selectedTab = currentTab,

                    onHomeClick = {
                        showRouteMenu = false
                        navigateToTab(
                            navController,
                            "home"
                        )
                    },

                    onSearchClick = {
                        showRouteMenu = false
                        navigateToTab(
                            navController,
                            "placeSearch"
                        )
                    },

                    // ⭐ 루트 버튼: 이미 열려있으면 닫고, 닫혀있으면 염 (토글)
                    onRouteClick = {
                        showRouteMenu = !showRouteMenu
                    },

                    onPingClick = {
                        showRouteMenu = false
                        navigateToTab(
                            navController,
                            "ping"
                        )
                    },

                    onMyClick = {
                        showRouteMenu = false
                        navigateToTab(
                            navController,
                            "mypage"
                        )
                    }
                )
            }
        }

    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {


            // ============================================================
            // 화면 이동
            // ============================================================

            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {


                // ========================================================
                // 로그인
                // ========================================================

                composable("login") {

                    val authViewModel: AuthViewModel =
                        viewModel()

                    val loginState by
                    authViewModel.loginState.collectAsState()


                    LaunchedEffect(loginState) {

                        if (loginState is AuthState.Success) {

                            navController.navigate("home") {

                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }
                    }


                    LoginScreen(

                        onLoginClick = { email, password ->

                            authViewModel.login(
                                email,
                                password
                            )
                        },

                        onSignUpLinkClick = {

                            navController.navigate(
                                "signup"
                            )
                        },

                        isLoading =
                            loginState is AuthState.Loading,

                        errorMessage =
                            (loginState as? AuthState.Error)
                                ?.message
                    )
                }


                // ========================================================
                // 회원가입
                // ========================================================

                composable("signup") {

                    val authViewModel: AuthViewModel =
                        viewModel()

                    val signUpState by
                    authViewModel.signUpState.collectAsState()

                    val regions by
                    authViewModel.regions.collectAsState()


                    LaunchedEffect(signUpState) {

                        if (signUpState is AuthState.Success) {

                            navController.popBackStack()
                        }
                    }


                    SignUpScreen(

                        regions = regions,

                        onLoadRegions = {

                            authViewModel.loadRegions()
                        },

                        onSignUpClick = {
                                nickname,
                                email,
                                password,
                                regionId ->

                            authViewModel.signUp(
                                nickname,
                                email,
                                password,
                                regionId
                            )
                        },

                        onLoginLinkClick = {

                            navController.popBackStack()
                        },

                        isLoading =
                            signUpState is AuthState.Loading,

                        errorMessage =
                            (signUpState as? AuthState.Error)
                                ?.message
                    )
                }


                // ========================================================
                // 홈
                // ========================================================

                composable("home") {

                    HomeScreen(

                        onStartRouteClick = {
                            showRouteMenu = true
                        },

                        onViewRouteClick = {
                            // TODO: 진행 중인 여행 루트 상세
                        },

                        onPingClick = {

                            navigateToTab(
                                navController,
                                "ping"
                            )
                        },

                        onSeeAllPopularRoutes = {

                            navController.navigate(
                                "popular_routes"
                            )
                        },

                        onSeeAllPopularPlaces = {

                            navController.navigate(
                                "popular_places"
                            )
                        },

                        onSeeAllKeywords = { keyword ->

                            navController.navigate(
                                "popular_keywords?keyword=${Uri.encode(keyword ?: "")}"
                            )
                        },

                        onSeeAllNearbyCourses = {

                            navController.navigate(
                                "nearby_courses"
                            )
                        },

                        onCourseClick = {
                            // TODO: 코스 상세
                        }
                    )
                }


                // ========================================================
                // 탐색
                // ========================================================

                composable("placeSearch") {
                    PlaceSearchScreen(
                        onPlaceClick = { spotId ->
                            // TODO: 장소 상세조회
                        },
                        onRouteClick = { routeId ->
                            navController.navigate("route_detail/$routeId")
                        }
                    )
                }


                // ========================================================
                // 탐색 - 루트 상세 (인기 루트 카드/선 클릭 시)
                // ========================================================

                composable(
                    "route_detail/{routeId}",
                    arguments = listOf(
                        navArgument("routeId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->

                    val routeId = backStackEntry.arguments?.getLong("routeId") ?: 0L

                    RouteDetailScreen(
                        routeId = routeId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onStopClick = { spotId ->
                            navController.navigate("place_detail/$spotId")
                        }
                    )
                }

// ⭐ 장소 상세 조회
                composable(
                    "place_detail/{spotId}",
                    arguments = listOf(
                        navArgument("spotId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->

                    val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L

                    PlaceDetailScreen(
                        placeId = spotId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onRouteCardClick = { actualRouteId ->
                            navController.navigate("route_detail/$actualRouteId")
                        }
                    )
                }


                // ========================================================
                // 인기 키워드
                // ========================================================

                composable(
                    "popular_keywords?keyword={keyword}",
                    arguments = listOf(
                        navArgument("keyword") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->

                    val keywordArg = backStackEntry.arguments?.getString("keyword").orEmpty()

                    PopularKeywordsScreen(

                        initialKeyword = keywordArg.ifBlank { null },

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onCourseClick = { routeId ->
                            navController.navigate("route_detail/$routeId")
                        }
                    )
                }


                // ========================================================
                // 주변 코스
                // ========================================================

                composable("nearby_courses") {

                    NearbyCoursesScreen(

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onCourseClick = {
                            // TODO
                        }
                    )
                }


                // ========================================================
                // 인기 루트
                // ========================================================

                composable("popular_routes") {

                    PopularRoutesScreen(

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onCourseClick = { routeId ->
                            navController.navigate("route_detail/$routeId")
                        }
                    )
                }


                // ========================================================
                // 인기 장소
                // ========================================================

                composable("popular_places") {

                    PopularPlacesScreen(

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onPlaceClick = { spotId ->
                            navController.navigate("place_detail/$spotId")
                        }
                    )
                }


                // ========================================================
                // 마이페이지
                // ========================================================

                composable("mypage") {

                    MyPageScreen(

                        onOpenSettings = {

                            navController.navigate(
                                "settings"
                            )
                        },

                        onOpenTripHistory = {

                            navController.navigate(
                                "trip_history"
                            )
                        },

                        onOpenMyMap = {

                            navController.navigate(
                                "my_map_detail"
                            )
                        },

                        onOpenTripDetail = {
                                tripId,
                                title ->

                            navigateToCourseDetail(
                                navController,
                                tripId,
                                title
                            )
                        }
                    )
                }


                // ========================================================
                // 설정
                // ========================================================

                composable("settings") {

                    val authViewModel: AuthViewModel =
                        viewModel()

                    val logoutState by
                    authViewModel.logoutState.collectAsState()


                    LaunchedEffect(logoutState) {

                        if (logoutState is AuthState.Success) {

                            navController.navigate("login") {

                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        }
                    }


                    SettingsScreen(

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onLogoutConfirmed = {

                            authViewModel.logout()
                        }
                    )
                }


                // ========================================================
                // 다녀온 여행
                // ========================================================

                composable("trip_history") {

                    TripHistoryScreen(

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onOpenTripDetail = {
                                tripId,
                                title ->

                            navigateToCourseDetail(
                                navController,
                                tripId,
                                title
                            )
                        }
                    )
                }


                // ========================================================
                // 코스 상세
                // ========================================================

                composable(
                    COURSE_DETAIL_ROUTE,

                    arguments = listOf(

                        navArgument("tripId") {
                            type = NavType.LongType
                        },

                        navArgument("title") {

                            type = NavType.StringType

                            defaultValue = ""
                        }
                    )

                ) { backStackEntry ->


                    val tripId =
                        backStackEntry.arguments
                            ?.getLong("tripId")
                            ?: 0L

                    val title =
                        backStackEntry.arguments
                            ?.getString("title")


                    CourseDetailScreen(

                        tripId = tripId,

                        fallbackTitle =
                            title?.ifBlank {
                                null
                            },

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onStartTrip = {
                                id,
                                tripTitle ->

                            navigateToTripStart(
                                navController,
                                id,
                                tripTitle
                            )
                        },

                        onGoToPing = {

                            navigateToTab(
                                navController,
                                "ping"
                            )
                        }
                    )
                }


                // ========================================================
                // 여행 시작
                // ========================================================

                composable(
                    TRIP_START_ROUTE,

                    arguments = listOf(

                        navArgument("tripId") {
                            type = NavType.LongType
                        },

                        navArgument("title") {

                            type = NavType.StringType

                            defaultValue = ""
                        }
                    )

                ) { backStackEntry ->


                    val tripId =
                        backStackEntry.arguments
                            ?.getLong("tripId")
                            ?: 0L

                    val title =
                        backStackEntry.arguments
                            ?.getString("title")


                    TripStartScreen(

                        tripId = tripId,

                        fallbackTitle =
                            title?.ifBlank {
                                null
                            },

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onGoToPing = {

                            navigateToTab(
                                navController,
                                "ping"
                            )
                        }
                    )
                }


                // ========================================================
                // 내 지도
                // ========================================================

                composable("my_map_detail") {

                    MyMapDetailScreen(

                        onBackClick = {

                            navController.popBackStack()
                        }
                    )
                }


                // ========================================================
                // Ping
                // ========================================================

                composable("ping") {

                    PingScreen(

                        onAddPingClick = { routeId ->

                            navigateToPingAddOngoingPlace(
                                navController,
                                routeId
                            )
                        },

                        onPingLogClick = { pingId, placeName ->
                            navigateToPingLogReview(
                                navController,
                                pingId,
                                placeName
                            )
                        },

                        onRouteCardClick = {
                                routeId,
                                title ->

                            navigateToPingCourseDetail(
                                navController,
                                routeId,
                                title
                            )
                        },

                        onCourseClick = { routeId ->

                            navigateToPingLogDetail(
                                navController,
                                routeId
                            )
                        },

                        onMoreClick = {

                            navigateToPingHistory(
                                navController
                            )
                        },

                        onRegisterRegionPingClick = { regionId, regionName ->

                            navigateToRegionPingPlace(
                                navController,
                                regionId,
                                regionName
                            )
                        }
                    )
                }


                // ========================================================
                // Ping 다녀온 여행
                // ========================================================

                composable(
                    PING_HISTORY_ROUTE
                ) {

                    PingHistoryScreen(

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onTripClick = {
                                tripId,
                                title ->

                            navigateToPingCourseDetail(
                                navController,
                                tripId,
                                title
                            )
                        }
                    )
                }


                // ========================================================
                // Ping 코스 상세
                // ========================================================

                composable(
                    PING_COURSE_DETAIL_ROUTE,

                    arguments = listOf(

                        navArgument("routeId") {
                            type = NavType.LongType
                        },

                        navArgument("title") {

                            type = NavType.StringType

                            defaultValue = ""
                        }
                    )

                ) { backStackEntry ->


                    val routeId =
                        backStackEntry.arguments
                            ?.getLong("routeId")
                            ?: 0L

                    val title =
                        backStackEntry.arguments
                            ?.getString("title")
                            ?: "여행 기록"


                    PingCourseDetailScreen(

                        routeId = routeId,

                        courseName = title,

                        onBackClick = {

                            navController.popBackStack()
                        },

                        onAddPingClick = {

                            navigateToPingAddPlace(
                                navController,
                                routeId
                            )
                        },

                        onPingLogClick = { pingId, placeName ->
                            navigateToPingLogReview(
                                navController,
                                pingId,
                                placeName
                            )
                        }
                    )
                }


                // ========================================================
                // Ping 로그 상세보기 (로그 커뮤니티 카드 클릭)
                // ========================================================

                composable(
                    PING_LOG_DETAIL_ROUTE,

                    arguments = listOf(
                        navArgument("routeId") {
                            type = NavType.LongType
                        }
                    )

                ) { backStackEntry ->

                    val routeId =
                        backStackEntry.arguments
                            ?.getLong("routeId")
                            ?: 0L

                    PingLogDetailScreen(
                        routeId = routeId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }


                // ========================================================
                // 👈 새로 추가: 지역핑 등록 - 1단계(장소 선택, PingPlaceSearchScreen 재사용)
                // ========================================================

                composable(
                    REGION_PING_PLACE_ROUTE,

                    arguments = listOf(
                        navArgument("regionId") {
                            type = NavType.StringType
                        },
                        navArgument("regionName") {
                            type = NavType.StringType
                            defaultValue = "내 지역"
                        }
                    )

                ) { backStackEntry ->

                    val regionName =
                        backStackEntry.arguments
                            ?.getString("regionName")
                            ?: "내 지역"

                    val regionId =
                        backStackEntry.arguments
                            ?.getString("regionId")

                    PingPlaceSearchScreen(
                        regionId = regionId,
                        onPlaceSelected = { place ->
                            navigateToRegionPingForm(
                                navController,
                                regionName,
                                place
                            )
                        }
                    )
                }


                // ========================================================
                // 👈 새로 추가: 지역핑 등록 - 2단계(평점/후기 입력)
                // ========================================================

                composable(
                    REGION_PING_FORM_ROUTE,

                    arguments = listOf(
                        navArgument("spotId") {
                            type = NavType.LongType
                        },
                        navArgument("spotName") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument("address") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument("latitude") {
                            type = NavType.StringType
                            defaultValue = "0.0"
                        },
                        navArgument("longitude") {
                            type = NavType.StringType
                            defaultValue = "0.0"
                        },
                        navArgument("regionName") {
                            type = NavType.StringType
                            defaultValue = "내 지역"
                        }
                    )

                ) { backStackEntry ->

                    val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
                    val spotName = backStackEntry.arguments?.getString("spotName") ?: ""
                    val address = backStackEntry.arguments?.getString("address") ?: ""
                    val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
                    val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
                    val regionName = backStackEntry.arguments?.getString("regionName") ?: "내 지역"

                    val communityViewModel: CommunityViewModel = viewModel()
                    val context = LocalContext.current

                    RegionPingRegisterScreen(
                        regionName = regionName,
                        spotName = spotName,
                        address = address,
                        latitude = latitude,
                        longitude = longitude,
                        isSubmitting = communityViewModel.isRegisteringRegionPing,
                        errorMessage = communityViewModel.regionPingErrorMessage,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmit = { rating, reviewComment ->
                            communityViewModel.registerRegionPing(
                                spotId = spotId,
                                rating = rating,
                                reviewComment = reviewComment,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "지역핑이 등록되었습니다!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // 장소 선택 화면까지 건너뛰고 "로그" 화면으로 바로 복귀
                                    navController.popBackStack(REGION_PING_PLACE_ROUTE, true)
                                }
                            )
                        }
                    )
                }


                // ========================================================
                // Ping 장소 추가
                // ========================================================

                composable(
                    PING_ADD_PLACE_ROUTE,

                    arguments = listOf(

                        navArgument("routeId") {
                            type = NavType.LongType
                        }
                    )

                ) { backStackEntry ->


                    val routeId =
                        backStackEntry.arguments
                            ?.getLong("routeId")
                            ?: 0L

                    val pingViewModel:
                            com.tripping.app.viewmodel.PingViewModel =
                        viewModel()

                    val context =
                        LocalContext.current


                    PingPlaceSearchScreen(

                        onPlaceSelected = { place ->

                            pingViewModel.addSpotToTrip(

                                routeId = routeId,

                                spotId = place.spotId,

                                latitude = place.latitude,

                                longitude = place.longitude,

                                onSuccess = {

                                    Toast.makeText(
                                        context,
                                        "과거 여행에 추가되었습니다!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }
                            )
                        }
                    )
                }


                // ========================================================
                // 👈 새로 추가: Ping 진행중인 여행 장소 추가 (실시간 핑 등록)
                // ========================================================

                composable(
                    PING_ADD_ONGOING_PLACE_ROUTE,

                    arguments = listOf(

                        navArgument("routeId") {
                            type = NavType.LongType
                        }
                    )

                ) { backStackEntry ->


                    val routeId =
                        backStackEntry.arguments
                            ?.getLong("routeId")
                            ?: 0L

                    val pingViewModel:
                            com.tripping.app.viewmodel.PingViewModel =
                        viewModel()

                    val context =
                        LocalContext.current

                    // 👈 새로 추가: 지금까지 찍은 핑들을 지도에 선으로 이어서 보여주기 위해 미리 불러옴
                    LaunchedEffect(routeId) {
                        pingViewModel.loadTripSpotsForMap(routeId)
                    }

                    PingPlaceSearchScreen(

                        existingRouteSpots = pingViewModel.ongoingTripSpots,

                        onPlaceSelected = { place ->

                            // 👈 수정: createPing(WIDGET_PING)에 저장하면 "기록" 탭 타임라인이
                            // 읽는 ACTUAL_ROUTE_SPOT에는 안 보여서, 완료된 여행 쪽과 똑같이
                            // addSpotToTrip(ACTUAL_ROUTE_SPOT)을 쓰도록 맞춤 - 추가한 장소가
                            // 기록 화면에 바로 보이게 됨
                            pingViewModel.addSpotToTrip(

                                routeId = routeId,

                                spotId = place.spotId,

                                latitude = place.latitude,

                                longitude = place.longitude,

                                onSuccess = {

                                    Toast.makeText(
                                        context,
                                        "핑이 등록되었습니다!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }
                            )
                        }
                    )
                }


                // ========================================================
                // 👈 새로 추가: Ping 로그 후기 작성/수정
                // ========================================================

                composable(
                    PING_LOG_REVIEW_ROUTE,

                    arguments = listOf(

                        navArgument("pingId") {
                            type = NavType.LongType
                        },

                        navArgument("placeName") {

                            type = NavType.StringType

                            defaultValue = ""
                        }
                    )

                ) { backStackEntry ->


                    val pingId =
                        backStackEntry.arguments
                            ?.getLong("pingId")
                            ?: 0L

                    val placeName =
                        backStackEntry.arguments
                            ?.getString("placeName")
                            ?: "핑로그"

                    val pingViewModel:
                            com.tripping.app.viewmodel.PingViewModel =
                        viewModel()

                    val context =
                        LocalContext.current

                    // 👈 새로 추가: 진입 시 이미 등록된 후기가 있는지 먼저 확인
                    LaunchedEffect(pingId) {
                        pingViewModel.loadExistingReview(pingId)
                    }

                    val existingReview = pingViewModel.existingReview
                    val isCheckingExisting = pingViewModel.isCheckingExistingReview

                    if (isCheckingExisting) {

                        // 확인 끝나기 전까지는 빈 화면(잠깐 대기) - 초기값이 안 채워진 채로
                        // 화면이 먼저 그려지는 걸 방지하기 위함
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else {

                        PingLogReviewScreen(

                            placeName = placeName,

                            initialRating = existingReview?.rating,

                            initialContent = existingReview?.reviewComment,

                            initialTags = existingReview?.tags ?: emptyList(),

                            isEditMode = existingReview != null,

                            onBackClick = {

                                navController.popBackStack()
                            },

                            onSubmit = { content, rating, tags ->

                                if (existingReview != null) {

                                    pingViewModel.updateReview(

                                        pingId = pingId,

                                        rating = rating,

                                        content = content,

                                        tags = tags,

                                        onSuccess = {

                                            Toast.makeText(
                                                context,
                                                "후기가 수정되었습니다!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            navController.popBackStack()
                                        }
                                    )
                                } else {

                                    pingViewModel.submitReview(

                                        pingId = pingId,

                                        rating = rating,

                                        content = content,

                                        tags = tags,

                                        onSuccess = {

                                            Toast.makeText(
                                                context,
                                                "후기가 등록되었습니다!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            navController.popBackStack()
                                        }
                                    )
                                }
                            },

                            onSubmitAndNext = { content, rating, tags ->

                                if (existingReview != null) {

                                    pingViewModel.updateReview(
                                        pingId = pingId,
                                        rating = rating,
                                        content = content,
                                        tags = tags,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "후기가 수정되었습니다!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.popBackStack()
                                        }
                                    )
                                } else {

                                    pingViewModel.submitReview(
                                        pingId = pingId,
                                        rating = rating,
                                        content = content,
                                        tags = tags,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "후기가 등록되었습니다!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }


                // ========================================================
                // ⭐ 루트 생성 화면
                // ========================================================

                composable(
                    ROUTE_CREATE_ROUTE
                ) {

                    RouteCreateScreen(
                        onBackClick = {
                            navigateToTab(navController, "home")
                        },
                        onNextClick = { request ->
                            routeCreateViewModel.requestRecommend(request) {
                                navController.navigate("route_recommend")
                            }
                        }
                    )
                }

                composable("route_recommend") {

                    val routes by routeCreateViewModel.recommendedRoutes.collectAsState()

                    RouteRecommendScreen(
                        routes = routes,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onApplyClick = { selectedRoute ->
                            routeCreateViewModel.selectRoute(selectedRoute)
                            navController.navigate("route_map_applied")
                        },
                        onSkipClick = {
                            routeCreateViewModel.startEmptyRoute()
                            navController.navigate("route_map_edit")
                        }
                    )
                }

                // ========================================================
                // ⭐ 적용된 루트 지도 화면
                // ========================================================

                composable("route_map_applied") {

                    val selectedRoute by routeCreateViewModel.selectedRoute.collectAsState()
                    val routeSaved by routeCreateViewModel.routeSaved.collectAsState()

                    selectedRoute?.let { route ->
                        RouteMapAppliedScreen(
                            route = route,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onSaveRouteClick = {
                                routeCreateViewModel.saveRoute()
                            },
                            onStartTripClick = {
                                routeCreateViewModel.startTripNow(
                                    onSuccess = { actualRouteId ->
                                        navigateToTripStart(navController, tripId = actualRouteId, title = route.theme ?: route.label)
                                    },
                                    onError = { message ->
                                        // TODO: 에러 토스트/스낵바 등으로 사용자에게 알려주기
                                        android.util.Log.e("ROUTE_DEBUG", "여행 시작 실패: $message")
                                    }
                                )
                            },
                            showSavedDialog = routeSaved,
                            onDismissSavedDialog = {
                                routeCreateViewModel.resetRouteSaved()
                                navigateToTab(navController, "home")
                            }
                        )
                    }
                }

                // ========================================================
                // ⭐ 추천 없이 직접 만드는 루트 지도 화면
                // ========================================================

                composable("route_map_edit") {

                    val places by routeCreateViewModel.editablePlaces.collectAsState()
                    val saved by routeCreateViewModel.savedPlaces.collectAsState()
                    val regionId by routeCreateViewModel.lastRegionId.collectAsState()

                    RouteMapEditScreen(
                        initialRegionText = regionId,
                        routePlaces = places,
                        onRoutePlacesChange = { routeCreateViewModel.updateEditablePlaces(it) },
                        onSearchBarClick = {
                            navController.navigate("route_place_search")
                        },
                        onAddPlace = { routeCreateViewModel.addPlaceToRoute(it) },
                        savedPlaces = saved,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNextClick = { title ->
                            routeCreateViewModel.selectRoute(
                                RecommendedRoute(
                                    id = "manual",
                                    label = title,
                                    theme = title,
                                    places = places
                                )
                            )
                            navController.navigate("route_map_applied")
                        }
                    )
                }

                // ========================================================
                // ⭐ 루트에 추가할 장소 검색 화면
                // ========================================================

                composable("route_place_search") {

                    val query by routeCreateViewModel.searchQuery.collectAsState()
                    val results by routeCreateViewModel.searchResults.collectAsState()

                    RoutePlaceSearchScreen(
                        query = query,
                        onQueryChange = { routeCreateViewModel.onSearchQueryChange(it) },
                        results = results,
                        onAddPlace = { routeCreateViewModel.addPlaceToRoute(it) },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            // ============================================================
            // ⭐ 루트 버튼 메뉴
            //
            // NavHost 위에 표시되므로 현재 화면 위에 메뉴가 뜸.
            // 실제 하단 위치는 RouteMenuSheet 내부 padding으로 조절.
            // ============================================================

            if (showRouteMenu) {

                // 화면 전체를 덮는 투명 오버레이: 이걸 누르면 메뉴 닫힘
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            showRouteMenu = false
                        }
                )

                RouteMenuSheet(

                    modifier = Modifier.align(Alignment.BottomCenter),

                    onDismiss = {

                        showRouteMenu = false
                    },

                    onCreateRoute = {

                        showRouteMenu = false

                        navController.navigate(
                            ROUTE_CREATE_ROUTE
                        )
                    },

                    onViewRoute = {

                        showRouteMenu = false

                        // TODO:
                        // 루트 보기 화면 연결 예정
                    }
                )
            }
        }
    }
}