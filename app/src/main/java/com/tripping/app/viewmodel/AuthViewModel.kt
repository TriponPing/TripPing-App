package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.LoginRequest
import com.tripping.app.data.request.SignUpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState: StateFlow<AuthState> = _signUpState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    _loginState.value = AuthState.Success
                } else {
                    _loginState.value = AuthState.Error("이메일 또는 비밀번호가 올바르지 않습니다.")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun signUp(nickname: String, email: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.signUp(SignUpRequest(email, password, nickname))
                if (response.isSuccessful) {
                    _signUpState.value = AuthState.Success
                } else {
                    _signUpState.value = AuthState.Error("회원가입에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = AuthState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}