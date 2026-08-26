package com.example.labevmissionconsole.data

import android.content.Context
import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.EvBatteryLevelListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import com.example.labevmissionconsole.domain.AiAnalysis
import com.example.labevmissionconsole.domain.Mission
import com.example.labevmissionconsole.domain.MissionReport
import com.example.labevmissionconsole.domain.RouteStatus
import com.example.labevmissionconsole.domain.Site
import com.example.labevmissionconsole.domain.SiteSummary
import com.example.labevmissionconsole.domain.VehicleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

/** PLEOS Vehicle 2.0.3 adapter. The demo-only setter is intentionally ignored. */
class PleosVehicleRepository(context: Context) : VehicleRepository {
  private val vehicle = PleosVehicleBridge(context)
  private val mutableStatus = MutableStateFlow(VehicleStatus(0, false, true, 3, 3))
  override val status: StateFlow<VehicleStatus> = mutableStatus

  private val batteryListener = object : EvBatteryLevelListener {
    override fun onEvBatteryLevelUpdated(value: Float?) = updateBattery(value)
    override fun onFailed(e: Exception) = Unit
  }
  private val gearListener = object : GearSelectionListener {
    override fun onSelected(value: VehicleGear?) {
      mutableStatus.value = mutableStatus.value.copy(isParked = value == VehicleGear.GEAR_PARK)
    }
    override fun onFailed(e: Exception) = Unit
  }

  init {
    vehicle.initialize()
    vehicle.evBattery.getEvBatteryLevel(::updateBattery) { }
    vehicle.gear.getCurrentGearSelectionState(
      { gear -> mutableStatus.value = mutableStatus.value.copy(isParked = gear == VehicleGear.GEAR_PARK) },
      { },
    )
    vehicle.evBattery.registerEvBatteryLevel(batteryListener)
    vehicle.gear.registerGearSelection(gearListener)
  }

  private fun updateBattery(value: Float?) {
    value ?: return
    mutableStatus.value = mutableStatus.value.copy(batteryPercent = value.roundToInt().coerceIn(0, 100))
  }

  override fun setParked(parked: Boolean) = Unit

  override fun close() {
    vehicle.evBattery.unregisterEvBatteryLevel(batteryListener)
    vehicle.gear.unregisterGearSelection(gearListener)
    vehicle.release()
  }
}

/** PLEOS NaviHelper 2.0.3 adapter using official route and guidance callbacks. */
class PleosNavigationRepository(context: Context) : NavigationRepository {
  private val navi = NaviHelper(context)
  private val mutableRoute = MutableStateFlow(RouteStatus(0.0, 0, false))
  override val route: StateFlow<RouteStatus> = mutableRoute

  private val listener = object : NaviHelperEventListener {
    override fun onRouteStarted(info: RouteStartInfo) = update(info.distance, info.duration, true)
    override fun onDrivingInfo(info: DrivingInfo) = update(info.destination.distance, info.destination.duration, true)
    override fun onDestinationArrived(info: DestinationArrivedInfo) = update(0, 0, false)
    override fun onRouteEnded() { mutableRoute.value = mutableRoute.value.copy(guidanceActive = false) }
    override fun onRouteCancelled() { mutableRoute.value = mutableRoute.value.copy(guidanceActive = false) }
  }

  init {
    navi.initialize()
    navi.addListener(listener)
  }

  override suspend fun navigateTo(site: Site) {
    navi.requestRoute(
      RouteInfo(
        longitude = site.location.longitude,
        latitude = site.location.latitude,
        poiName = site.name,
        poiId = site.id,
        address = site.address,
        poiSubId = "",
        routeOption = RouteOption.RECOMMENDED,
      )
    )
  }

  private fun update(distanceMeters: Int, durationSeconds: Int, active: Boolean) {
    mutableRoute.value = RouteStatus(distanceMeters / 1000.0, (durationSeconds / 60.0).roundToInt(), active)
  }

  override fun arrive() = Unit

  override fun close() {
    navi.removeListener(listener)
    navi.release()
  }
}

/**
 * The LAB EV console project does not yet have Gleo AI approval, so AI remains a
 * deterministic fallback while preserving the same repository contract.
 */
class PleosAiRepository(context: Context) : AiRepository {
  private val fallback = MockAiRepository()

  override suspend fun analyze(mission: Mission, summaries: List<SiteSummary>, fieldNote: String): AiAnalysis =
    fallback.analyze(mission, summaries, fieldNote)

  override suspend fun generateReport(
    mission: Mission,
    summaries: List<SiteSummary>,
    analysis: AiAnalysis,
    fieldNote: String,
  ): MissionReport = fallback.generateReport(mission, summaries, analysis, fieldNote)
}
