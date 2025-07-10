package xyz.malkki.neostumbler.ui.composables.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.malkki.neostumbler.PREFERENCES
import xyz.malkki.neostumbler.R
import xyz.malkki.neostumbler.constants.PreferenceKeys
import xyz.malkki.neostumbler.extensions.get
import xyz.malkki.neostumbler.ui.composables.settings.SettingsToggle
import xyz.malkki.neostumbler.ui.composables.settings.UrlField
import xyz.malkki.neostumbler.ui.composables.shared.Dialog
import xyz.malkki.neostumbler.ui.map.MapTileSource

private typealias TileSourceAndStyleUrl = Pair<MapTileSource, String>

private fun DataStore<Preferences>.selectedMapTileSourceAndStyleUrl(): Flow<TileSourceAndStyleUrl> =
    data.map { prefs ->
        val tileSource =
            prefs.get<MapTileSource>(PreferenceKeys.MAP_TILE_SOURCE) ?: MapTileSource.DEFAULT
        val styleUrl = prefs[stringPreferencesKey(PreferenceKeys.MAP_TILE_SOURCE_CUSTOM_URL)] ?: ""

        tileSource to styleUrl
    }

@Composable
fun MapSettingsButton(
    modifier: Modifier,
    settingsStore: DataStore<Preferences> = koinInject<DataStore<Preferences>>(PREFERENCES),
) {
    val coroutineScope = rememberCoroutineScope()

    var dialogOpen by rememberSaveable { mutableStateOf(false) }

    val selectedMapTileSourceAndStyleUrl by
        settingsStore
            .selectedMapTileSourceAndStyleUrl()
            .collectAsStateWithLifecycle(initialValue = null)

    val selectedMapTileSource = selectedMapTileSourceAndStyleUrl?.first
    val styleUrl = selectedMapTileSourceAndStyleUrl?.second

    if (dialogOpen) {
        MapSettingsDialog(
            currentSettings = selectedMapTileSource!! to styleUrl!!,
            onCloseDialog = { newSettings ->
                if (newSettings != null) {
                    coroutineScope.launch {
                        settingsStore.edit { prefs ->
                            prefs[stringPreferencesKey(PreferenceKeys.MAP_TILE_SOURCE)] =
                                newSettings.first.name

                            prefs[stringPreferencesKey(PreferenceKeys.MAP_TILE_SOURCE_CUSTOM_URL)] =
                                newSettings.second
                        }
                    }
                }

                dialogOpen = false
            },
        )
    }

    FilledTonalIconButton(
        modifier = modifier,
        onClick = { dialogOpen = true },
        enabled = selectedMapTileSourceAndStyleUrl != null,
        colors = IconButtonDefaults.filledTonalIconButtonColors(),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.layers_18),
            contentDescription = stringResource(id = R.string.map_tile_source),
        )
    }
}

@Composable
private fun MapSettingsDialog(
    currentSettings: TileSourceAndStyleUrl,
    onCloseDialog: (TileSourceAndStyleUrl?) -> Unit,
) {
    val selectedTileSource = rememberSaveable { mutableStateOf(currentSettings.first) }
    val selectedStyleUrl = rememberSaveable { mutableStateOf<String?>(currentSettings.second) }

    val onClose = {
        if (selectedTileSource.value == MapTileSource.CUSTOM) {
            onCloseDialog(selectedTileSource.value to selectedStyleUrl.value!!)
        } else {
            onCloseDialog(null)
        }
    }

    Dialog(title = stringResource(id = R.string.map_tile_source), onDismissRequest = onClose) {
        Column {
            Column(
                modifier = Modifier.selectableGroup().padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MapTileSource.entries.forEach { mapTileSource ->
                    Row(
                        Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .defaultMinSize(minHeight = 36.dp)
                            .selectable(
                                selected = mapTileSource == selectedTileSource.value,
                                onClick = {
                                    if (mapTileSource != MapTileSource.CUSTOM) {
                                        onCloseDialog(mapTileSource to selectedStyleUrl.value!!)
                                    } else {
                                        selectedTileSource.value = mapTileSource
                                    }
                                },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            modifier =
                                Modifier.align(Alignment.CenterVertically).padding(top = 4.dp),
                            selected = mapTileSource == selectedTileSource.value,
                            onClick = null,
                        )

                        Text(
                            modifier =
                                Modifier.align(Alignment.CenterVertically).padding(start = 16.dp),
                            text =
                                if (mapTileSource == MapTileSource.CUSTOM) {
                                    stringResource(R.string.map_tile_source_custom_title)
                                } else {
                                    mapTileSource.title!!
                                },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (selectedTileSource.value == MapTileSource.CUSTOM) {
                UrlField(
                    modifier = Modifier.padding(bottom = 12.dp),
                    label = stringResource(R.string.map_tile_source_custom_style_url),
                    onDone = onClose,
                    state = selectedStyleUrl,
                )
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(all = 8.dp)) {
                SettingsToggle(
                    title = stringResource(R.string.map_show_coverage_layer),
                    preferenceKey = PreferenceKeys.COVERAGE_LAYER_ENABLED,
                    default = true,
                )
            }
        }
    }
}
