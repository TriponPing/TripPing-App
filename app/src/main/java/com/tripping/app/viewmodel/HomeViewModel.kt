package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.CurrentTripSummaryResponse
import com.tripping.app.data.response.PopularTripResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 홈 화면 상태 관리. 섹션 하나씩 순서대로 실제 API에 연결하는 중.
 *   [1번 섹션] GET /trips/current-summary -> 진행 중인 여행 (없으면 204 -> currentTrip = null)
 *   [2번 섹션] GET /auth/me -> 인사말 닉네임 (AuthApi는 기존 파일 그대로, 호출만 함)
 *   [3번 섹션] GET /trips/popular -> 이번 주 인기 루트
 */
class HomeViewModel : ViewModel() {

    private val _nickname = MutableStateFlow<String?>(null)
    val nickname: StateFlow<String?> = _nickname

    private val _currentTrip = MutableStateFlow<CurrentTripSummaryResponse?>(null)
    val currentTrip: StateFlow<CurrentTripSummaryResponse?> = _currentTrip

    private val _popularTrips = MutableStateFlow<List<PopularTripResponse>>(emptyList())
    val popularTrips: StateFlow<List<PopularTripResponse>> = _popularTrips

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadNickname() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.getMe()
                if (response.isSuccessful) {
                    _nickname.value = response.body()?.nickname
                }
            } catch (e: Exception) {
                // 인사말용 부가 정보라 실패해도 화면은 기본 문구로 대체됨 (에러 배너 안 띄움)
            }
        }
    }

    fun loadCurrentTrip() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getCurrentTripSummary()
                // 200이면 진행 중인 여행 있음, 204(body == null)면 없음 -> 둘 다 정상 상태
                _currentTrip.value = if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "진행 중인 여행을 불러오지 못했습니다."
            }
        }
    }

    fun loadPopularTrips() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getPopularTrips()
                _popularTrips.value = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                // 목록형 섹션이라 실패해도 빈 목록으로 두고 에러 배너는 안 띄움
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
