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

    fun requestRecommend(request: RouteRecommendRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = RetrofitClient.routeApi.recommendRoutes(request)
                _recommendedRoutes.value = result.map { candidate ->
                    RecommendedRoute(
                        id = candidate.candidateOrder.toString(),
                        label = "추천${candidate.candidateOrder}",
                        totalTimeLabel = formatMinutes(candidate.totalWalkTimeMinutes),
                        moveTimeLabel = "이동 거리 %.1fkm".format(candidate.totalDistanceKm),
                        places = candidate.spots
                            .sortedBy { it.visitOrder }
                            .map { spot ->
                                RoutePlaceItem(
                                    order = spot.visitOrder,
                                    name = spot.name,
                                    tags = listOf(spot.category)
                                )
                            }
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "추천 루트를 불러오지 못했어요: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "총 ${h}시간 ${m}분" else "총 ${m}분"
    }
}