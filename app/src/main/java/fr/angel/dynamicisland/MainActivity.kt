package fr.angel.dynamicisland

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.model.SETTINGS_THEME_INVERTED
import fr.angel.dynamicisland.model.THEME_INVERTED
import fr.angel.dynamicisland.navigation.*
import fr.angel.dynamicisland.plugins.ExportedPlugins
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.model.DISCLOSURE_ACCEPTED
import fr.angel.dynamicisland.ui.disclosure.DisclosureScreen
import fr.angel.dynamicisland.ui.settings.settings
import fr.angel.dynamicisland.ui.theme.DynamicIslandTheme
import fr.angel.dynamicisland.ui.theme.Theme


class MainActivity : ComponentActivity() {

	private lateinit var settingsPreferences: SharedPreferences

	companion object {
		lateinit var instance: MainActivity
	}

	var actions = mutableStateListOf<@Composable () -> Unit>()

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		instance = this

		settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

		WindowCompat.setDecorFitsSystemWindows(window, false)

		// Invert theme in app
		settingsPreferences.edit().putBoolean(THEME_INVERTED, true).apply()
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED))

		setContent {
			// Setup plugins
			ExportedPlugins.setupPlugins(LocalContext.current)

			// Init
			Theme.instance.Init()
			IslandSettings.instance.loadSettings(this)

			val disclosureAccepted by remember { mutableStateOf(settingsPreferences.getBoolean(
				DISCLOSURE_ACCEPTED, false)
			) }

			if (!disclosureAccepted) {
				startActivity(Intent(this, DisclosureActivity::class.java))
				finish()
			}

			DynamicIslandTheme(
				darkTheme = Theme.instance.isDarkTheme,
			) {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					// Navigation
					val settingsRoutes = settings.map { (it as IslandDestination).route }

					val navController = rememberNavController()
					val currentBackStack by navController.currentBackStackEntryAsState()
					val currentDestination = currentBackStack?.destination
					val currentScreen : IslandDestination =
						bottomDestinations.find { it.route == currentDestination?.route } ?:
							// If current destination is contained in settings
						(settings.find { (it as IslandDestination).route == currentDestination?.route }
							?: if (currentDestination?.route == IslandPluginSettings.routeWithArgs) IslandPluginSettings else IslandHome
								) as IslandDestination

					// Top app bar
					val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

					LaunchedEffect(currentScreen) {
						actions.clear()
					}

					Scaffold(
						modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Crossfade(
										targetState = currentScreen,
									) { screen ->
										Text(
											text = if (screen == IslandHome) {
												stringResource(id = R.string.app_name)
											} else {
												screen.title
											},
											textAlign = TextAlign.Center,
											modifier = Modifier
												.fillMaxWidth()
										)
									}
								},
								navigationIcon = {
									if (
										currentDestination?.route in settingsRoutes
										|| currentDestination?.route == IslandPluginSettings.routeWithArgs
									) {
										IconButton(onClick = { navController.popBackStack() }) {
											Icon(
												imageVector = Icons.Default.ArrowBack,
												contentDescription = "Back"
											)
										}
									}
								},
								actions = {
									actions.forEach { it() }
								},
								scrollBehavior = scrollBehavior
							)
						},
						bottomBar = {
							NavigationBar {
								for (destination in bottomDestinations) {
									NavigationBarItem(
										icon = { Icon(destination.icon, contentDescription = null) },
										label = { Text(destination.title) },
										selected = currentScreen == destination
												|| (destination == fr.angel.dynamicisland.navigation.IslandSettings && settings.contains(currentScreen))
												|| (destination == IslandPlugins && currentScreen == IslandPluginSettings),
										onClick = {
											navController.navigateSingleTopTo(destination.route)
										}
									)
								}
							}
						},
					) {
						IslandNavHost(
							modifier = Modifier
								.padding(it)
								.fillMaxSize(),
							navController = navController
						)
					}
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(THEME_INVERTED, false).apply()
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED))
	}

	override fun onStop() {
		super.onStop()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(THEME_INVERTED, false).apply()
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED))
	}

	override fun onPause() {
		super.onPause()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(THEME_INVERTED, false).apply()
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED))
	}

	override fun onResume() {
		super.onResume()
		// Invert theme in app
		settingsPreferences.edit().putBoolean(THEME_INVERTED, true).apply()
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED))
	}
}