package fr.angel.dynamicisland.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

interface IslandDestination {
	val icon: ImageVector
	val route: String
	val title: String
}

object IslandHome : IslandDestination {
	override val icon = Icons.Filled.Home
	override val route = "home"
	override val title = "Home"
}

object IslandPlugins : IslandDestination {
	override val icon = Icons.Filled.Extension
	override val route = "plugins"
	override val title = "Plugins"
}

object IslandSettings : IslandDestination {
	override val icon = Icons.Filled.Settings
	override val route = "settings"
	override val title = "Settings"
}

object IslandPluginSettings : IslandDestination {
	override val icon = Icons.Filled.Settings
	override val route = "settings_item"
	override val title = "Plugin Settings"

	const val pluginArg = "plugin_id"

	val routeWithArgs = "$route/{$pluginArg}"
	val arguments = listOf(
		navArgument(pluginArg) { type = NavType.StringType }
	)
	val deepLinks = listOf(
		navDeepLink { uriPattern = "dynamicisland://$route/{$pluginArg}" }
	)
}

val bottomDestinations = listOf(
	IslandHome,
	IslandPlugins,
	IslandSettings,
)
