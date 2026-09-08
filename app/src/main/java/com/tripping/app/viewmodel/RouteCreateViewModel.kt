package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.RouteRecommendRequest
import com.tripping.app.ui.screen.RecommendedRoute
import com.tripping.app.ui.screen.RoutePlaceItem
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.data.request.PlannedRouteCreateRequest
import com.tripping.app.data.request.PlannedRoutePlaceRequest
import com.tripping.app.data.request.TripCreateRequest
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
        loadSavedPlaces()
    }

    fun updateEditablePlaces(places: List<RoutePlaceItem>) {
        _editablePlaces.value = places
    }

    fun addPlaceToRoute(place: RoutePlaceItem) {
        val nextOrder = _editablePlaces.value.size + 1
        _editablePlaces.value = _editablePlaces.value + place.copy(order = nextOrder)
    }

    // 지도 초기 중심을 위해, 방금 요청한 regionId를 기억해둠
    private val _lastRegionId = MutableStateFlow("R01")
    val lastRegionId: StateFlow<String> = _lastRegionId

    // 검색 / 저장한 장소는 아직 백엔드 API가 없어서 우선 빈 상태로 둠 (나중에 실제 API 연결)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<PlaceSearchResponse>>(emptyList())
    val searchResults: StateFlow<List<PlaceSearchResponse>> = _searchResults

    private val _savedPlaces = MutableStateFlow<List<RoutePlaceItem>>(emptyList())
    val savedPlaces: StateFlow<List<RoutePlaceItem>> = _savedPlaces

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            try {
                _searchResults.value = RetrofitClient.routeApi.searchPlaces(
                    query = query,
                    regionId = _lastRegionId.value
                )
            } catch (e: Exception) {
                android.util.Log.e("ROUTE_DEBUG", "장소 검색 실패: ${e.message}", e)
                _searchResults.value = emptyList()
            }
        }
    }

    private val _lastRequest = MutableStateFlow<RouteRecommendRequest?>(null)
    fun requestRecommend(request: RouteRecommendRequest, onSuccess: () -> Unit) {
        _lastRegionId.value = request.regionId ?: "R01"
        _lastRequest.value = request
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
                        theme = candidate.theme,
                        places = candidate.spots
                            .sortedBy { it.visitOrder }
                            .map { spot ->
                                RoutePlaceItem(
                                    order = spot.visitOrder,
                                    name = spot.name,
                                    tags = listOf(spot.category),
                                    latitude = spot.latitude,
                                    longitude = spot.longitude,
                                    spotId = spot.spotId
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

    fun loadSavedPlaces() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.routeApi.getSavedPlaces()
                _savedPlaces.value = response.map { r ->
                    RoutePlaceItem(
                        order = 0,
                        name = r.name,
                        tags = listOf(r.category),
                        latitude = r.latitude,
                        longitude = r.longitude,
                        spotId = r.spotId
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ROUTE_DEBUG", "저장한 장소 조회 실패: ${e.message}", e)
            }
        }
    }

    private val _routeSaved = MutableStateFlow(false)
    val routeSaved: StateFlow<Boolean> = _routeSaved

    private val _savedRouteId = MutableStateFlow<Long?>(null)
    val savedRouteId: StateFlow<Long?> = _savedRouteId

    // 아직 저장 안 됐으면 저장하고, 이미 저장했으면 그 routeId 재사용
    private suspend fun ensureRouteSaved(): Long {
        _savedRouteId.value?.let { return it }

        val route = _selectedRoute.value ?: throw IllegalStateException("선택된 루트가 없습니다")

        val created = RetrofitClient.routeApi.createPlannedRoute(
            PlannedRouteCreateRequest(
                title = route.theme ?: route.label,
                candidateId = route.id.toLongOrNull() // 추천 결과면 숫자, 직접 만든 루트("manual")면 null
            )
        )

        for (place in route.places.sortedBy { it.order }) {
            val spotId = place.spotId ?: continue // spotId 없는 장소는 건너뜀 (있으면 안 되지만 방어)
            RetrofitClient.routeApi.addPlannedRoutePlace(
                routeId = created.routeId,
                request = PlannedRoutePlaceRequest(spotId = spotId, visitOrder = place.order)
            )
        }

        _savedRouteId.value = created.routeId
        return created.routeId
    }

    fun saveRoute() {
        viewModelScope.launch {
            try {
                ensureRouteSaved()
                _routeSaved.value = true
            } catch (e: Exception) {
                android.util.Log.e("ROUTE_DEBUG", "루트 저장 실패: ${e.message}", e)
            }
        }
    }

    fun resetRouteSaved() {
        _routeSaved.value = false
    }

    fun startTripNow(onSuccess: (Long) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val routeId = ensureRouteSaved()
                val req = _lastRequest.value

                val trip = RetrofitClient.routeApi.createTrip(
                    routeId = routeId,
                    request = TripCreateRequest(
                        travelDate = toIsoDate(req?.startDate),
                        companionType = req?.companionType,
                        transport = req?.transport,
                        memberCount = req?.memberCount
                    )
                )

                onSuccess(trip.actualRouteId)
            } catch (e: Exception) {
                android.util.Log.e("ROUTE_DEBUG", "여행 시작 실패: ${e.message}", e)
                onError(e.message ?: "여행을 시작하지 못했어요")
            }
        }
    }

    // "2024.06.01 (토)" 같은 표시용 날짜를 "2024-06-01"(ISO) 형식으로 변환
    private fun toIsoDate(raw: String?): String {
        val datePart = raw?.substringBefore(" ")?.trim() ?: return ""
        return datePart.replace(".", "-").trim('-')
    }

}