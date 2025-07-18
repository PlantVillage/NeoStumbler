package xyz.malkki.neostumbler.data.emitter

import android.Manifest
import android.content.Context
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import xyz.malkki.neostumbler.core.CellTower
import xyz.malkki.neostumbler.core.CellTower.Companion.fillMissingData
import xyz.malkki.neostumbler.data.emitter.mapper.toCellTower

class TelephonyManagerPassiveCellInfoSource(private val telephonyManager: TelephonyManager) :
    PassiveCellTowerSource {
    constructor(context: Context) : this(context.getSystemService<TelephonyManager>()!!)

    @RequiresPermission(
        allOf =
            [
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
            ]
    )
    override fun getCellTowers(): List<CellTower> {
        val serviceState = telephonyManager.serviceState

        return telephonyManager.allCellInfo
            .mapNotNull { cellInfo -> cellInfo.toCellTower() }
            .fillMissingData(serviceState?.operatorNumeric)
    }
}
