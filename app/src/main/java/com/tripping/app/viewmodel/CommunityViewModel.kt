// [파일 설명] Ping "로그" 탭(로그 커뮤니티) 상태/로직 담당. 지역별 공개 루트 목록 + 루트 상세의 후기 목록.
package com.tripping.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.RegionResponse
import com.tripping.app.data.response.RouteReviewResponse
import com.tripping.app.data.response.RouteSummaryResponse
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    // ===== 지역 선택 드롭다운 =====
    var regions by mutableStateOf<List<RegionResponse>>(emptyList())
        private set

    var selectedRegion by mutableStateOf<RegionResponse?>(null)
        private set

    // ===== "로그 커뮤니티" 목록 =====
    var routes by mutableStateOf<List<RouteSummaryResponse>>(emptyList())
        private set

    var isLoadingRoutes by mutableStateOf(false)
        private set

    // ===== "로그 상세보기" 후기 목록 =====
    var reviews by mutableStateOf<List<RouteReviewResponse>>(emptyList())
        private set

    var isLoadingReviews by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** 로그 커뮤니티 화면 진입 시: 지역 목록 불러오고, 첫 지역을 기본 선택해서 그 지역 루트도 같이 불러옴 */
    fun loadInitialRegions() {
        if (regions.isNotEmpty()) return // 이미 불러왔으면 재요청 안 함
        viewModelScope.launch {
            try {
                val response = RetrofitClient.regionApi.getAllRegions()
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    regions = body
                    val first = body.firstOrNull()
                    if (first != null) {
                        selectRegion(first)
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "지역 목록을 불러오지 못했습니다."
            }
        }
    }

    /** 지역 드롭다운에서 다른 지역 선택 시 */
    fun selectRegion(region: RegionResponse) {
        selectedRegion = region
        loadRoutesByRegion(region.regionId)
    }

    private fun loadRoutesByRegion(regionId: String) {
        viewModelScope.launch {
            isLoadingRoutes = true
            errorMessage = null
            try {
                val response = RetrofitClient.communityApi.getRoutesByRegion(regionId)
                routes = if (response.isSuccessful) response.body()?.content ?: emptyList() else emptyList()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "루트 목록을 불러오지 못했습니다."
                routes = emptyList()
            } finally {
                isLoadingRoutes = false
            }
        }
    }

    /** "로그 상세보기" 화면 진입 시: 그 루트의 후기 목록을 불러옴 */
    fun loadReviews(routeId: Long) {
        viewModelScope.launch {
            isLoadingReviews = true
            errorMessage = null
            try {
                val response = RetrofitClient.communityApi.getRouteReviews(routeId)
                reviews = if (response.isSuccessful) response.body()?.content ?: emptyList() else emptyList()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "후기를 불러오지 못했습니다."
                reviews = emptyList()
            } finally {
                isLoadingReviews = false
            }
        }
    }
}
