package com.tripping.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.tripping.app.ui.screen.PingScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel

private const val COURSE_DETAIL_ROUTE = "course_detail/{tripId}?title={title}"

// 바텀바를 보여줄 라우트와, 그 라우트가 어떤 탭에 해당하는지 매핑
// 이 맵에 없는 라우트(로그인/회원가입 등)는 바텀바가 자동으로 안 보임
private val bottomBarRoutes = mapOf(
    "home" to AppBottomNavTab.HOME,
    "mypage" to AppBottomNavTab.MY,
    "trip_history" to AppBottomNavTab.MY,
    "my_map_detail" to AppBottomNavTab.MY,
    COURSE_DETAIL_ROUTE to AppBottomNavTab.MY,
    "ping" to AppBottomNavTab.PING
)

// "다녀온 여행" 카드 클릭 -> 코스 상세보기 화면으로 이동 (마이페이지/다녀온 여행 자세히보기 둘 다 공용)
private fun navigateToCourseDetail(navController: NavController, tripId: Long, title: String) {
    navController.navigate("course_detail/$tripId?title=${Uri.encode(title)}")
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

    // 하단 네비게이션 바는 여기 한 곳에서만 그림 (화면마다 따로 그리면 콜백 연결 누락되기 쉬워서 통일함)
    Scaffold(
        bottomBar = {
            if (currentTab != null) {
                AppBottomNavBar(
                    selectedTab = currentTab,
                    onHomeClick = { navigateToTab(navController, "home") },
                    onSearchClick = { /* TODO: 탐색 화면 아직 없음 */ },
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
                            // 로그인 화면을 백스택에서 제거
                            // → 홈 화면에서 뒤로가기 눌러도 로그인 화면으로 안 돌아가고 앱 종료됨
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
                        // 회원가입 성공 -> 로그인 화면으로 돌아가서 로그인하게 함
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
                    onGoToMyPageClick = {
                        navigateToTab(navController, "mypage")
                    }
                )
            }

            composable("mypage") {
                MyPageScreen(
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
                    onRouteCardClick = { routeId -> /* TODO: 루트 상세로 이동 */ }
                )
            }
        }
    }
}
