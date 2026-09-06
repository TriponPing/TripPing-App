package com.tripping.app.ui.model

// ===== 홈 화면 전용 모델 =====
// 홈/인기 키워드/내 주변 코스 관련 API가 아직 백엔드에 없어서(페이지 분담 기준 "시작 전"),
// 화면 구조를 먼저 잡기 위한 임시 모델. API 나오면 필드 맞춰서 교체하면 됨.
// 필드/문구는 피그마 목업(node-id 19:2, 108:793, 74:3178, 88:3521) 텍스트를 그대로 사용함.

// 키워드 칩 - selected=true면 피그마상 채워진(#5AA2D9) 스타일, false면 아웃라인 스타일
data class HomeKeyword(
    val text: String,
    val selected: Boolean = false
)

// "내 주변 코스" / "인기 키워드" 화면에서 공통으로 쓰는 코스 카드 모델
data class HomeCourseCard(
    val id: Int,
    val authorName: String?, // null이면 작성자 행 자체를 숨김 (내 주변 코스 화면 카드가 이 케이스)
    val courseName: String,
    val stops: List<String>,
    val tags: List<HomeKeyword>,
    val pingCount: Int,
    val distanceKm: Double,
    val bookmarkCount: Int
)
