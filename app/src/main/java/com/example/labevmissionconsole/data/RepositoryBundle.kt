package com.example.labevmissionconsole.data

data class RepositoryBundle(
  val vehicle: VehicleRepository,
  val navigation: NavigationRepository,
  val sensor: SensorRepository,
  val ai: AiRepository,
)
