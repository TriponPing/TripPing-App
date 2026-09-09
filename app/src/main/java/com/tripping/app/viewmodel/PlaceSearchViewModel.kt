package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.data.response.RouteMapSearchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 화면 상단 탭과 서버 category 값 매핑
enum class PlaceCategory(val label: String, val serverValue: String) {
    ATTRACTION("관광지", "attraction"),
    RESTAURANT("맛집", "restaurant"),
    CAFE("카페", "cafe");

    companion object {
        fun labelOf(serverValue: String): String =
            entries.find { it.serverValue == serverValue }?.label ?: serverValue
    }
}

class PlaceSearchViewModel : ViewModel() {

    private val _places = MutableStateFlow<List<PlaceSearchResponse>>(emptyList())
    val places: StateFlow<List<PlaceSearchResponse>> = _places

    private val _routes = MutableStateFlow<List<RouteMapSearchResponse>>(emptyList())
    val routes: StateFlow<List<RouteMapSearchResponse>> = _routes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 처음엔 선택된 카테고리 없음 (null = 아무것도 선택 안 됨 -> 전체 루트/핑 표시)
    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory

    private val _selectedRegionId = MutableStateFlow<String?>(null)
    val selectedRegionId: StateFlow<String?> = _selectedRegionId

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot

    private val _selectedMinPingCount = MutableStateFlow<Int?>(null)
    val selectedMinPingCount: StateFlow<Int?> = _selectedMinPingCount

    // 같은 카테고리를 다시 누르면 선택 해제 -> 전체 루트/장소 다시 로드
    fun onCategorySelected(category: PlaceCategory) {
        if (_selectedCategory.value == category) {
            _selectedCategory.value = null
            _places.value = emptyList()
            loadSavedRoutesAndPlaces()
        } else {
            _selectedCategory.value = category
            loadPlaces()
        }
    }

    fun onRegionSelected(regionId: String?) {
        _selectedRegionId.value = regionId
        if (_selectedCategory.value != null) loadPlaces() else loadSavedRoutesAndPlaces()
    }

    fun onTimeSlotSelected(timeSlot: String?) {
        _selectedTimeSlot.value = timeSlot
        if (_selectedCategory.value != null) loadPlaces()
    }

    fun onMinPingCountSelected(minPingCount: Int?) {
        _selectedMinPingCount.value = minPingCount
        if (_selectedCategory.value != null) loadPlaces()
    }

    // 카테고리 선택 상태: 해당 카테고리로 장소 검색 + 루트 표시
    fun loadPlaces() {
        val category = _selectedCategory.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val placeResult = RetrofitClient.placeApi.getPlaces(
                    category = category.serverValue,
                    regionId = _selectedRegionId.value,
                    timeSlot = _selectedTimeSlot.value,
                    minPingCount = _selectedMinPingCount.value
                )
                _places.value = placeResult
                _routes.value = emptyList() // 카테고리 선택 시 루트는 표시 안 함
            } catch (e: Exception) {
                _errorMessage.value = "장소를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 카테고리 미선택 상태: 사용사 저장 루트(및 거기 포함된 장소들) 로드
    fun loadSavedRoutesAndPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routes.value = RetrofitClient.routeApi.getSavedRoutesForMap()
                _savedIndividualPlaces.value = RetrofitClient.routeApi.getSavedPlaces()
            } catch (e: Exception) {
                _errorMessage.value = "저장한 루트/장소를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _savedIndividualPlaces = MutableStateFlow<List<PlaceSearchResponse>>(emptyList())
    val savedIndividualPlaces: StateFlow<List<PlaceSearchResponse>> = _savedIndividualPlaces
}