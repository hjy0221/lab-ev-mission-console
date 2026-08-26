package com.example.labevmissionconsole.data

import com.example.labevmissionconsole.domain.AiAnalysis
import com.example.labevmissionconsole.domain.GeoPoint
import com.example.labevmissionconsole.domain.Mission
import com.example.labevmissionconsole.domain.MissionReport
import com.example.labevmissionconsole.domain.RouteStatus
import com.example.labevmissionconsole.domain.SensorReading
import com.example.labevmissionconsole.domain.Site
import com.example.labevmissionconsole.domain.SiteSummary
import com.example.labevmissionconsole.domain.VehicleStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin

/** Boundary for Pleos Vehicle SDK. Replace MockVehicleRepository with a Pleos adapter. */
interface VehicleRepository {
  val status: StateFlow<VehicleStatus>
  fun setParked(parked: Boolean)
  fun close() = Unit
}

/** Boundary for NaviHelper.initialize/requestRoute/listener/release lifecycle. */
interface NavigationRepository {
  val route: StateFlow<RouteStatus>
  suspend fun navigateTo(site: Site)
  fun arrive()
  fun close() = Unit
}

/** LAB equipment boundary. A USB/BLE sensor adapter can replace the mock stream. */
interface SensorRepository {
  fun readings(missionId: String, site: Site): Flow<SensorReading>
}

/** Boundary for Gleo LLM initialize/register/generateContent/release lifecycle. */
interface AiRepository {
  suspend fun analyze(mission: Mission, summaries: List<SiteSummary>, fieldNote: String): AiAnalysis
  suspend fun generateReport(mission: Mission, summaries: List<SiteSummary>, analysis: AiAnalysis, fieldNote: String): MissionReport
  fun close() = Unit
}

class MockVehicleRepository : VehicleRepository {
  private val mutableStatus = MutableStateFlow(VehicleStatus(82, true, true, 3, 3))
  override val status: StateFlow<VehicleStatus> = mutableStatus
  override fun setParked(parked: Boolean) { mutableStatus.value = mutableStatus.value.copy(isParked = parked, batteryPercent = if (parked) 79 else 81) }
}

class MockNavigationRepository : NavigationRepository {
  private val mutableRoute = MutableStateFlow(RouteStatus(8.4, 18, false))
  override val route: StateFlow<RouteStatus> = mutableRoute
  override suspend fun navigateTo(site: Site) {
    delay(350)
    mutableRoute.value = RouteStatus(8.4, 18, true)
  }
  override fun arrive() { mutableRoute.value = RouteStatus(0.0, 0, false) }
}

class MockSensorRepository : SensorRepository {
  override fun readings(missionId: String, site: Site): Flow<SensorReading> = flow {
    var index = 0
    while (true) {
      val wave = sin(index / 2.2)
      emit(
        SensorReading(
          missionId = missionId,
          siteId = site.id,
          timestampMillis = System.currentTimeMillis(),
          pm25 = 26.9 + wave * 2.1 + (index % 4) * 0.18,
          temperature = 27.3 + sin(index / 4.0) * 0.4,
          humidity = 61.0 + sin(index / 3.0) * 1.8,
          location = GeoPoint(site.location.latitude + index * 0.000001, site.location.longitude),
        )
      )
      index += 1
      delay(800)
    }
  }
}

class MockAiRepository : AiRepository {
  override suspend fun analyze(mission: Mission, summaries: List<SiteSummary>, fieldNote: String): AiAnalysis {
    delay(900)
    val high = summaries.maxBy { it.averagePm25 }
    val others = summaries.filterNot { it === high }.map { it.averagePm25 }.average()
    val difference = ((high.averagePm25 / others - 1) * 100).toInt()
    return AiAnalysis(
      headline = "${high.siteName}에서 상대적으로 높은 PM2.5가 관찰되었습니다.",
      observations = listOf(
        "다른 지점 평균보다 약 ${difference}% 높은 값입니다.",
        "현장 메모에 화물차 통행과 남서풍이 기록되었습니다.",
        "온도와 습도는 세 지점에서 유사한 범위입니다.",
      ),
      recommendation = "Site B를 동일 시간대에 반복 측정하고 풍향 데이터와 함께 비교하세요.",
      disclaimer = "현재 데이터는 상관 관찰이며 원인을 특정하지 않습니다.",
    )
  }

  override suspend fun generateReport(mission: Mission, summaries: List<SiteSummary>, analysis: AiAnalysis, fieldNote: String): MissionReport {
    delay(700)
    val values = summaries.joinToString(" · ") { "${it.siteName} ${"%.1f".format(it.averagePm25)}" }
    return MissionReport(
      reportId = "RPT-${mission.id}",
      title = "LAB EV FIELD REPORT",
      date = "2026.08.26",
      summary = "PM2.5 지점 평균: $values μg/m³. ${analysis.headline}",
      fieldNote = fieldNote,
      recommendation = analysis.recommendation,
    )
  }
}
