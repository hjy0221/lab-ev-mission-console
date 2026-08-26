package com.example.labevmissionconsole.data

import android.content.Context

object RepositoryFactory {
  fun create(context: Context) = RepositoryBundle(
    vehicle = PleosVehicleRepository(context.applicationContext),
    navigation = PleosNavigationRepository(context.applicationContext),
    sensor = MockSensorRepository(),
    ai = PleosAiRepository(context.applicationContext),
  )
}
