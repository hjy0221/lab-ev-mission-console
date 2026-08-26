package com.example.labevmissionconsole.data

import android.content.Context

object RepositoryFactory {
  fun create(context: Context) = RepositoryBundle(
    vehicle = MockVehicleRepository(),
    navigation = MockNavigationRepository(),
    sensor = MockSensorRepository(),
    ai = MockAiRepository(),
  )
}
