package fr.angel.dynamicisland.ui.plugins

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.plugins.BasePlugin
import fr.angel.dynamicisland.plugins.ExportedPlugins

@Composable
fun PluginScreen(
	onPluginClicked: (BasePlugin) -> Unit,
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(8.dp)
	) {
		items(ExportedPlugins.plugins) { plugin ->
			PluginCard(
				plugin = plugin,
				enabled = plugin.enabled.value,
				permissionsGranted = plugin.allPermissionsGranted,
				onPluginClicked = onPluginClicked,
			)
		}
	}
}

@Composable
fun PluginCard(
	modifier: Modifier = Modifier,
	plugin: BasePlugin,
	expanded: Boolean = false,
	permissionsGranted: Boolean = false,
	enabled: Boolean,
	onPluginClicked: (BasePlugin) -> Unit,
	content: @Composable () -> Unit = { },
) {
	val context = LocalContext.current
	val expandedState by remember { mutableStateOf(expanded) }

	Card(
		modifier = modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable { onPluginClicked(plugin) },
		colors = CardDefaults.cardColors(
			containerColor = animateColorAsState(targetValue = if (!plugin.active) {
				MaterialTheme.colorScheme.surfaceVariant
			} else {
				MaterialTheme.colorScheme.secondaryContainer
			}).value,
		),
	) {
		Column(
			modifier = Modifier
				.padding(16.dp)
				.animateContentSize()
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				PluginHeader(
					modifier = Modifier.weight(1f),
					title = plugin.name,
					description = plugin.description,
				)
				Spacer(modifier = Modifier.width(8.dp))
				Switch(
					checked = enabled && permissionsGranted,
					onCheckedChange = {
						plugin.switchEnabled(context, it)
					},
					enabled = permissionsGranted,
				)
				IconButton(onClick = {
					onPluginClicked(plugin)
				}) {
					Icon(
						imageVector = Icons.Rounded.NavigateNext,
						contentDescription = null
					)
				}
			}
			AnimatedVisibility(
				visible = expandedState
			) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					content()
				}
			}
		}
	}
}

@Composable
fun PluginHeader(
	modifier: Modifier = Modifier,
	title: String,
	description: String
) {
	Column(
		modifier = modifier
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium
		)
		Text(
			text = description,
			style = MaterialTheme.typography.labelMedium
		)
	}
}