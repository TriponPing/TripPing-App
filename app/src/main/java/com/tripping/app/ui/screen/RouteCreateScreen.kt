package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.tripping.app.R
import com.tripping.app.data.request.RouteRecommendRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccentBlue = Color(0xFF0074CE)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)

private val regionOptions = listOf("서울", "강원", "부산", "제주")
private val companionOptions = listOf("혼자", "친구", "가족", "연인")
private val transportOptions = listOf("도보", "대중교통", "자동차")
private val timeOptions = (0..23).map { "%02d:00".format(it) }

private val dateFormatter = SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREAN)
private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private val regionNameToId = mapOf(
    "서울" to "R01",
    "부산" to "R02",
    "강원" to "R03",
    "제주" to "R04"

    // TODO: 강원, 제주 코드는 Supabase region 테이블에 데이터 추가되면 채워넣기
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteCreateScreen(
    onBackClick: () -> Unit = {},
    onNextClick: (RouteRecommendRequest) -> Unit = {}
) {
    var region by remember { mutableStateOf<String?>(null) }
    var regionMenuExpanded by remember { mutableStateOf(false) }

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    val startDateDisplay = startDateMillis?.let { dateFormatter.format(Date(it)) } ?: "2024.06.01 (토)"
    val endDateDisplay = endDateMillis?.let { dateFormatter.format(Date(it)) } ?: "2024.06.02 (일)"

    var selectedCompanions by remember { mutableStateOf(setOf<String>()) }
    var selectedTransports by remember { mutableStateOf(setOf<String>()) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var startTimeMenuExpanded by remember { mutableStateOf(false) }
    var endTimeMenuExpanded by remember { mutableStateOf(false) }
    var startPlace by remember { mutableStateOf("") }

    var showStartPlaceDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ===== 상단 타이틀 =====
        Text(
            text = "루트",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        // ===== 뒤로가기 =====
        BackButtonRow(onClick = onBackClick)
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        // ===== 단계 표시기 =====
        StepIndicator(currentStep = 1)

        Spacer(modifier = Modifier.height(15.dp))

        // ===== 타이틀 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "어디로 떠나시나요?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(id = R.drawable.route_create_tap_sidebar),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier.height(55.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // ===== 지역 드롭다운 선택 =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "지역", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
                        .clickable { regionMenuExpanded = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = region ?: "지역을 선택해주세요",
                        fontSize = 15.sp,
                        color = if (region == null) GrayText else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = GrayText)
                }
                DropdownMenu(
                    expanded = regionMenuExpanded,
                    onDismissRequest = { regionMenuExpanded = false }
                ) {
                    regionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                region = option
                                regionMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ===== 여행 날짜 =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(text = "여행 날짜", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = startDateDisplay,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.clickable { showStartDatePicker = true }
                )
                Text(text = "  ~  ", fontSize = 15.sp, color = GrayText)
                Text(
                    text = endDateDisplay,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true }
                )
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = GrayText)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== 동행 유형 =====
        Text(
            text = "동행 유형",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            companionOptions.forEach { option ->
                val isSelected = option in selectedCompanions
                ChipButton(
                    text = option,
                    selected = isSelected,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedCompanions = if (isSelected) selectedCompanions - option else selectedCompanions + option
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== 이동 수단 (다중 선택) =====
        Text(
            text = "이동 수단",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transportOptions.forEach { option ->
                val isSelected = option in selectedTransports
                ChipButton(
                    text = option,
                    selected = isSelected,
                    modifier = Modifier.weight(1f)
                ) {
                    selectedTransports = if (isSelected) selectedTransports - option else selectedTransports + option
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = LightGrayBorder, thickness = 6.dp)
        Spacer(modifier = Modifier.height(20.dp))

        // ===== 추가 여행 정보 (선택 사항) =====
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(text = "추가 여행 정보", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "더 정확한 루트를 추천해드려요 (선택 입력)", fontSize = 13.sp, color = GrayText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 여행 시간
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "여행 시간", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "출발 시간", fontSize = 12.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        TimePill(text = startTime) { startTimeMenuExpanded = true }
                        DropdownMenu(expanded = startTimeMenuExpanded, onDismissRequest = { startTimeMenuExpanded = false }) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(text = { Text(time) }, onClick = {
                                    startTime = time
                                    startTimeMenuExpanded = false
                                })
                            }
                        }
                    }
                }
                Text(text = "  ~  ", fontSize = 15.sp, color = GrayText, modifier = Modifier.padding(top = 20.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "종료 시간", fontSize = 12.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        TimePill(text = endTime) { endTimeMenuExpanded = true }
                        DropdownMenu(expanded = endTimeMenuExpanded, onDismissRequest = { endTimeMenuExpanded = false }) {
                            timeOptions.forEach { time ->
                                DropdownMenuItem(text = { Text(time) }, onClick = {
                                    endTime = time
                                    endTimeMenuExpanded = false
                                })
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 출발 장소
        InputRow(
            icon = Icons.Filled.LocationOn,
            label = "출발 장소",
            value = startPlace.ifBlank { "장소를 선택해주세요" },
            valueColor = if (startPlace.isBlank()) GrayText else Color.Black,
            onClick = { showStartPlaceDialog = true }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 필수 입력값 안내 메시지
        if (validationError != null) {
            Text(
                text = validationError ?: "",
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // ===== 다음 버튼 =====
        Button(
            onClick = {
                validationError = when {
                    region == null -> "지역을 선택해주세요"
                    startDateMillis == null || endDateMillis == null -> "여행 날짜를 선택해주세요"
                    selectedCompanions.isEmpty() -> "동행 유형을 선택해주세요"
                    selectedTransports.isEmpty() -> "이동 수단을 선택해주세요"
                    else -> null
                }

                if (validationError != null) return@Button

                val startDateStr = startDateMillis?.let { apiDateFormat.format(Date(it)) } ?: apiDateFormat.format(Date())
                val endDateStr = endDateMillis?.let { apiDateFormat.format(Date(it)) } ?: startDateStr

                fun parseTime(text: String): String {
                    return if (text.count { it == ':' } == 1) "$text:00" else text
                }

                val request = RouteRecommendRequest(
                    regionId = region?.let { regionNameToId[it] },
                    startDate = startDateStr,
                    endDate = endDateStr,
                    companionType = selectedCompanions.joinToString(",").ifBlank { null },
                    transport = selectedTransports.joinToString(",").ifBlank { null },
                    startPlace = startPlace.ifBlank { null },
                    startTime = parseTime(startTime),
                    endTime = parseTime(endTime)
                )
                onNextClick(request)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text(text = "다음", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ===== 출발 장소 텍스트 입력 다이얼로그 =====
    if (showStartPlaceDialog) {
        TextInputDialog(
            title = "출발 장소 입력",
            placeholder = "장소명을 입력해주세요",
            initialValue = startPlace,
            onConfirm = { startPlace = it },
            onDismiss = { showStartPlaceDialog = false }
        )
    }

    // ===== 날짜 선택 다이얼로그들 =====
    if (showStartDatePicker) {
        KoreanLocaleProvider {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDateMillis = millis
                        }
                        showStartDatePicker = false
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("취소") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    if (showEndDatePicker) {
        KoreanLocaleProvider {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDateMillis = millis
                        }
                        showEndDatePicker = false
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("취소") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    placeholder: String,
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(text)
                onDismiss()
            }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun StepIndicator(currentStep: Int) {
    val steps = listOf("정보 입력", "추천 확인", "완료")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isDone = stepNumber < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isActive || isDone) AccentBlue else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) AccentBlue else GrayText
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 20.dp),
                    color = if (stepNumber < currentStep) AccentBlue else Color(0xFFE0E0E0),
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
private fun InputRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 15.sp, color = valueColor, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = GrayText)
        }
    }
}

@Composable
private fun ChipButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AccentBlue else LightGrayBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .background(Color.White, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) AccentBlue else Color.Black
        )
    }
}

@Composable
private fun TimePill(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, LightGrayBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = GrayText, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun KoreanLocaleProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val currentConfiguration = LocalConfiguration.current
    val koreanConfiguration = android.content.res.Configuration(currentConfiguration).apply {
        setLocale(Locale.KOREAN)
    }
    val koreanContext = context.createConfigurationContext(koreanConfiguration)

    CompositionLocalProvider(
        LocalContext provides koreanContext,
        LocalConfiguration provides koreanConfiguration
    ) {
        content()
    }
}

@Composable
private fun BackButtonRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowLeft,
            contentDescription = "뒤로가기",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "뒤로가기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}