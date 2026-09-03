package com.tripping.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tripping.app.ui.screen.HomeScreen
import com.tripping.app.ui.screen.LoginScreen
import com.tripping.app.ui.screen.MyMapDetailScreen
import com.tripping.app.ui.screen.MyPageScreen
import com.tripping.app.ui.screen.SignUpScreen
import com.tripping.app.ui.screen.TripHistoryScreen
import com.tripping.app.viewmodel.AuthState
import com.tripping.app.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
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
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
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
                },
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("my_map_detail") {
            MyMapDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}