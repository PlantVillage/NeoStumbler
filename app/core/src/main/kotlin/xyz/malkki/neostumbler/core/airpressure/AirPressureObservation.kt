package xyz.malkki.neostumbler.core.airpressure

data class AirPressureObservation(
    /** Air pressure in hPa */
    val airPressure: Float,
    /** Time when the air pressure was observed in milliseconds since boot */
    val timestamp: Long,
)
