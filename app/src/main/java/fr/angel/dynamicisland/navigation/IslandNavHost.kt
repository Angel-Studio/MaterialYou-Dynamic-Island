package fr.angel.dynamicisland.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.angel.dynamicisland.plugins.ExportedPlugins
import fr.angel.dynamicisland.plugins.PluginSettingsScreen
import fr.angel.dynamicisland.ui.home.HomeScreen
import fr.angel.dynamicisland.ui.plugins.PluginScreen
import fr.angel.dynamicisland.ui.settings.*
import fr.angel.dynamicisland.ui.settings.pages.*

@Composable
fun IslandNavHost(
	modifier: Modifier = Modifier,
	navController: NavHostController,
) {
	NavHost(
		navController = navController,
		startDestination = bottomDestinations.first().route,
		modifier = modifier,
	) {
		// Main destinations
		composable(IslandHome.route) {
			HomeScreen(
				onGetStartedClick = {
					navController.navigateSingleTopTo(IslandPlugins.route)
				},
				onShowDisclosureClick = {
					navController.navigateSingleTopTo(AboutSetting.route)
				},
			)
		}
		composable(IslandPlugins.route) {
			PluginScreen(
				onPluginClicked = { plugin ->
					navController.navigateToPluginSettings(plugin.id)
				}
			)
		}
		composable(IslandSettings.route) {
			SettingsScreen(
				onSettingClicked = { setting ->
					navController.navigate(setting.route)
				}
			)
		}
		// Settings screens
		composable(ThemeSetting.route) {
			ThemeSettingsScreen()
		}
		composable(BehaviorSetting.route) {
			BehaviorSettingsScreen()
		}
		composable(PositionSizeSetting.route) {
			PositionSizeSettingsScreen()
		}
		composable(EnabledAppsSetting.route) {
			EnabledAppsSettingsScreen()
		}
		composable(AboutSetting.route) {
			AboutSettingsScreen()
		}

		// Plugin settings
		composable(
			route = IslandPluginSettings.routeWithArgs,
			arguments = IslandPluginSettings.arguments,
			deepLinks = IslandPluginSettings.deepLinks,
		) { backStackEntry ->
			val pluginId = backStackEntry.arguments?.getString(IslandPluginSettings.pluginArg)

			if (pluginId != null) {
				PluginSettingsScreen(
					plugin = ExportedPlugins.getPlugin(pluginId)
				)
			}
		}
	}
}

fun NavHostController.navigateSingleTopTo(route: String) =
	this.navigate(route) {
		// Pop up to the start destination of the graph to
		// avoid building up a large stack of destinations
		// on the back stack as users select items
		popUpTo(
			this@navigateSingleTopTo.graph.findStartDestination().id
		) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
		// Restore state when reselecting a previously selected item
		restoreState = true
	}

fun NavHostController.navigateToPluginSettings(pluginId: String) {
	this.navigate(route = "${IslandPluginSettings.route}/$pluginId") {
		popUpTo(IslandSettings.route) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
	}
}