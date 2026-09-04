package com.tripping.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.PlaceSearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    // TODO: 실제로는 여기서 로그인 API 호출 → 성공하면 아래 navigate 실행
                    // 지금은 네비게이션 테스트용으로 바로 홈으로 이동시킴
                    navController.navigate("home") {
                        // 로그인 화면을 백스택에서 제거
                        // → 홈 화면에서 뒤로가기 눌러도 로그인 화면으로 안 돌아가고 앱 종료됨
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpLinkClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpClick = { nickname, email, password ->
                    // TODO: 회원가입 API 호출
                    navController.popBackStack()
                },
                onLoginLinkClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToPlaceSearch = {
                    navController.navigate("placeSearch")
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
    }
}