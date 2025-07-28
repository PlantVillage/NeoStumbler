package xyz.malkki.neostumbler.scanner.speed

import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.malkki.neostumbler.core.observation.PositionObservation
import xyz.malkki.neostumbler.extensions.pairwise

// Maximum age difference between two consecutive locations
private val LOCATION_MAX_AGE_DIFF = 5.seconds

// Smoothening factor, i.e. how much to weight previous speed vs. current speed
private const val A = 0.15
private const val B = 1 - A

/** Calculates smoothened speed by using data from two consecutive locations */
class SmoothenedGpsSpeedSource(private val positionFlow: Flow<PositionObservation>) : SpeedSource {
    override fun getSpeedFlow(): Flow<Double> {
        return positionFlow.pairwise().map { (prevLocation, currentLocation) ->
            if (currentLocation.position.speed != null) {
                // Smoothen the speed value by using data from two consecutive locations
                if (
                    prevLocation.position.speed != null &&
                        abs(prevLocation.timestamp - currentLocation.timestamp).milliseconds <=
                            LOCATION_MAX_AGE_DIFF
                ) {
                    prevLocation.position.speed!! * A + currentLocation.position.speed!! * B
                } else {
                    currentLocation.position.speed!!.toDouble()
                }
            } else {
                0.0
            }
        }
    }
}
