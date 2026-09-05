// [파일 설명] Ping 탭(기록) 화면의 상태/로직 담당. 진행중인 여행의 핑 목록(WIDGET_PING)과, 가장 최근 다녀온 여행 요약(ACTUAL_ROUTE_SPOT)을 완전히 분리해서 관리함.
package com.tripping.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.AddTripSpotRequest
import com.tripping.app.data.request.CreatePingRequest
import com.tripping.app.data.request.PingReviewRequest
import com.tripping.app.data.response.PingDto
import com.tripping.app.data.response.TripDetailResponse
import com.tripping.app.data.response.TripSummaryResponse
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {

    // ===== "다녀온 여행" 요약 (하단 카드용, ACTUAL_ROUTE_SPOT 기반) =====
    var recentTrip by mutableStateOf<TripSummaryResponse?>(null)
        private set

    // ===== "진행중인 여행"의 핑 목록 (상단 타임라인용, WIDGET_PING 기반) =====
    var ongoingRouteId by mutableStateOf<Long?>(null)
        private set

    var hasOngoingTrip by mutableStateOf(false)
        private set

    var ongoingPings by mutableStateOf<List<PingDto>>(emptyList())
        private set

    // ===== 코스 상세 화면(PingCourseDetailScreen) 전용 - 완료된 여행의 확정된 방문 스팟 (ACTUAL_ROUTE_SPOT) =====
    var tripSpots by mutableStateOf<List<TripDetailResponse.SpotDetail>>(emptyList())
        private set

    // (더 이상 안 씀 - WIDGET_PING 기반이라 완료된 여행 상세랑 안 맞음. 대신 tripSpots 사용)
    var pings by mutableStateOf<List<PingDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var reviewSubmitSuccess by mutableStateOf(false)
        private set

    /** Ping 탭(기록) 진입 시: 진행중인 여행 + 가장 최근 다녀온 여행을 각각 따로 불러옴 */
    fun loadPingTabData() {
        loadOngoingTrip()
        loadRecentTrip()
    }

    /** 진행중인 여행이 있는지 조회하고, 있으면 그 여행의 핑 목록도 함께 불러옴 */
    private fun loadOngoingTrip() {
        viewModelScope.launch {
            try {
                val current = RetrofitClient.pingApi.getCurrentInProgressRoute()
                ongoingRouteId = current.actualRouteId
                hasOngoingTrip = true

                val ongoing = RetrofitClient.pingApi.getOngoingPings(current.actualRouteId)
                ongoingPings = ongoing.pings
            } catch (e: Exception) {
                // 진행중인 여행이 없으면 서버가 404 등을 줄 수 있음 - 정상적인 "없음" 상태로 처리
                ongoingRouteId = null
                hasOngoingTrip = false
                ongoingPings = emptyList()
            }
        }
    }

    /** 가장 최근 다녀온(완료된) 여행 요약만 불러옴 (핑 목록은 필요 없음 - 요약 카드용) */
    private fun loadRecentTrip() {
        viewModelScope.launch {
            try {
                val tripsResponse = RetrofitClient.myPageApi.getRecentTrips()
                if (tripsResponse.isSuccessful) {
                    recentTrip = tripsResponse.body()?.firstOrNull()
                } else {
                    recentTrip = null
                }
            } catch (e: Exception) {
                recentTrip = null
            }
        }
    }

    /** 진행중인 여행에 실시간으로 핑 찍기 (WIDGET_PING) */
    fun createPing(
        routeId: Long,
        spotId: Long,
        placeName: String,
        latitude: Double,
        longitude: Double,
        onSuccess: (PingDto) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val newPing = RetrofitClient.pingApi.createPing(
                    routeId = routeId,
                    request = CreatePingRequest(
                        spotId = spotId,
                        placeName = placeName,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
                onSuccess(newPing)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "핑 등록 중 오류가 발생했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    /** 완료된(다녀온) 여행에 놓친 방문 스팟 추가 (ACTUAL_ROUTE_SPOT) */
    fun addSpotToTrip(
        routeId: Long,
        spotId: Long,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                RetrofitClient.pingApi.addTripSpot(
                    routeId = routeId,
                    request = AddTripSpotRequest(
                        spotId = spotId,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "방문 스팟 추가 중 오류가 발생했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    /** 코스 상세 화면 진입 시: 완료된 여행(routeId)의 확정된 방문 스팟(ACTUAL_ROUTE_SPOT)을 불러옴 */
    fun loadTripSpots(routeId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.myPageApi.getTripDetail(routeId)
                if (response.isSuccessful) {
                    tripSpots = response.body()?.spots ?: emptyList()
                } else {
                    errorMessage = "여행 상세를 불러오지 못했습니다. (${response.code()})"
                    tripSpots = emptyList()
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "데이터를 불러오지 못했습니다."
                tripSpots = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun submitReview(
        pingId: Long,
        content: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            reviewSubmitSuccess = false
            try {
                RetrofitClient.pingApi.createPingReview(
                    pingId = pingId,
                    request = PingReviewRequest(content = content)
                )
                reviewSubmitSuccess = true
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "후기 등록 중 오류가 발생했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun updateReview(
        pingId: Long,
        content: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            reviewSubmitSuccess = false
            try {
                RetrofitClient.pingApi.updatePingReview(
                    pingId = pingId,
                    request = PingReviewRequest(content = content)
                )
                reviewSubmitSuccess = true
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "후기 수정 중 오류가 발생했습니다."
            } finally {
                isLoading = false
            }
        }
    }
}