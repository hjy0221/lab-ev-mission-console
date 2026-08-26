package com.example.labevmissionconsole.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labevmissionconsole.data.AiRepository
import com.example.labevmissionconsole.data.NavigationRepository
import com.example.labevmissionconsole.data.SensorRepository
import com.example.labevmissionconsole.data.VehicleRepository
import com.example.labevmissionconsole.data.RepositoryFactory
import com.example.labevmissionconsole.domain.AiAnalysis
import com.example.labevmissionconsole.domain.DemoMission
import com.example.labevmissionconsole.domain.Mission
import com.example.labevmissionconsole.domain.MissionReport
import com.example.labevmissionconsole.domain.MissionStage
import com.example.labevmissionconsole.domain.RouteStatus
import com.example.labevmissionconsole.domain.SensorReading
import com.example.labevmissionconsole.domain.SiteSummary
import com.example.labevmissionconsole.domain.VehicleStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MissionUiState(
  val stage: MissionStage = MissionStage.MISSION,
  val mission: Mission = DemoMission.mission,
  val vehicle: VehicleStatus = VehicleStatus(82, true, true, 3, 3),
  val route: RouteStatus = RouteStatus(8.4, 18, false),
  val readings: List<SensorReading> = emptyList(),
  val isMeasuring: Boolean = false,
  val elapsedSeconds: Int = 0,
  val fieldNote: String = "대형 화물차 통행이 많고 남서쪽에서 강한 바람이 관찰됨.",
  val summaries: List<SiteSummary> = emptyList(),
  val analysis: AiAnalysis? = null,
  val report: MissionReport? = null,
  val isBusy: Boolean = false,
)

class MissionViewModel(application: Application) : AndroidViewModel(application) {
  private val repositories = RepositoryFactory.create(application)
  private val vehicleRepository: VehicleRepository = repositories.vehicle
  private val navigationRepository: NavigationRepository = repositories.navigation
  private val sensorRepository: SensorRepository = repositories.sensor
  private val aiRepository: AiRepository = repositories.ai
  private val mutableUiState = MutableStateFlow(MissionUiState())
  val uiState: StateFlow<MissionUiState> = mutableUiState.asStateFlow()
  private var measurementJob: Job? = null

  init {
    viewModelScope.launch { vehicleRepository.status.collect { value -> mutableUiState.update { it.copy(vehicle = value) } } }
    viewModelScope.launch { navigationRepository.route.collect { value -> mutableUiState.update { it.copy(route = value) } } }
  }

  fun startMission() {
    val site = mutableUiState.value.mission.sites[1]
    vehicleRepository.setParked(false)
    mutableUiState.update { it.copy(stage = MissionStage.DRIVE, isBusy = true) }
    viewModelScope.launch {
      navigationRepository.navigateTo(site)
      mutableUiState.update { it.copy(isBusy = false) }
    }
  }

  fun simulateArrival() {
    navigationRepository.arrive()
    vehicleRepository.setParked(true)
    mutableUiState.update { it.copy(stage = MissionStage.FIELD) }
  }

  fun toggleMeasurement() {
    if (mutableUiState.value.isMeasuring) stopMeasurement() else startMeasurement()
  }

  private fun startMeasurement() {
    val state = mutableUiState.value
    val site = state.mission.sites[1]
    mutableUiState.update { it.copy(isMeasuring = true, readings = emptyList(), elapsedSeconds = 0) }
    measurementJob = viewModelScope.launch {
      sensorRepository.readings(state.mission.id, site).collect { reading ->
        mutableUiState.update {
          it.copy(readings = (it.readings + reading).takeLast(24), elapsedSeconds = it.elapsedSeconds + 1)
        }
      }
    }
  }

  private fun stopMeasurement() {
    measurementJob?.cancel()
    val readings = mutableUiState.value.readings
    val averagePm = readings.map { it.pm25 }.average().takeUnless { it.isNaN() } ?: 27.8
    val averageTemp = readings.map { it.temperature }.average().takeUnless { it.isNaN() } ?: 27.4
    val averageHumidity = readings.map { it.humidity }.average().takeUnless { it.isNaN() } ?: 61.0
    val summaries = listOf(
      SiteSummary("Site A", 17.2, 27.1, 60.0, 18),
      SiteSummary("Site B", averagePm, averageTemp, averageHumidity, readings.size.coerceAtLeast(12), highlighted = true),
      SiteSummary("Site C", 16.4, 26.9, 62.0, 18),
    )
    mutableUiState.update { it.copy(stage = MissionStage.ANALYSIS, isMeasuring = false, summaries = summaries) }
  }

  fun runAnalysis() {
    val state = mutableUiState.value
    mutableUiState.update { it.copy(isBusy = true) }
    viewModelScope.launch {
      val analysis = aiRepository.analyze(state.mission, state.summaries, state.fieldNote)
      mutableUiState.update { it.copy(analysis = analysis, isBusy = false) }
    }
  }

  fun generateReport() {
    val state = mutableUiState.value
    val analysis = state.analysis ?: return
    mutableUiState.update { it.copy(isBusy = true) }
    viewModelScope.launch {
      val report = aiRepository.generateReport(state.mission, state.summaries, analysis, state.fieldNote)
      mutableUiState.update { it.copy(stage = MissionStage.REPORT, report = report, isBusy = false) }
    }
  }

  fun reset() {
    measurementJob?.cancel()
    mutableUiState.value = MissionUiState()
    vehicleRepository.setParked(true)
  }

  override fun onCleared() {
    measurementJob?.cancel()
    vehicleRepository.close()
    navigationRepository.close()
    aiRepository.close()
    super.onCleared()
  }
}
