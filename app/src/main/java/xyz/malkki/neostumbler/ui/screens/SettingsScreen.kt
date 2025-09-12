package xyz.malkki.neostumbler.ui.screens

import android.os.Build
import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.koin.compose.koinInject
import xyz.malkki.neostumbler.R
import xyz.malkki.neostumbler.constants.PreferenceKeys
import xyz.malkki.neostumbler.data.settings.Settings
import xyz.malkki.neostumbler.data.settings.getStringFlow
import xyz.malkki.neostumbler.scanner.ScannerService
import xyz.malkki.neostumbler.ui.composables.AboutNeoStumbler
import xyz.malkki.neostumbler.ui.composables.ReportReuploadButton
import xyz.malkki.neostumbler.ui.composables.settings.AutoScanToggle
import xyz.malkki.neostumbler.ui.composables.settings.AutoUploadToggle
import xyz.malkki.neostumbler.ui.composables.settings.CoverageLayerSettings
import xyz.malkki.neostumbler.ui.composables.settings.CrashLogSettingsItem
import xyz.malkki.neostumbler.ui.composables.settings.DbPruneSettings
import xyz.malkki.neostumbler.ui.composables.settings.FusedLocationToggle
import xyz.malkki.neostumbler.ui.composables.settings.GeocoderSettings
import xyz.malkki.neostumbler.ui.composables.settings.IgnoreScanThrottlingToggle
import xyz.malkki.neostumbler.ui.composables.settings.LanguageSwitcher
import xyz.malkki.neostumbler.ui.composables.settings.ManageStorageSettingsItem
import xyz.malkki.neostumbler.ui.composables.settings.MovementDetectorSettings
import xyz.malkki.neostumbler.ui.composables.settings.ParamField
import xyz.malkki.neostumbler.ui.composables.settings.PassiveScanToggle
import xyz.malkki.neostumbler.ui.composables.settings.ScannerNotificationStyleSettings
import xyz.malkki.neostumbler.ui.composables.settings.SettingsGroup
import xyz.malkki.neostumbler.ui.composables.settings.SettingsToggle
import xyz.malkki.neostumbler.ui.composables.settings.SliderSetting
import xyz.malkki.neostumbler.ui.composables.settings.TextSetting
import xyz.malkki.neostumbler.ui.composables.settings.geosubmit.GeosubmitEndpointSettings
import xyz.malkki.neostumbler.ui.composables.settings.privacy.WifiFilterSettings
import xyz.malkki.neostumbler.ui.composables.troubleshooting.TroubleshootingSettingsItem
import xyz.malkki.neostumbler.ui.modifiers.handleDisplayCutouts

@Composable
private fun PlantVillageSettings() {
    SettingsGroup(title="PlantVillage+") {
        TextSetting(
            label = "name",
            key = PreferenceKeys.USERNAME,
            default = ""
        )

        TextSetting(
            label = "email",
            key = PreferenceKeys.USER_EMAIL,
            filter = {
                it.replace(" ", "")
            },
            isError = {
                !Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            default = ""
        )
    }
}

@Composable
private fun ReportSettings() {
    SettingsGroup(title = stringResource(id = R.string.settings_group_reports)) {
        GeosubmitEndpointSettings()
        CoverageLayerSettings()
        AutoUploadToggle()
        DbPruneSettings()
    }
}

@Composable
private fun ScanningSettings() {
    val context = LocalContext.current

    SettingsGroup(title = stringResource(id = R.string.settings_group_scanning)) {
        MovementDetectorSettings()
        FusedLocationToggle()
        IgnoreScanThrottlingToggle()

        SliderSetting(
            title = stringResource(R.string.pause_scanning_on_low_battery_title),
            preferenceKey = PreferenceKeys.PAUSE_ON_BATTERY_LEVEL_THRESHOLD,
            range = 0..50,
            step = 5,
            valueFormatter = {
                if (it == 0) {
                    context.getString(R.string.disabled)
                } else {
                    context.getString(R.string.pause_scanning_on_low_battery_description, it)
                }
            },
            default = 20,
        )
        SliderSetting(
            title = stringResource(R.string.wifi_scan_frequency),
            preferenceKey = PreferenceKeys.WIFI_SCAN_DISTANCE,
            // Some translations assume this will always be a multiple of ten
            range = 10..250,
            step = 10,
            valueFormatter = {
                ContextCompat.getString(context, R.string.every_x_meters).format(it)
            },
            default = ScannerService.DEFAULT_WIFI_SCAN_DISTANCE,
        )

        SliderSetting(
            title = stringResource(R.string.cell_tower_scan_frequency),
            preferenceKey = PreferenceKeys.CELL_SCAN_DISTANCE,
            // Some translations assume this will always be a multiple of ten
            range = 20..500,
            step = 20,
            valueFormatter = {
                ContextCompat.getString(context, R.string.every_x_meters).format(it)
            },
            default = ScannerService.DEFAULT_CELL_SCAN_DISTANCE,
        )

        SettingsToggle(
            title = stringResource(id = R.string.moving_device_filter_title),
            description = stringResource(id = R.string.moving_device_filter_description),
            preferenceKey = PreferenceKeys.FILTER_MOVING_DEVICES,
            default = true,
        )

        PassiveScanToggle()
        AutoScanToggle()
    }
}

@Composable
private fun PrivacySettings() {
    SettingsGroup(title = stringResource(id = R.string.settings_group_privacy)) {
        WifiFilterSettings()

        SettingsToggle(
            title = stringResource(id = R.string.reduced_metadata_title),
            description = stringResource(id = R.string.reduced_metadata_description),
            preferenceKey = PreferenceKeys.REDUCED_METADATA,
            default = false,
        )
    }
}

@Composable
private fun OtherSettings() {
    SettingsGroup(title = stringResource(id = R.string.settings_group_other)) {
        ScannerNotificationStyleSettings()
        LanguageSwitcher()
        GeocoderSettings()

        // Dynamic color is available on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsToggle(
                title = stringResource(id = R.string.use_dynamic_color_ui),
                preferenceKey = PreferenceKeys.DYNAMIC_COLOR_THEME,
                default = false,
            )
        }

        TroubleshootingSettingsItem()
        ManageStorageSettingsItem()

        CrashLogSettingsItem()
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .handleDisplayCutouts()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PlantVillageSettings()

        ReportSettings()

        ScanningSettings()

        PrivacySettings()

        OtherSettings()

        Spacer(modifier = Modifier.height(8.dp))

        ReportReuploadButton()

        Spacer(modifier = Modifier.height(8.dp))

        AboutNeoStumbler()

        Spacer(modifier = Modifier.height(16.dp))
    }
}
