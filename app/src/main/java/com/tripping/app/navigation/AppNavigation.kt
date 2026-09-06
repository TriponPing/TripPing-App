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

import com.tripping.app.ui.component.AppBottomNavBar
import com.tripping.app.ui.component.AppBottomNavTab
import com.tripping.app.ui.component.RouteMenuSheet

import com.tripping.app.ui.screen.CourseDetailScreen
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.MyMapDetailScreen
import com.tripping.app.ui.screen.MyPageScreen
import com.tripping.app.ui.screen.NearbyCoursesScreen
import com.tripping.app.ui.screen.PingCourseDetailScreen
import com.tripping.app.ui.screen.PingHistoryScreen
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

import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel


private const val COURSE_DETAIL_ROUTE = "course_detail/{tripId}?title={title}"
private const val TRIP_START_ROUTE = "trip_start/{tripId}?title={title}"

private const val PING_COURSE_DETAIL_ROUTE =
    "ping_course_detail/{routeId}?title={title}"

private const val PING_ADD_PLACE_ROUTE =
    "ping_add_place/{routeId}"

private const val PING_HISTORY_ROUTE =
    "ping_history"

private const val ROUTE_CREATE_ROUTE =
    "route_create"


// 바텀바를 보여줄 라우트와 현재 선택된 탭을 연결
private val bottomBarRoutes = mapOf(
    "home" to AppBottomNavTab.HOME,

    "placeSearch" to AppBottomNavTab.SEARCH,

    ROUTE_CREATE_ROUTE to AppBottomNavTab.ROUTE,

    "ping" to AppBottomNavTab.PING,
    PING_COURSE_DETAIL_ROUTE to AppBottomNavTab.PING,
    PING_ADD_PLACE_ROUTE to AppBottomNavTab.PING,
    PING_HISTORY_ROUTE to AppBottomNavTab.PING,

    "mypage" to AppBottomNavTab.MY,
    "trip_history" to AppBottomNavTab.MY,
    "my_map_detail" to AppBottomNavTab.MY,
    COURSE_DETAIL_ROUTE to AppBottomNavTab.MY,
    TRIP_START_ROUTE to AppBottomNavTab.MY,
    "settings" to AppBottomNavTab.MY,

    "popular_keywords" to AppBottomNavTab.HOME,
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


// Ping 코스 상세 → 장소 추가
private fun navigateToPingAddPlace(
    navController: NavController,
    routeId: Long
) {
    navController.navigate(
        "ping_add_place/$routeId"
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


    Scaffold(
        containerColor = Color.White,

        bottomBar = {

            if (currentTab != null) {

                AppBottomNavBar(

                    selectedTab = currentTab,

                    onHomeClick = {
                        navigateToTab(
                            navController,
                            "home"
                        )
                    },

                    onSearchClick = {
                        navigateToTab(
                            navController,
                            "placeSearch"
                        )
                    },

                    // ⭐ 루트 버튼
                    onRouteClick = {
                        showRouteMenu = true
                    },

                    onPingClick = {
                        navigateToTab(
                            navController,
                            "ping"
                        )
                    },

                    onMyClick = {
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


                    LaunchedEffect(signUpState) {

                        if (signUpState is AuthState.Success) {

                            navController.popBackStack()
                        }
                    }


                    SignUpScreen(

                        onSignUpClick = {
                                nickname,
                                email,
                                password ->

                            authViewModel.signUp(
                                nickname,
                                email,
                                password
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

                        onSeeAllKeywords = {

                            navController.navigate(
                                "popular_keywords"
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
                        }
                    )
                }


                // ========================================================
                // 인기 키워드
                // ========================================================

                composable("popular_keywords") {

                    PopularKeywordsScreen(

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onCourseClick = {
                            // TODO
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

                        onCourseClick = {
                            // TODO
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

                        onPlaceClick = {
                            // TODO
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

                        onAddPingClick = {
                            // TODO
                        },

                        onPingLogClick = { pingId ->
                            // TODO
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

                        onCourseClick = {
                            // TODO
                        },

                        onMoreClick = {

                            navigateToPingHistory(
                                navController
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

                        onPingLogClick = {
                            // TODO
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
                // ⭐ 루트 생성 화면
                // ========================================================

                composable(
                    ROUTE_CREATE_ROUTE
                ) {

                    RouteCreateScreen()
                }
            }


            // ============================================================
            // ⭐ 루트 버튼 메뉴
            //
            // NavHost 위에 표시되므로 현재 화면 위에 메뉴가 뜸.
            // 실제 하단 위치는 RouteMenuSheet 내부 padding으로 조절.
            // ============================================================

            if (showRouteMenu) {

                RouteMenuSheet(

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