package com.example.labevmissionconsole.domain

enum class MissionStage { MISSION, DRIVE, FIELD, ANALYSIS, REPORT }

enum class SiteStatus { PENDING, ACTIVE, MEASURED }

data class GeoPoint(val latitude: Double, val longitude: Double)

data class Site(
  val id: String,
  val name: String,
  val address: String,
  val location: GeoPoint,
  val status: SiteStatus = SiteStatus.PENDING,
)

data class Mission(
  val id: String,
  val title: String,
  val subtitle: String,
  val sites: List<Site>,
)

data class SensorReading(
  val missionId: String,
  val siteId: String,
  val timestampMillis: Long,
  val pm25: Double,
  val temperature: Double,
  val humidity: Double,
  val location: GeoPoint,
)

data class VehicleStatus(
  val batteryPercent: Int,
  val isParked: Boolean,
  val gpsReady: Boolean,
  val connectedSensors: Int,
  val totalSensors: Int,
)

data class RouteStatus(
  val distanceKm: Double,
  val etaMinutes: Int,
  val guidanceActive: Boolean,
)

data class SiteSummary(
  val siteName: String,
  val averagePm25: Double,
  val averageTemperature: Double,
  val averageHumidity: Double,
  val sampleCount: Int,
  val highlighted: Boolean = false,
)

data class AiAnalysis(
  val headline: String,
  val observations: List<String>,
  val recommendation: String,
  val disclaimer: String,
)

data class MissionReport(
  val reportId: String,
  val title: String,
  val date: String,
  val summary: String,
  val fieldNote: String,
  val recommendation: String,
)

object DemoMission {
  val mission = Mission(
    id = "M-0826-001",
    title = "울산 산업단지 대기환경 조사",
    subtitle = "PM2.5 · 온도 · 습도 / 3개 지점 비교",
    sites = listOf(
      Site("SITE-A", "Site A", "산업단지 동문", GeoPoint(35.5382, 129.3114)),
      Site("SITE-B", "Site B", "물류 차량 진입로", GeoPoint(35.5421, 129.3198), SiteStatus.ACTIVE),
      Site("SITE-C", "Site C", "완충녹지 관측점", GeoPoint(35.5359, 129.3271)),
    ),
  )
}
