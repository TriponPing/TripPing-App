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
    }

    // TODO: 실제 "루트 저장(찜)" API 연동 (SavedRoute 관련 기능 확인 필요)
    fun saveRoute(routeId: Long) {
        // 아직 API 연동 전
    }
}