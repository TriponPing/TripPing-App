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

    private val _selectedCategory = MutableStateFlow(PlaceCategory.ATTRACTION)
    val selectedCategory: StateFlow<PlaceCategory> = _selectedCategory

    private val _selectedRegionId = MutableStateFlow<String?>(null) // null = 전체
    val selectedRegionId: StateFlow<String?> = _selectedRegionId

    fun onCategorySelected(category: PlaceCategory) {
        _selectedCategory.value = category
        loadPlaces()
    }

    fun onRegionSelected(regionId: String?) {
        _selectedRegionId.value = regionId
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val placeResult = RetrofitClient.placeApi.getPlaces(
                    category = _selectedCategory.value.serverValue,
                    regionId = _selectedRegionId.value
                )
                _places.value = placeResult

                // 지도에 표시할 루트(경로)도 같이 불러오기
                val routeResult = RetrofitClient.routeApi.searchRoutesForMap(
                    regionId = _selectedRegionId.value,
                    category = _selectedCategory.value.serverValue
                )
                _routes.value = routeResult
            } catch (e: Exception) {
                _errorMessage.value = "장소를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}