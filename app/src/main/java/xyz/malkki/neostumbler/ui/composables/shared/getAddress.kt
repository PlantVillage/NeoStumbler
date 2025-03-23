package xyz.malkki.neostumbler.ui.composables.shared

import android.annotation.SuppressLint
import android.location.Address
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import java.io.IOException
import timber.log.Timber
import xyz.malkki.neostumbler.extensions.roundToString
import xyz.malkki.neostumbler.utils.geocoder.Geocoder

private const val FRACTION_DIGITS = 4

@Composable
@SuppressLint("ProduceStateDoesNotAssignValue") // https://issuetracker.google.com/issues/349411310
fun getAddress(
    latitude: Double,
    longitude: Double,
    geocoder: Geocoder,
    fallbackToCoordinates: Boolean = true,
): State<String?> =
    produceState(
        initialValue = null,
        latitude,
        longitude,
        geocoder,
        producer = {
            val addresses =
                try {
                    geocoder.getAddresses(latitude, longitude)
                } catch (ioException: IOException) {
                    Timber.w(
                        ioException,
                        "Failed to geocode address for location ${latitude}, $longitude",
                    )
                    emptyList()
                }

            value =
                if (addresses.isEmpty() && fallbackToCoordinates) {
                    "${latitude.roundToString(FRACTION_DIGITS)}, ${longitude.roundToString(FRACTION_DIGITS)}"
                } else {
                    addresses.firstOrNull()?.format()
                }
        },
    )

private fun Address.format(): String {
    val firstAddressLine = getAddressLine(0)
    if (firstAddressLine != null) {
        return firstAddressLine
    }

    val streetAddress =
        if (subThoroughfare != null && thoroughfare != null) {
            "$thoroughfare $subThoroughfare"
        } else if (thoroughfare != null) {
            thoroughfare
        } else {
            null
        }

    val elements = listOfNotNull(streetAddress, locality, countryCode)
    return elements.joinToString(", ")
}
