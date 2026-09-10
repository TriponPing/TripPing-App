// [파일 설명] 커뮤니티(regions/routes, routes/reviews) API는 우리 쪽 커스텀 PageResponse가 아니라
// Spring Data의 기본 Page<T> 직렬화 형태 그대로를 내려줌. content만 실제로 쓰므로 나머지 필드는 무시함
// (Gson은 클래스에 없는 JSON 필드를 알아서 무시하므로 totalElements 등은 선언하지 않아도 안전함).
package com.tripping.app.data.response

data class SpringPageResponse<T>(
    val content: List<T>,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)
