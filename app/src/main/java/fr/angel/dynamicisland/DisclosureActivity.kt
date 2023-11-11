package fr.angel.dynamicisland

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import fr.angel.dynamicisland.model.DISCLOSURE_ACCEPTED
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.navigation.*
import fr.angel.dynamicisland.ui.disclosure.Disclosure
import fr.angel.dynamicisland.ui.disclosure.DisclosureScreen
import fr.angel.dynamicisland.ui.disclosure.Link
import fr.angel.dynamicisland.ui.theme.DynamicIslandTheme
import fr.angel.dynamicisland.ui.theme.Theme

class DisclosureActivity : ComponentActivity() {

	private lateinit var settingsPreferences: SharedPreferences

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {

			Theme.instance.Init()
			WindowCompat.setDecorFitsSystemWindows(window, false)

			settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

			// Top app bar
			val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

			val disclosureScreens = arrayListOf(
				Disclosure(
					title = "Welcome to Dynamic Island",
					description = "In the same way as IOS does, Dynamic Island will display " +
							"an interactive notch that can change to show you incoming calls, alerts, " +
							"notifications, turn-by-turn navigation, music playback, etc. You can tap or " +
							"long press the area to access different functions or launch apps.",
					illustration = R.raw.welcome,
					cropIllustration = false,
				),
				Disclosure(
					title = "Use of Accessibility API and Overlay Permissions",
					description = "Dynamic Island uses the accessibility services to " +
							"display the notch and nothing else. Dynamic Island will never ask for permissions that are " +
							"not required for the app to work. No personal information is collected. " +
							"You can read the privacy policy at any time for more information.",
					illustration = R.raw.layer,
					cropIllustration = false,
				),
				Disclosure(
					title = "Plugin Permissions",
					description = "Dynamic Island will ask you to grant permissions to the plugins " +
							"you install. This is required to allow the plugins to work properly. " +
							"Dynamic Island will never ask for permissions that are not required " +
							"for the plugin to work.",
					illustration = R.raw.plugin,
				),
				Disclosure(
					title = "Privacy",
					description = "Dynamic Island respects your privacy. We don't collect any personal " +
							"information. We don't track you. We don't sell your data. We don't use your data " +
							"for any other purpose than to provide you with the best experience possible.",
					illustration = R.raw.privacy,
					link = Link(
						text = "Read our privacy policy",
						url = "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/privacy-policy",
					)
				),
				Disclosure(
					title = "Terms of Service",
					description = "By using Dynamic Island, you agree to our terms of service. " +
							"Dynamic Island is provided as is, without any warranty. We are not responsible " +
							"for any damage caused by the use of Dynamic Island.",
					illustration = R.raw.signature,
					link = Link(
						text = "Read our terms of service",
						url = "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/terms-conditions",
					)
				),
			)
			var step by remember { mutableStateOf(0) }

			DynamicIslandTheme(
				darkTheme = Theme.instance.isDarkTheme,
			) {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					Scaffold(
						modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
						topBar = {
							CenterAlignedTopAppBar(
								title = { Text(
									text = disclosureScreens[step].title,
									textAlign = TextAlign.Center,
								) },
								scrollBehavior = scrollBehavior
							)
						},
					) {
						DisclosureScreen(
							modifier = Modifier.padding(it),
							screens = disclosureScreens,
							step = step,
							onNext = { step++ },
							onPrevious = { step-- },
							onStart = {
								settingsPreferences.edit().putBoolean(DISCLOSURE_ACCEPTED, true).apply()
								startActivity(Intent(this, MainActivity::class.java))
								finish()
							},
						)
					}
				}
			}
		}
	}
}