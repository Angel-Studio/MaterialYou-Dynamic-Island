package fr.angel.dynamicisland.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
	onSettingClicked: (SettingItem) -> Unit,
) {
	LazyColumn(
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		items(settings) { setting ->
			val settingsItem = setting as SettingItem
			SettingsItem(
				title = settingsItem.title,
				subtitle = settingsItem.subtitle,
				icon = settingsItem.icon,
				onClick = { onSettingClicked(settingsItem) },
			)
		}
	}
}

@Composable
fun SettingsItem(
	title: String,
	subtitle: String,
	icon: ImageVector,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable { onClick() }
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(modifier = Modifier
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.primary)
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier.padding(8.dp)
			)
		}
		Spacer(modifier = Modifier.width(16.dp))
		Column {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
			Text(
				text = subtitle,
				style = MaterialTheme.typography.labelMedium
			)
		}
	}
}

@Composable
fun SettingsDivider(
	modifier: Modifier = Modifier,
) {
	Divider(
		modifier = modifier
			.fillMaxWidth(),
		color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
	)
}