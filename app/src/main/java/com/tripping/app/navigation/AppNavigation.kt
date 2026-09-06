package com.tripping.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.tripping.app.ui.screen.CourseDetailScreen
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.MyMapDetailScreen
import com.tripping.app.ui.screen.MyPageScreen
import com.tripping.app.ui.screen.NearbyCoursesScreen
import com.tripping.app.ui.screen.PingCourseDetailScreen
import com.tripping.app.ui.screen.PingHistoryScreen // 👈 1. 임포트 추가됨
import com.tripping.app.ui.screen.PingPlaceSearchScreen
import com.tripping.app.ui.screen.PingScreen
import com.tripping.app.ui.screen.PopularKeywordsScreen
import com.tripping.app.ui.screen.PopularPlacesScreen
import com.tripping.app.ui.screen.PopularRoutesScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.PlaceSearchScreen
import com.tripping.app.ui.screen.SettingsScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.ui.screen.TripStartScreen
import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel

private const val COURSE_DETAIL_ROUTE = "course_detail/{tripId}?title={title}"
private const val TRIP_START_ROUTE = "trip_start/{tripId}?title={title}"
// 👈 Ping 탭 전용 "코스 상세" 라우트 (마이페이지 쪽 course_detail과는 별개 - Ping 탭 안에서만 이동)
private const val PING_COURSE_DETAIL_ROUTE = "ping_course_detail/{routeId}?title={title}"
// 👈 코스 상세 화면 "+" 버튼 -> 이 라우트로 이동해서 장소 검색 후 핑 추가
private const val PING_ADD_PLACE_ROUTE = "ping_add_place/{routeId}"
// 👈 2. Ping 탭 전용 "다녀온 여행 목록" 라우트 추가
private const val PING_HISTORY_ROUTE = "ping_history"

// 바텀바를 보여줄 라우트와, 그 라우트가 어떤 탭에 해당하는지 매핑
// 이 맵에 없는 라우트(로그인/회원가입 등)는 바텀바가 자동으로 안 보임
private val bottomBarRoutes = mapOf(
    "home" to AppBottomNavTab.HOME,
    "mypage" to AppBottomNavTab.MY,
    "trip_history" to AppBottomNavTab.MY,
    "my_map_detail" to AppBottomNavTab.MY,
    COURSE_DETAIL_ROUTE to AppBottomNavTab.MY,
    TRIP_START_ROUTE to AppBottomNavTab.MY,
    "settings" to AppBottomNavTab.MY,
    "ping" to AppBottomNavTab.PING,
    "placeSearch" to AppBottomNavTab.SEARCH,
    "popular_keywords" to AppBottomNavTab.HOME,
    "nearby_courses" to AppBottomNavTab.HOME,
    "popular_routes" to AppBottomNavTab.HOME,
    "popular_places" to AppBottomNavTab.HOME,
    PING_COURSE_DETAIL_ROUTE to AppBottomNavTab.PING, // 👈 Ping 탭 소속이라 눌러도 바텀바 Ping 탭 유지됨
    PING_ADD_PLACE_ROUTE to AppBottomNavTab.PING,
    PING_HISTORY_ROUTE to AppBottomNavTab.PING // 👈 3. 다녀온 여행 목록도 Ping 탭 소속으로 지정
)

// "다녀온 여행" 카드 클릭 -> 코스 상세보기 화면으로 이동 (마이페이지/다녀온 여행 자세히보기 둘 다 공용)
private fun navigateToCourseDetail(navController: NavController, tripId: Long, title: String) {
    navController.navigate("course_detail/$tripId?title=${Uri.encode(title)}")
}

// "여행 바로 시작하기" 클릭 -> 다음 페이지(큰 지도 + 여행 계획 수정하기)로 이동
private fun navigateToTripStart(navController: NavController, tripId: Long, title: String) {
    navController.navigate("trip_start/$tripId?title=${Uri.encode(title)}")
}

// 👈 Ping 탭 하단 카드("경복궁" 등) 클릭 -> Ping 전용 코스 상세(PingCourseDetailScreen)로 이동
private fun navigateToPingCourseDetail(navController: NavController, routeId: Long, title: String) {
    navController.navigate("ping_course_detail/$routeId?title=${Uri.encode(title)}")
}

