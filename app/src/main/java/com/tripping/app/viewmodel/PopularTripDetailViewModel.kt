package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.PopularTripDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopularTripDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<PopularTripDetailResponse?>(null)
    val detail: StateFlow<PopularTripDetailResponse?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 이 루트가 이미 저장(북마크)돼 있는지. 상세조회 API엔 이 정보가 없어서
    // 마이페이지 "저장한 여행" 목록(GET /users/me/routes/saved)을 불러와 tripId로 대조해서
    // 복원함. HomeViewModel.savedRouteIds/loadSavedRouteIds와 동일한 패턴.
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

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

    // 저장 안 된 상태면 POST로 저장, 저장된 상태면 DELETE로 저장 취소.
    // _isSaved를 기준으로 판단해서 보내므로 버튼 연타로 같은 요청이 중복으로 나가지 않고,
    // 서버(MyPageRouteService.saveRoute)도 이미 저장돼 있으면 다시 넣지 않아 멱등하게 처리됨.
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
}