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

    // 이 장소가 이미 저장(북마크)돼 있는지. 상세조회 API엔 이 정보가 없어서
    // 마이페이지 "저장한 장소" 목록(GET /users/me/places/saved)을 불러와 placeId로 대조해서 복원함.
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

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
        checkSaved(placeId)
    }

    private fun checkSaved(placeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getSavedPlaces(page = 0, size = 100)
                if (response.isSuccessful) {
                    _isSaved.value = response.body()?.content.orEmpty().any { it.spotId == placeId }
                }
            } catch (e: Exception) {
                // 초기 상태 복원용이라 실패해도 기본값(저장 안 됨)으로 두고 넘어감
            }
        }
    }

    fun toggleSavePlace(placeId: Long) {
        if (_isSaved.value) unsavePlace(placeId) else savePlace(placeId)
    }

    private fun savePlace(placeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.savePlace(placeId)
                if (response.isSuccessful) {
                    _isSaved.value = true
                }
            } catch (e: Exception) {
                // 실패하면 상태를 안 바꿔서 아이콘이 실제 서버 상태와 어긋나지 않게 함
            }
        }
    }

    private fun unsavePlace(placeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.unsavePlace(placeId)
                if (response.isSuccessful) {
                    _isSaved.value = false
                }
            } catch (e: Exception) {
                // 실패하면 상태를 안 바꿔서 아이콘이 실제 서버 상태와 어긋나지 않게 함
            }
        }
    }
}