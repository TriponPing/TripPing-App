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

    // 👈 새로 추가: "내 주변 코스" - 진행 중인 여행의 마지막으로 찍은 핑 위치 기준으로 채워짐
    // (loadCurrentTrip()에서 좌표가 있으면 자동으로 같이 불러옴). 찍은 핑이 없으면 빈 목록.
    private val _nearbyCourses = MutableStateFlow<List<PopularTripResponse>>(emptyList())
    val nearbyCourses: StateFlow<List<PopularTripResponse>> = _nearbyCourses

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

    // 인기 키워드 더보기 화면에서 칩을 선택하면 그 키워드가 달린 루트 목록을 보여줌
    private val _selectedKeyword = MutableStateFlow<String?>(null)
    val selectedKeyword: StateFlow<String?> = _selectedKeyword

    private val _keywordRoutes = MutableStateFlow<List<PopularTripResponse>>(emptyList())
    val keywordRoutes: StateFlow<List<PopularTripResponse>> = _keywordRoutes

    private val _popularPlaces = MutableStateFlow<List<PopularPlaceResponse>>(emptyList())
    val popularPlaces: StateFlow<List<PopularPlaceResponse>> = _popularPlaces

    // 내가 저장한 장소 spotId 집합. savedRouteIds와 동일한 이유로, 화면 진입 시
    // GET /places/saved/me/ids 목록을 통째로 불러와 초기화함.
    private val _savedPlaceIds = MutableStateFlow<Set<Long>>(emptySet())
    val savedPlaceIds: StateFlow<Set<Long>> = _savedPlaceIds

    // places/popular의 savedCount도 화면 진입 시점 스냅샷이라, savedCountDeltas와 동일하게
    // 이 화면에서 저장/취소한 만큼만(+1/-1) 보정해서 보여줌.
    private val _savedPlaceCountDeltas = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val savedPlaceCountDeltas: StateFlow<Map<Long, Int>> = _savedPlaceCountDeltas

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
                val trip = if (response.isSuccessful) response.body() else null
                _currentTrip.value = trip

                // 👈 수정: "내 주변 코스"를 기기 현재 위치가 아니라 마지막으로 찍은 핑 위치 기준으로
                // 보여주기로 함. 진행 중인 여행이 없거나 아직 찍은 핑이 없으면(좌표 null) 비워둠.
                val lastLat = trip?.lastPingLatitude
                val lastLng = trip?.lastPingLongitude
                if (lastLat != null && lastLng != null) {
                    loadNearbyCourses(lastLat, lastLng)
                } else {
                    _nearbyCourses.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "진행 중인 여행을 불러오지 못했습니다."
            }
        }
    }

    // 👈 새로 추가: 기준 좌표(마지막으로 찍은 핑) 근처의 공개 루트를 거리순으로 불러옴.
    fun loadNearbyCourses(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getNearbyTrips(lat = lat, lng = lng)
                _nearbyCourses.value = if (response.isSuccessful) response.body()?.content ?: emptyList() else emptyList()
            } catch (e: Exception) {
                // 목록형 섹션이라 실패해도 빈 목록으로 두고 에러 배너는 안 띄움
                _nearbyCourses.value = emptyList()
            }
        }
    }

    // 👈 새로 추가: 홈 "Ping 찍기" 버튼 - 매번 장소를 고르지 않고, 계획된 방문 순서(visit_order)
    // 대로 큐처럼 다음 장소 하나를 바로 확정해서 찍음. 성공하면 요약을 다시 불러와
    // 카운트/스테퍼가 바로 "다음 핑"을 반영하도록 함.
    fun confirmNextPing(onResult: (message: String, success: Boolean) -> Unit = { _, _ -> }) {
        val routeId = _currentTrip.value?.actualRouteId
        if (routeId == null) {
            onResult("진행 중인 여행이 없어요.", false)
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.pingApi.confirmNextPing(routeId)
                loadCurrentTrip() // 방금 찍은 핑이 카운트/스테퍼에 바로 반영되도록 다시 불러옴
                val placeName = response.spotName ?: "다음 장소"
                val message = if (response.hasNext) {
                    "\"$placeName\" 핑을 찍었어요! 다음은 \"${response.nextSpotName ?: "다음 장소"}\"예요."
                } else {
                    "\"$placeName\" 핑을 찍었어요! 이 루트의 마지막 장소예요."
                }
                onResult(message, true)
            } catch (e: retrofit2.HttpException) {
                val message = if (e.code() == 409) {
                    "찍을 수 있는 다음 장소가 없어요. 계획된 장소를 모두 찍었어요."
                } else {
                    e.message() ?: "핑을 찍지 못했어요."
                }
                onResult(message, false)
            } catch (e: Exception) {
                onResult(e.localizedMessage ?: "핑을 찍지 못했어요.", false)
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

    // 키워드 칩 선택: 같은 걸 다시 누르면 선택 해제(목록 닫힘)
    fun toggleKeywordSelection(keyword: String) {
        if (_selectedKeyword.value == keyword) {
            _selectedKeyword.value = null
            _keywordRoutes.value = emptyList()
            return
        }
        _selectedKeyword.value = keyword
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getRoutesByKeyword(keyword)
                _keywordRoutes.value = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                _keywordRoutes.value = emptyList()
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

    fun loadSavedPlaceIds() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.homeApi.getSavedPlaceIds()
                if (response.isSuccessful) {
                    _savedPlaceIds.value = response.body()?.toSet() ?: emptySet()
                }
            } catch (e: Exception) {
                // 초기 상태 복원용이라 실패해도 빈 집합(전부 안 채워진 상태)으로 두고 넘어감
            }
        }
    }

    // 북마크 탭: 저장 안 된 상태면 POST로 저장, 저장된 상태면 DELETE로 저장 취소. toggleSaveRoute와 동일한 패턴.
    fun toggleSavePlace(spotId: Long) {
        viewModelScope.launch {
            try {
                if (_savedPlaceIds.value.contains(spotId)) {
                    val response = RetrofitClient.homeApi.unsavePlace(spotId)
                    if (response.isSuccessful) {
                        _savedPlaceIds.value = _savedPlaceIds.value - spotId
                        _savedPlaceCountDeltas.value = _savedPlaceCountDeltas.value + (spotId to -1)
                    }
                } else {
                    val response = RetrofitClient.homeApi.savePlace(spotId)
                    if (response.isSuccessful) {
                        _savedPlaceIds.value = _savedPlaceIds.value + spotId
                        _savedPlaceCountDeltas.value = _savedPlaceCountDeltas.value + (spotId to 1)
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
