package com.tripping.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.response.CurrentTripSummaryResponse
import com.tripping.app.data.response.PopularKeywordResponse
import com.tripping.app.data.response.PopularPlaceResponse
import com.tripping.app.data.response.PopularTripResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 홈 화면 상태 관리. 섹션 하나씩 순서대로 실제 API에 연결하는 중.
 *   [1번 섹션] GET /trips/current-summary -> 진행 중인 여행 (없으면 204 -> currentTrip = null)
 *   [2번 섹션] GET /auth/me -> 인사말 닉네임 (AuthApi는 기존 파일 그대로, 호출만 함)
 *   [3번 섹션] GET /trips/popular -> 이번 주 인기 루트
 *   [4번 섹션] GET /keyword/popular -> 이번 주 인기 키워드
 *   [더보기] GET /places/popular -> 떠오르는 인기 장소 (저장 수 기준 TOP N)
 */
class HomeViewModel : ViewModel() {

    private val _nickname = MutableStateFlow<String?>(null)
    val nickname: StateFlow<String?> = _nickname

    private val _currentTrip = MutableStateFlow<CurrentTripSummaryResponse?>(null)
    val currentTrip: StateFlow<CurrentTripSummaryResponse?> = _currentTrip

    private val _popularTrips = MutableStateFlow<List<PopularTripResponse>>(emptyList())
    val popularTrips: StateFlow<List<PopularTripResponse>> = _popularTrips

    // 내가 저장한 루트 id 집합. "이 루트를 이미 저장했는지" 조회하는 API가 따로 없어서,
    // 화면 진입 시 GET /users/me/routes/saved 목록을 통째로 불러와 tripId만 뽑아 초기화함.
    private val _savedRouteIds = MutableStateFlow<Set<Long>>(emptySet())
    val savedRouteIds: StateFlow<Set<Long>> = _savedRouteIds

    // trips/popular의 savedCount는 화면 진입 시점 스냅샷이라, 이 화면에서 저장/취소한 만큼만
    // (+1/-1) 보정해서 보여줌. loadSavedRouteIds로 복원된, 원래부터 저장돼있던 루트는 그
    // 스냅샷에 이미 포함돼 있으므로 델타를 안 건드림.
    private val _savedCountDeltas = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val savedCountDeltas: StateFlow<Map<Long, Int>> = _savedCountDeltas

    private val _popularKeywords = MutableStateFlow<List<PopularKeywordResponse>>(emptyList())
    val popularKeywords: StateFlow<List<PopularKeywordResponse>> = _popularKeywords

    private val _popularPlaces = MutableStateFlow<List<PopularPlaceResponse>>(emptyList())
    val popularPlaces: StateFlow<List<PopularPlaceResponse>> = _popularPlaces

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadNickname() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.getMe()
                if (response.isSuccessful) {
                    _nickname.value = response.body()?.nickname
                }
            } catch (e: Exception) {
                // 인사말용 부가 정보라 실패해도 화면은 기본 문구로 대체됨 (에러 배너 안 띄움)
            }
        }
    }

    fun loadCurrentTrip() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getCurrentTripSummary()
                // 200이면 진행 중인 여행 있음, 204(body == null)면 없음 -> 둘 다 정상 상태
                _currentTrip.value = if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "진행 중인 여행을 불러오지 못했습니다."
            }
        }
    }

    fun loadPopularTrips(limit: Int = 10) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getPopularTrips(limit = limit)
                _popularTrips.value = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                // 목록형 섹션이라 실패해도 빈 목록으로 두고 에러 배너는 안 띄움
            }
        }
    }

    fun loadPopularKeywords(limit: Int = 10) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getPopularKeywords(limit = limit)
                _popularKeywords.value = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                // 목록형 섹션이라 실패해도 빈 목록으로 두고 에러 배너는 안 띄움
            }
        }
    }

    fun loadPopularPlaces(limit: Int = 30) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getPopularPlaces(limit = limit)
                _popularPlaces.value = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                // 목록형 섹션이라 실패해도 빈 목록으로 두고 에러 배너는 안 띄움
            }
        }
    }

    // 화면 진입 시 한 번 불러와서 북마크 초기 상태(채워짐/빈 상태)를 정확하게 복원함.
    // size는 넉넉하게 100 - 페이지네이션까지 구현할 정도로 저장 개수가 많은 상황은 아직 아님.
    fun loadSavedRouteIds() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.myPageApi.getSavedRoutes(page = 0, size = 100)
                if (response.isSuccessful) {
                    _savedRouteIds.value = response.body()?.content
                        ?.map { it.tripId }
                        ?.toSet()
                        ?: emptySet()
                }
            } catch (e: Exception) {
                // 초기 상태 복원용이라 실패해도 빈 집합(전부 안 채워진 상태)으로 두고 넘어감
            }
        }
    }

    // 북마크 탭: 저장 안 된 상태면 POST로 저장, 저장된 상태면 DELETE로 저장 취소.
    // 마이페이지 "저장한 여행"은 이 API가 실제로 저장하는 대상이라 바로 반영됨.
    fun toggleSaveRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                if (_savedRouteIds.value.contains(routeId)) {
                    val response = RetrofitClient.myPageApi.unsaveRoute(routeId)
                    if (response.isSuccessful) {
                        _savedRouteIds.value = _savedRouteIds.value - routeId
                        _savedCountDeltas.value = _savedCountDeltas.value + (routeId to -1)
                    }
                } else {
                    val response = RetrofitClient.myPageApi.saveRoute(routeId)
                    if (response.isSuccessful) {
                        _savedRouteIds.value = _savedRouteIds.value + routeId
                        _savedCountDeltas.value = _savedCountDeltas.value + (routeId to 1)
                    }
                }
            } catch (e: Exception) {
                // 실패하면 상태를 안 바꿔서 아이콘/카운트가 실제 서버 상태와 어긋나지 않게 함
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
