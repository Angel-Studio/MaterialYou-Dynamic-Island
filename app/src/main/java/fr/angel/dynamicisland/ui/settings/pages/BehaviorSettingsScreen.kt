package fr.angel.dynamicisland.ui.settings.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.island.IslandSettings

@Composable
fun BehaviorSettingsScreen() {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		contentPadding = PaddingValues(16.dp)
	) {
		item {
			SwitchSettingsItem(
				title = "Show on lock screen",
				description = "Show the island on the lock screen and on the always-on display",
				checked = IslandSettings.instance.showOnLockScreen
			) { IslandSettings.instance.showOnLockScreen = it }
		}
		item {
			SwitchSettingsItem(
				title = "Show in landscape",
				description = "Show island in landscape mode",
				checked = IslandSettings.instance.showInLandscape
			) { IslandSettings.instance.showInLandscape = it }
		}
		item {
			Spacer(modifier = Modifier.height(16.dp))
			SettingsSlider(
				onValueChange = {
					IslandSettings.instance.autoHideOpenedAfter = it * 1000
				},
				onReset = {
					IslandSettings.instance.autoHideOpenedAfter = 5000f
				},
				roundedTo = 1,
				preciseValue = .5f,
				title = "Auto hide opened island after",
				extension = "s",
				value = IslandSettings.instance.autoHideOpenedAfter / 1000,
				range = 0.5f..60f,
			)
		}
	}
}

@Composable
fun SwitchSettingsItem(
	title: String,
	description: String? = null,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable(onClick = { onCheckedChange(!checked) })
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
			if (description != null) {
				Text(
					text = description,
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
		Switch(checked = checked, onCheckedChange = onCheckedChange)
	}
}
