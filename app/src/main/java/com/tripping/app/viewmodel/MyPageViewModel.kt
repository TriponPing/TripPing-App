package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.MapDetailResponse
import com.tripping.app.data.response.MapPinResponse
import com.tripping.app.data.response.MapSearchResponse
import com.tripping.app.data.response.ProfileResponse
import com.tripping.app.data.response.SavedRouteResponse
import com.tripping.app.data.response.TripSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {

    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile

    private val _recentTrips = MutableStateFlow<List<TripSummaryResponse>>(emptyList())
    val recentTrips: StateFlow<List<TripSummaryResponse>> = _recentTrips

    private val _allTrips = MutableStateFlow<List<TripSummaryResponse>>(emptyList())
    val allTrips: StateFlow<List<TripSummaryResponse>> = _allTrips

    private val _savedRoutes = MutableStateFlow<List<SavedRouteResponse>>(emptyList())
    val savedRoutes: StateFlow<List<SavedRouteResponse>> = _savedRoutes

    private val _savedRoutesTotal = MutableStateFlow(0)
    val savedRoutesTotal: StateFlow<Int> = _savedRoutesTotal

    private val _visitedPlaceCount = MutableStateFlow(0)
    val visitedPlaceCount: StateFlow<Int> = _visitedPlaceCount

    // "나의 여행 지도 자세히보기" 화면용 - 지도에 찍을 핀 전체 목록
    private val _mapPins = MutableStateFlow<List<MapPinResponse>>(emptyList())
    val mapPins: StateFlow<List<MapPinResponse>> = _mapPins

    // type("drawn" | "saved")별 상세 경로 캐시 - 한 번 불러온 타입은 다시 안 불러옴
    private val _mapDetailByType = MutableStateFlow<Map<String, List<MapDetailResponse>>>(emptyMap())
    val mapDetailByType: StateFlow<Map<String, List<MapDetailResponse>>> = _mapDetailByType

    private val _mapSearchResults = MutableStateFlow<List<MapSearchResponse>>(emptyList())
    val mapSearchResults: StateFlow<List<MapSearchResponse>> = _mapSearchResults

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** 마이페이지 진입 시 한 번에 다 불러오기 */
    fun loadAll() {
        loadProfile()
        loadRecentTrips()
        loadSavedRoutes()
        loadMyMap()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getProfile()
                if (response.isSuccessful) {
                    _profile.value = response.body()
                } else {
                    _errorMessage.value = "프로필을 불러오지 못했습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun loadRecentTrips() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getRecentTrips()
                if (response.isSuccessful) {
                    _recentTrips.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    /** "다녀온 여행 자세히보기" 화면용 전체 목록 (지금은 1페이지만, 페이징 UI는 나중에) */
    fun loadAllTrips() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getAllTrips()
                if (response.isSuccessful) {
                    _allTrips.value = response.body()?.content ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun loadSavedRoutes() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getSavedRoutes()
                if (response.isSuccessful) {
                    val body = response.body()
                    _savedRoutes.value = body?.content ?: emptyList()
                    _savedRoutesTotal.value = body?.totalElements?.toInt() ?: 0
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun loadMyMap() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getMyMap()
                if (response.isSuccessful) {
                    val pins = response.body() ?: emptyList()
                    _mapPins.value = pins
                    _visitedPlaceCount.value = pins.size
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    /** "여행 보기" / "내가 그린 루트 보기" 버튼용 - type = "drawn"(다녀온 여행) | "saved"(저장한 루트) */
    fun loadMapDetail(type: String) {
        if (_mapDetailByType.value.containsKey(type)) return // 이미 불러온 타입은 재요청 안 함
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getMyMapDetail(type)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _mapDetailByType.value = _mapDetailByType.value + (type to list)
                } else {
                    _errorMessage.value = "경로를 불러오지 못했습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun searchMyMap(keyword: String) {
        if (keyword.isBlank()) {
            _mapSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.searchMyMap(keyword)
                if (response.isSuccessful) {
                    _mapSearchResults.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun clearMapSearch() {
        _mapSearchResults.value = emptyList()
    }

    /** 저장한 루트 취소 (성공하면 목록 새로고침) */
    fun unsaveRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.unsaveRoute(routeId)
                if (response.isSuccessful) {
                    loadSavedRoutes()
                } else {
                    _errorMessage.value = "저장 취소에 실패했습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
