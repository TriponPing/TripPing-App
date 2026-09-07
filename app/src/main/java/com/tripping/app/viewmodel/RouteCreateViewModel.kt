package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.RouteRecommendRequest
import com.tripping.app.ui.screen.RecommendedRoute
import com.tripping.app.ui.screen.RoutePlaceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RouteCreateViewModel : ViewModel() {

    private val _recommendedRoutes = MutableStateFlow<List<RecommendedRoute>>(emptyList())
    val recommendedRoutes: StateFlow<List<RecommendedRoute>> = _recommendedRoutes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // "적용" 눌렀을 때 선택된 루트를 들고 있다가, 지도 화면에서 꺼내 씀
    private val _selectedRoute = MutableStateFlow<RecommendedRoute?>(null)
    val selectedRoute: StateFlow<RecommendedRoute?> = _selectedRoute

    fun selectRoute(route: RecommendedRoute) {
        _selectedRoute.value = route
    }

    // "추천 안 받고 다음으로" 눌렀을 때 직접 채워나갈 빈 루트
    private val _editablePlaces = MutableStateFlow<List<RoutePlaceItem>>(emptyList())
    val editablePlaces: StateFlow<List<RoutePlaceItem>> = _editablePlaces

    fun startEmptyRoute() {
        _editablePlaces.value = emptyList()
    }

    fun updateEditablePlaces(places: List<RoutePlaceItem>) {
        _editablePlaces.value = places
    }

    fun addPlaceToRoute(place: RoutePlaceItem) {
        _editablePlaces.value = _editablePlaces.value + place
    }

    // 지도 초기 중심을 위해, 방금 요청한 regionId를 기억해둠
    private val _lastRegionId = MutableStateFlow("R01")
    val lastRegionId: StateFlow<String> = _lastRegionId

    // 검색 / 저장한 장소는 아직 백엔드 API가 없어서 우선 빈 상태로 둠 (나중에 실제 API 연결)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<RoutePlaceItem>>(emptyList())
    val searchResults: StateFlow<List<RoutePlaceItem>> = _searchResults

    private val _savedPlaces = MutableStateFlow<List<RoutePlaceItem>>(emptyList())
    val savedPlaces: StateFlow<List<RoutePlaceItem>> = _savedPlaces

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // TODO: 여기서 실제 검색 API 호출해서 _searchResults.value 채우기
    }

    fun requestRecommend(request: RouteRecommendRequest, onSuccess: () -> Unit) {
        _lastRegionId.value = request.regionId ?: "R01"
        android.util.Log.e("ROUTE_DEBUG", "requestRecommend 시작")
        android.util.Log.e("ROUTE_DEBUG", "요청 내용: ${com.google.gson.Gson().toJson(request)}")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.e("ROUTE_DEBUG", "API 호출 직전")
                val result = RetrofitClient.routeApi.recommendRoutes(request)
                android.util.Log.e("ROUTE_DEBUG", "API 응답 성공: ${result.size}개")
                _recommendedRoutes.value = result.map { candidate ->
                    RecommendedRoute(
                        id = candidate.candidateOrder.toString(),
                        label = "추천${candidate.candidateOrder}",
                        places = candidate.spots
                            .sortedBy { it.visitOrder }
                            .map { spot ->
                                RoutePlaceItem(
                                    order = spot.visitOrder,
                                    name = spot.name,
                                    tags = listOf(spot.category),
                                    latitude = spot.latitude,
                                    longitude = spot.longitude
                                )
                            }
                    )
                }
                android.util.Log.e("ROUTE_DEBUG", "onSuccess 호출 직전")
                onSuccess()
                android.util.Log.e("ROUTE_DEBUG", "onSuccess 호출 완료")
            } catch (e: Exception) {
                android.util.Log.e("ROUTE_DEBUG", "에러 발생: ${e.javaClass.simpleName} - ${e.message}", e)
                _errorMessage.value = "추천 루트를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}