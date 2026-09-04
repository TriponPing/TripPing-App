package com.tripping.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel

// 하단 네비게이션 바를 보여줄 라우트. 로그인/회원가입 화면에는 안 보여줌.
private val ROUTES_WITH_BOTTOM_BAR = setOf("home", "mypage", "trip_history", "my_map_detail")

// "마이" 탭 쪽에 속하는 화면들 (마이페이지에서 파생된 화면이라 하단 바도 "마이"가 선택된 상태로 보여줘야 함)
private val MY_TAB_ROUTES = setOf("mypage", "trip_history", "my_map_detail")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // 하단 네비게이션 바는 여기 한 곳에서만 그림 (화면마다 따로 그리면 콜백 연결 누락되기 쉬워서 통일함)
    Scaffold(
        bottomBar = {
            if (currentRoute in ROUTES_WITH_BOTTOM_BAR) {
                AppBottomNavBar(
                    selectedTab = if (currentRoute in MY_TAB_ROUTES) AppBottomNavTab.MY else AppBottomNavTab.HOME,
                    onHomeClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onMyClick = {
                        navController.navigate("mypage")
                    }
                    // TODO: 탐색/루트/Ping 화면 생기면 onSearchClick/onRouteClick/onPingClick도 여기서 연결
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
                        navController.navigate("mypage")
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
        }
    }
}
