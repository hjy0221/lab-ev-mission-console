package com.example.labevmissionconsole.data;

import android.content.Context;
import ai.pleos.playground.vehicle.Vehicle;
import ai.pleos.playground.vehicle.api.EvBattery;
import ai.pleos.playground.vehicle.api.Gear;

/** Java bridge for public SDK accessors that are hidden by the library's Kotlin metadata. */
final class PleosVehicleBridge {
    private final Vehicle vehicle;

    PleosVehicleBridge(Context context) {
        vehicle = new Vehicle(context);
    }

    void initialize() { vehicle.initialize(); }
    void release() { vehicle.release(); }
    EvBattery getEvBattery() { return vehicle.getEvBattery(); }
    Gear getGear() { return vehicle.getGear(); }
}
