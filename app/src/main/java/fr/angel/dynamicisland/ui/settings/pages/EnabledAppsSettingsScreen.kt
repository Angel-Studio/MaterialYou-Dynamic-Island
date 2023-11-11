package fr.angel.dynamicisland.ui.settings.pages

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.rememberDrawablePainter
import fr.angel.dynamicisland.MainActivity
import fr.angel.dynamicisland.island.IslandSettings
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun EnabledAppsSettingsScreen() {

	val context = LocalContext.current

	// Get the list of installed apps in the background
	val apps = remember { mutableStateListOf<PackageInfo>() }
	val executor: ExecutorService = Executors.newSingleThreadExecutor()
	LaunchedEffect(Unit) {
		executor.execute {
			//Background work here
			apps.addAll(
				context.packageManager.getInstalledPackages(0)
					.filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }.toMutableList()
					.sortedBy { it.applicationInfo.loadLabel(context.packageManager).toString().lowercase() }
			)
		}
	}

	LaunchedEffect(Unit) {
		MainActivity.instance.actions.clear()
		MainActivity.instance.actions.add {
			IconButton(
				onClick = {
					if (apps.all { IslandSettings.instance.enabledApps.contains(it.packageName) }) {
						// Unselect all
						IslandSettings.instance.enabledApps.clear()
						IslandSettings.instance.applySettings(context)
					} else {
						// Select all
						IslandSettings.instance.enabledApps.clear()
						IslandSettings.instance.enabledApps.addAll(apps.map { it.packageName })
						IslandSettings.instance.applySettings(context)
					}
				},
			) {
				Icon(if (apps.all { IslandSettings.instance.enabledApps.contains(it.packageName) }) Icons.Filled.Deselect else Icons.Filled.SelectAll, contentDescription = null)
			}
		}
	}

	LazyColumn(
		verticalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(8.dp),
	) {
		items(apps) { app ->
			var selected by remember { mutableStateOf(false) }

			EnabledAppCard(
				app = app,
				selected = IslandSettings.instance.enabledApps.contains(app.packageName),
				onSwitch = { switch ->
					if (switch) {
						IslandSettings.instance.enabledApps.add(app.packageName)
					} else {
						IslandSettings.instance.enabledApps.remove(app.packageName)
					}
					selected = switch
					IslandSettings.instance.applySettings(context)
				}
			)
		}
	}
}

@Composable
fun EnabledAppCard(
	app: PackageInfo,
	selected: Boolean,
	onSwitch: (Boolean) -> Unit = {},
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable {
				onSwitch(!selected)
			}
			.padding(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Image(
			painter = rememberDrawablePainter(app.applicationInfo.loadIcon(LocalContext.current.packageManager)),
			contentDescription = null,
			modifier = Modifier
				.size(48.dp)
				.clip(CircleShape)
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = app.applicationInfo.loadLabel(LocalContext.current.packageManager).toString(),
			modifier = Modifier.weight(1f),
			style = MaterialTheme.typography.titleSmall,
		)
		Switch(checked = selected, onCheckedChange = {
			onSwitch(it)
		})
		Spacer(modifier = Modifier.width(8.dp))
	}
}
