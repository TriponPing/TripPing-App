package com.tripping.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tripping.app.ui.component.AppBottomNavBar
import com.tripping.app.ui.component.AppBottomNavTab
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.MyMapDetailScreen
import com.tripping.app.ui.screen.MyPageScreen
import com.tripping.app.ui.screen.PingScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.PlaceSearchScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel

// 바텀바를 보여줄 라우트와, 그 라우트가 어떤 탭에 해당하는지 매핑
// 이 맵에 없는 라우트(로그인/회원가입 등)는 바텀바가 자동으로 안 보임
private val bottomBarRoutes = mapOf(
    "home" to AppBottomNavTab.HOME,
    "mypage" to AppBottomNavTab.MY,
    "trip_history" to AppBottomNavTab.MY,
    "my_map_detail" to AppBottomNavTab.MY,
    "ping" to AppBottomNavTab.PING,
    "placeSearch" to AppBottomNavTab.SEARCH
)

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
                    onGoToMyPageClick = {
                        navigateToTab(navController, "mypage")
                    },
                    onNavigateToPlaceSearch = {
                        navigateToTab(navController, "placeSearch")
                    }
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
                    onOpenTripHistory = {
                        navController.navigate("trip_history")
                    },
                    onOpenMyMap = {
                        navController.navigate("my_map_detail")
                    }
                )
            }

            composable("trip_history") {
                TripHistoryScreen(
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
                    onRouteCardClick = { routeId -> /* TODO: 루트 상세로 이동 */ },
                    onCourseClick = { courseId -> /* TODO: 코스 상세로 이동 */ }
                )
            }
        }
    }
}