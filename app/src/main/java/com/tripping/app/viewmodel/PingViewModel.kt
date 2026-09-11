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
import com.tripping.app.data.response.PingReviewResponse
import com.tripping.app.data.response.TripDetailResponse
import com.tripping.app.data.response.TripSummaryResponse
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {

    // ===== "다녀온 여행" 요약 (하단 카드용, ACTUAL_ROUTE_SPOT 기반) =====
    var recentTrip by mutableStateOf<TripSummaryResponse?>(null)
        private set

    // ===== "진행중인 여행"의 전체 일정 (상단 타임라인용, ACTUAL_ROUTE_SPOT 기반 - WIDGET_PING 아님) =====
    var ongoingRouteId by mutableStateOf<Long?>(null)
        private set

    var hasOngoingTrip by mutableStateOf(false)
        private set

    var ongoingTripSpots by mutableStateOf<List<TripDetailResponse.SpotDetail>>(emptyList())
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

    // ===== 👈 새로 추가: 기존 후기 조회 (등록/수정 모드 판단용) =====
    var existingReview by mutableStateOf<PingReviewResponse?>(null)
        private set

    var isCheckingExistingReview by mutableStateOf(true)
        private set

    /** Ping 탭(기록) 진입 시: 진행중인 여행 + 가장 최근 다녀온 여행을 각각 따로 불러옴 */
    fun loadPingTabData() {
        loadOngoingTrip()
        loadRecentTrip()
    }

    /** 진행중인 여행이 있는지 조회하고, 있으면 그 여행의 전체 일정(ACTUAL_ROUTE_SPOT)도 함께 불러옴 */
    private fun loadOngoingTrip() {
        viewModelScope.launch {
            try {
                val current = RetrofitClient.pingApi.getCurrentInProgressRoute()
                ongoingRouteId = current.actualRouteId
                hasOngoingTrip = true

                // 👈 WIDGET_PING이 아니라, "다녀온 여행 상세"랑 같은 API로 이 여행의 전체 방문 일정을 가져옴
                val detailResponse = RetrofitClient.myPageApi.getTripDetail(current.actualRouteId)
                ongoingTripSpots = if (detailResponse.isSuccessful) {
                    detailResponse.body()?.spots ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                // 진행중인 여행이 없으면 서버가 404 등을 줄 수 있음 - 정상적인 "없음" 상태로 처리
                ongoingRouteId = null
                hasOngoingTrip = false
                ongoingTripSpots = emptyList()
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

    /** 👈 새로 추가: 진행중인 여행에 새 장소를 추가하는 화면(PingPlaceSearchScreen)에서, 지금까지
     * 찍은 핑들을 지도에 선으로 이어서 보여주기 위해 씀. loadPingTabData()와 별개로 그 화면
     * 진입 시 routeId를 이미 알고 있으니 바로 불러옴. */
    fun loadTripSpotsForMap(routeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getTripDetail(routeId)
                ongoingTripSpots = if (response.isSuccessful) response.body()?.spots ?: emptyList() else emptyList()
            } catch (e: Exception) {
                ongoingTripSpots = emptyList()
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

    /** 핑 카드 "···" -> 삭제. "기록"(진행중) 탭과 "코스 상세"(완료된 여행) 화면 양쪽에서 씀 */
    fun deleteTripSpot(routeId: Long, actualRouteSpotId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                RetrofitClient.pingApi.deleteTripSpot(routeId, actualRouteSpotId)
                // 로컬 목록에서 바로 제거 - 재조회 없이 즉시 반영 (둘 중 실제로 들어있는 목록만 걸러짐)
                ongoingTripSpots = ongoingTripSpots.filter { it.actualRouteSpotId != actualRouteSpotId }
                tripSpots = tripSpots.filter { it.actualRouteSpotId != actualRouteSpotId }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "기록 삭제 중 오류가 발생했습니다."
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

    /** 핑로그 후기 화면 진입 시: 이미 등록된 후기가 있는지 확인 (있으면 수정 모드로 전환하는 데 씀) */
    fun loadExistingReview(pingId: Long) {
        viewModelScope.launch {
            isCheckingExistingReview = true
            try {
                existingReview = RetrofitClient.pingApi.getPingReview(pingId)
            } catch (e: Exception) {
                // 404 등 - 아직 후기가 없다는 뜻, 정상적인 "없음" 상태로 처리
                existingReview = null
            } finally {
                isCheckingExistingReview = false
            }
        }
    }

    /** 핑 후기 등록 - 별점(필수)/후기 텍스트/태그를 서버 스펙에 맞게 전송 */
    fun submitReview(
        pingId: Long,
        rating: Int,
        content: String,
        tags: List<String> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            reviewSubmitSuccess = false
            try {
                RetrofitClient.pingApi.createPingReview(
                    pingId = pingId,
                    request = PingReviewRequest(
                        rating = rating,
                        photoUrl = null,
                        reviewComment = content.ifBlank { null },
                        tags = tags
                    )
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

    /** 핑 후기 수정 */
    fun updateReview(
        pingId: Long,
        rating: Int,
        content: String,
        tags: List<String> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            reviewSubmitSuccess = false
            try {
                RetrofitClient.pingApi.updatePingReview(
                    pingId = pingId,
                    request = PingReviewRequest(
                        rating = rating,
                        photoUrl = null,
                        reviewComment = content.ifBlank { null },
                        tags = tags
                    )
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