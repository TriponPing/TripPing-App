package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.PlaceDetailResponse
import com.tripping.app.data.response.PopularTripDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Ping "로그" 탭 카드 -> 로그 상세보기(PingLogDetailScreen) 전용 뷰모델.
// 헤더 카드(작성자/경유지)는 PopularTripDetailViewModel과 같은 GET /trips/{routeId}를 쓰지만
// (그 화면의 주석대로 이 API는 "탐색에서 보는 남의 공개 루트 상세"라 로그 상세보기에도 그대로 맞음),
// 여기서는 추가로 장소별 상세(후기 가로 스와이프용)를 spotId 단위로 따로 불러와야 해서 별도 뷰모델로 분리함.
class PingLogDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<PopularTripDetailResponse?>(null)
    val detail: StateFlow<PopularTripDetailResponse?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 상세조회 API엔 저장 여부가 없어서, 마이페이지 "저장한 여행" 목록과 대조해서 복원함
    // (PopularTripDetailViewModel과 동일한 패턴)
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    // spotId -> 장소 상세(평균 별점/후기 목록 포함). 페이지 넘길 때마다 다시 안 불러오도록 캐싱.
    private val _placeDetails = MutableStateFlow<Map<Long, PlaceDetailResponse>>(emptyMap())
    val placeDetails: StateFlow<Map<Long, PlaceDetailResponse>> = _placeDetails

    private val _loadingPlaceIds = MutableStateFlow<Set<Long>>(emptySet())
    val loadingPlaceIds: StateFlow<Set<Long>> = _loadingPlaceIds

    fun loadDetail(routeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _detail.value = RetrofitClient.routeApi.getTripDetail(routeId)
            } catch (e: Exception) {
                _errorMessage.value = "루트 정보를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        checkSaved(routeId)
    }

    private fun checkSaved(routeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getSavedRoutes(page = 0, size = 100)
                if (response.isSuccessful) {
                    _isSaved.value = response.body()?.content.orEmpty().any { it.tripId == routeId }
                }
            } catch (e: Exception) {
                // 초기 상태 복원용이라 실패해도 기본값(저장 안 됨)으로 두고 넘어감
            }
        }
    }

    fun toggleSaveRoute(routeId: Long) {
        if (_isSaved.value) unsaveRoute(routeId) else saveRoute(routeId)
    }

    private fun saveRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.saveRoute(routeId)
                if (response.isSuccessful) {
                    _isSaved.value = true
                }
            } catch (e: Exception) {
                // 실패하면 상태를 안 바꿔서 아이콘이 실제 서버 상태와 어긋나지 않게 함
            }
        }
    }

    private fun unsaveRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.unsaveRoute(routeId)
                if (response.isSuccessful) {
                    _isSaved.value = false
                }
            } catch (e: Exception) {
                // 실패하면 상태를 안 바꿔서 아이콘이 실제 서버 상태와 어긋나지 않게 함
            }
        }
    }

    // 장소 선택(가로 스와이프)용: 이미 불러온 장소는 다시 요청하지 않음
    fun loadPlaceDetail(spotId: Long) {
        if (_placeDetails.value.containsKey(spotId) || _loadingPlaceIds.value.contains(spotId)) return

        viewModelScope.launch {
            _loadingPlaceIds.value = _loadingPlaceIds.value + spotId
            try {
                val result = RetrofitClient.placeApi.getPlaceDetail(spotId)
                _placeDetails.value = _placeDetails.value + (spotId to result)
            } catch (e: Exception) {
                // 실패해도 다른 장소 스와이프엔 지장 없게 조용히 넘어감
            } finally {
                _loadingPlaceIds.value = _loadingPlaceIds.value - spotId
            }
        }
    }
}
