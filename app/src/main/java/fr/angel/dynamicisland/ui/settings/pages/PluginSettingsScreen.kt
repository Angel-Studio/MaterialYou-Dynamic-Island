package fr.angel.dynamicisland.plugins

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.angel.dynamicisland.ui.settings.pages.SwitchSettingsItem

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PluginSettingsScreen(
	plugin: BasePlugin
) {
	val context = LocalContext.current

	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		contentPadding = PaddingValues(8.dp)
	) {
		// Enable / Disable plugin
		item {
			Card(
				modifier = Modifier
					.fillMaxWidth()
					.height(IntrinsicSize.Min)
					.clip(MaterialTheme.shapes.large)
					.clickable {
						plugin.switchEnabled(context)
					},
				colors = CardDefaults.cardColors(
					containerColor = animateColorAsState(targetValue =
					if (plugin.active) {
						MaterialTheme.colorScheme.primary
					} else {
						MaterialTheme.colorScheme.surfaceVariant
					}).value,
				)
			) {
				AnimatedContent(
					targetState = plugin.enabled.value,
					transitionSpec = {
						// Compare the incoming number with the previous number.
						if (targetState) {
							slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
						} else {
							slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
						}.using(SizeTransform(clip = false))
					},
					modifier = Modifier
						.fillMaxSize()
						.padding(16.dp),
					contentAlignment = Alignment.Center
				) { enabled ->
					Text(
						text = if (plugin.allPermissionsGranted) {
							if (enabled) {
								"ENABLED"
							} else {
								"DISABLED"
							}
						} else {
							"MISSING PERMISSIONS"
						},
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.Bold,
						letterSpacing = 5.sp,
						textAlign = TextAlign.Center,
					)
				}
			}
		}

		// Permissions
		item {
			SettingsLabel(
				label = if (plugin.permissions.isEmpty()) "No permissions required" else "Required permissions",
				modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
			)
		}
		items(plugin.permissions) { permission ->
			PermissionSettings(permission = ExportedPlugins.permissions[permission]!!)
		}

		// Plugin settings
		item {
			SettingsLabel(
				label = if (plugin.pluginSettings.isEmpty()) "No settings available" else "Plugin settings",
				modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
			)
		}
		items(plugin.pluginSettings.values.toList()) { settings ->
			when (settings) {
				is PluginSettingsItem.SwitchSettingsItem -> SwitchSettingsItem(
					title = settings.title,
					description = settings.description,
					checked = settings.value.value,
					onCheckedChange = { settings.onValueChange(context, it) }
				)
			}
		}
	}
}

@Composable
fun SettingsLabel(label: String, modifier: Modifier) {
	Text(
		text = label,
		style = MaterialTheme.typography.labelMedium,
		modifier = modifier
			.fillMaxWidth()
			.padding(start = 16.dp)
	)
}

@Composable
fun PermissionSettings(
	modifier: Modifier = Modifier,
	permission: PluginPermission,
) {
	val context = LocalContext.current

	permission.granted.value = permission.checkPermission(context)

	val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		permission.granted.value = permission.checkPermission(context)
	}

	Row(
		modifier = modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable { startForResult.launch(permission.requestIntent) }
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier
				.weight(1f),
		) {
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(end = 16.dp)
			) {
				Text(
					text = permission.name,
					style = MaterialTheme.typography.labelLarge
				)
				Text(
					text = permission.description,
					style = MaterialTheme.typography.bodySmall
				)
			}
			Switch(
				checked = permission.granted.value,
				onCheckedChange = {
					startForResult.launch(permission.requestIntent)
				}
			)
		}
	}
}