// 👈 코스 상세 화면 "+" 버튼 -> 장소 검색(핑 추가 모드)으로 이동
private fun navigateToPingAddPlace(navController: NavController, routeId: Long) {
    navController.navigate("ping_add_place/$routeId")
}

// 👈 4. Ping 탭의 "다녀온 여행" 화면으로 이동하는 함수 추가
private fun navigateToPingHistory(navController: NavController) {
    navController.navigate(PING_HISTORY_ROUTE)
}

// 바텀 탭 전환 공통 로직: 이미 떠 있는 화면 재사용 + 백스택 중복 쌓임 방지
private fun navigateToTab(navController: NavController, route: String) {
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
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentTab = bottomBarRoutes[currentRoute]

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (currentTab != null) {
                AppBottomNavBar(
                    selectedTab = currentTab,
                    onHomeClick = { navigateToTab(navController, "home") },
                    onSearchClick = { navigateToTab(navController, "placeSearch") },
                    onRouteClick = { /* TODO: 루트 화면 아직 없음 */ },
                    onPingClick = { navigateToTab(navController, "ping") },
                    onMyClick = { navigateToTab(navController, "mypage") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                val authViewModel: AuthViewModel = viewModel()
                val loginState by authViewModel.loginState.collectAsState()

                LaunchedEffect(loginState) {
                    if (loginState is AuthState.Success) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    onLoginClick = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onSignUpLinkClick = {
                        navController.navigate("signup")
                    },
                    isLoading = loginState is AuthState.Loading,
                    errorMessage = (loginState as? AuthState.Error)?.message
                )
            }

            composable("signup") {
                val authViewModel: AuthViewModel = viewModel()
                val signUpState by authViewModel.signUpState.collectAsState()

                LaunchedEffect(signUpState) {
                    if (signUpState is AuthState.Success) {
                        navController.popBackStack()
                    }
                }

                SignUpScreen(
                    onSignUpClick = { nickname, email, password ->
                        authViewModel.signUp(nickname, email, password)
                    },
                    onLoginLinkClick = {
                        navController.popBackStack()
                    },
                    isLoading = signUpState is AuthState.Loading,
                    errorMessage = (signUpState as? AuthState.Error)?.message
                )
            }

            composable("home") {
                HomeScreen(
                    onStartRouteClick = { /* TODO: 루트 생성 - 1) 정보 입력 화면 아직 없음 */ },
                    onViewRouteClick = { /* TODO: 진행 중인 여행의 루트 상세 화면 아직 없음 */ },
                    onPingClick = { navigateToTab(navController, "ping") },
                    onSeeAllPopularRoutes = { navController.navigate("popular_routes") },
                    onSeeAllPopularPlaces = { navController.navigate("popular_places") },
                    onSeeAllKeywords = { navController.navigate("popular_keywords") },
                    onSeeAllNearbyCourses = { navController.navigate("nearby_courses") },
                    onCourseClick = { /* TODO: 코스 상세 화면 아직 없음 */ }
                )
            }

            composable("popular_keywords") {
                PopularKeywordsScreen(
                    onBackClick = { navController.popBackStack() },
                    onCourseClick = { /* TODO: 코스 상세 화면 아직 없음 */ }
                )
            }

            composable("nearby_courses") {
                NearbyCoursesScreen(
                    onBackClick = { navController.popBackStack() },
                    onCourseClick = { /* TODO: 코스 상세 화면 아직 없음 */ }
                )
            }

            composable("popular_routes") {
                PopularRoutesScreen(
                    onBackClick = { navController.popBackStack() },
                    onCourseClick = { /* TODO: 코스 상세 화면 아직 없음 */ }
                )
            }

            composable("popular_places") {
                PopularPlacesScreen(
                    onBackClick = { navController.popBackStack() },
                    onPlaceClick = { /* TODO: 장소 상세조회 화면 만들면 여기서 이동 처리 */ }
                )
            }

            composable("placeSearch") {
                PlaceSearchScreen(
                    onPlaceClick = { spotId ->
                        // TODO: 장소 상세조회 화면 만들면 여기서 이동 처리
                    }
                )
            }

            composable("mypage") {
                MyPageScreen(
                    onOpenSettings = {
                        navController.navigate("settings")
                    },
                    onOpenTripHistory = {
                        navController.navigate("trip_history")
                    },
                    onOpenMyMap = {
                        navController.navigate("my_map_detail")
                    },
                    onOpenTripDetail = { tripId, title ->
                        navigateToCourseDetail(navController, tripId, title)
                    }
                )
            }

            composable("settings") {
                val authViewModel: AuthViewModel = viewModel()
                val logoutState by authViewModel.logoutState.collectAsState()

                LaunchedEffect(logoutState) {
                    if (logoutState is AuthState.Success) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
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

            composable("trip_history") {
                TripHistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onOpenTripDetail = { tripId, title ->
                        navigateToCourseDetail(navController, tripId, title)
                    }
                )
            }

            composable(
                COURSE_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("tripId") { type = NavType.LongType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
                val title = backStackEntry.arguments?.getString("title")
                CourseDetailScreen(
                    tripId = tripId,
                    fallbackTitle = title?.ifBlank { null },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onStartTrip = { id, t ->
                        navigateToTripStart(navController, id, t)
                    },
                    onGoToPing = {
                        navigateToTab(navController, "ping")
                    }
                )
            }

            composable(
                TRIP_START_ROUTE,
                arguments = listOf(
                    navArgument("tripId") { type = NavType.LongType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
                val title = backStackEntry.arguments?.getString("title")
                TripStartScreen(
                    tripId = tripId,
                    fallbackTitle = title?.ifBlank { null },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onGoToPing = {
                        navigateToTab(navController, "ping")
                    }
                )
            }

            composable("my_map_detail") {
                MyMapDetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("ping") {
                PingScreen(
                    onAddPingClick = { /* TODO: 핑 추가 로직 */ },
                    onPingLogClick = { pingId -> /* TODO: 핑로그 상세로 이동 */ },
                    onRouteCardClick = { routeId, title ->
                        navigateToPingCourseDetail(navController, routeId, title)
                    },
                    onCourseClick = { courseId -> /* TODO: 코스 상세로 이동 */ },
                    onMoreClick = { // 👈 바로가기를 눌렀을 때 다녀온 여행 화면으로 이동하도록 연결!
                        navigateToPingHistory(navController)
                    }
                )
            }

            // 👈 Ping 탭 전용 "다녀온 여행 목록" 화면 연결
            composable(PING_HISTORY_ROUTE) {
                PingHistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onTripClick = { tripId: Long, title: String ->
                        navigateToPingCourseDetail(navController, tripId, title)
                    }
                )
            }

            // 👈 새로 추가: Ping 탭 하단 "경복궁" 카드 눌렀을 때 이동하는 코스 상세 화면
            composable(
                PING_COURSE_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("routeId") { type = NavType.LongType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val routeId = backStackEntry.arguments?.getLong("routeId") ?: 0L
                val title = backStackEntry.arguments?.getString("title") ?: "여행 기록"
                PingCourseDetailScreen(
                    routeId = routeId,
                    courseName = title,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddPingClick = {
                        navigateToPingAddPlace(navController, routeId)
                    },
                    onPingLogClick = { pingId -> /* TODO: 핑로그 상세로 이동 */ }
                )
            }

            // 👈 새로 추가: "+" 버튼 눌렀을 때 - 지도에서 장소 골라서 그 코스(routeId)에 핑 등록
            composable(
                PING_ADD_PLACE_ROUTE,
                arguments = listOf(
                    navArgument("routeId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val routeId = backStackEntry.arguments?.getLong("routeId") ?: 0L
                val pingViewModel: com.tripping.app.viewmodel.PingViewModel = viewModel()
                val context = LocalContext.current

                PingPlaceSearchScreen(
                    onPlaceSelected = { place ->
                        pingViewModel.addSpotToTrip(
                            routeId = routeId,
                            spotId = place.spotId,
                            latitude = place.latitude,
                            longitude = place.longitude,
                            onSuccess = {
                                Toast.makeText(context, "과거 여행에 추가되었습니다!", Toast.LENGTH_SHORT).show()
                                // 등록 성공 -> 코스 상세 화면으로 돌아감 (돌아가면 LaunchedEffect가 재조회해서 새 스팟이 바로 보임)
                                navController.popBackStack()
                            }
                        )
                    }
                )
            }
        }
    }
}