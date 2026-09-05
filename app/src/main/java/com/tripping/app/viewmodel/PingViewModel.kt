// [파일 설명] Ping 탭(기록) 화면의 상태/로직 담당. 최근 다녀온 여행+핑 목록 조회, 핑 등록, 핑 후기 등록/수정을 처리함.
package com.tripping.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.CreatePingRequest
import com.tripping.app.data.request.PingReviewRequest
import com.tripping.app.data.response.PingDto
import com.tripping.app.data.response.TripSummaryResponse
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {

    // 가장 최근 다녀온 여행 정보 (이름/날짜/장소수 등 - 카드 표시용)
    var recentTrip by mutableStateOf<TripSummaryResponse?>(null)
        private set

    // 그 여행의 핑 기록 목록 (타임라인/리스트 표시용)
    var pings by mutableStateOf<List<PingDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 방금 등록/수정에 성공했는지 화면에서 알 수 있도록 하는 이벤트성 상태
    var reviewSubmitSuccess by mutableStateOf(false)
        private set

    /** Ping 탭 진입 시: 가장 최근 다녀온 여행 + 그 여행의 전체 핑 기록을 함께 불러옴 */
    fun loadRecentTripWithPings() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val tripsResponse = RetrofitClient.myPageApi.getRecentTrips()
                if (!tripsResponse.isSuccessful) {
                    errorMessage = "여행 정보를 불러오지 못했습니다. (${tripsResponse.code()})"
                    recentTrip = null
                    pings = emptyList()
                    return@launch
                }

                val trip = tripsResponse.body()?.firstOrNull()
                recentTrip = trip

                if (trip == null) {
                    pings = emptyList()
                    return@launch
                }

                pings = RetrofitClient.pingApi.getTripPingHistory(trip.tripId)

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "데이터를 불러오지 못했습니다."
                pings = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 진행 중인 여행(routeId)에 새 방문 장소를 핑으로 등록.
     * 성공하면 방금 등록한 PingDto를 콜백으로 넘겨줌 (다음 화면에서 pingId가 필요하므로).
     */
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

    /** 핑 후기 등록 (실제 PingReviewRequest는 content 필드 하나뿐 - 사진/별점은 아직 API에 없음) */
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

    /** 핑 후기 수정 */
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