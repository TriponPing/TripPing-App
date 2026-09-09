package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.PlaceDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaceDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<PlaceDetailResponse?>(null)
    val detail: StateFlow<PlaceDetailResponse?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadDetail(placeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _detail.value = RetrofitClient.placeApi.getPlaceDetail(placeId)
            } catch (e: Exception) {
                _errorMessage.value = "장소 정보를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}