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
                                    tags = listOf(spot.category)
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