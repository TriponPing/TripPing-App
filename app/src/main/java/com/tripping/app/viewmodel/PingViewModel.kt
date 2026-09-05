package com.tripping.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.PingResponse
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {   // 👈 생성자 인자 제거

    var currentRoute by mutableStateOf<PingResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadOngoingPings(routeId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                currentRoute = RetrofitClient.pingApi.getOngoingPings(routeId)   // 👈 직접 참조
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "데이터를 불러오지 못했습니다."
                currentRoute = null
            } finally {
                isLoading = false
            }
        }
    }
}