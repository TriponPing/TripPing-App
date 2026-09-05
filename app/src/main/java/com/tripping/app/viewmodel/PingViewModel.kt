package com.tripping.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
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

    /** Ping 탭 진입 시: 가장 최근 다녀온 여행 + 그 여행의 전체 핑 기록을 함께 불러옴 */
    fun loadRecentTripWithPings() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1) 가장 최근 다녀온 여행 하나 조회
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

                // 2) 그 여행의 전체 핑 기록 조회 (List<PingDto>가 직접 옴)
                pings = RetrofitClient.pingApi.getTripPingHistory(trip.tripId)

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "데이터를 불러오지 못했습니다."
                pings = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}