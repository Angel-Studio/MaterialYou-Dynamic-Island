package fr.angel.dynamicisland.plugins

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import fr.angel.dynamicisland.model.SETTINGS_CHANGED
import fr.angel.dynamicisland.model.SETTINGS_KEY

abstract class BasePlugin {
	abstract val id: String
	abstract val name: String
	abstract val description: String
	abstract val permissions: ArrayList<String>
	abstract var enabled: MutableState<Boolean>
	abstract var pluginSettings: MutableMap<String, PluginSettingsItem>

	protected var host: PluginHost? = null

	val active get() = enabled.value && allPermissionsGranted

	abstract fun canExpand(): Boolean

	fun onCreate(host: PluginHost) {
		this.host = host
		onPluginCreate()
	}

	abstract fun onPluginCreate()

	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	abstract fun Composable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	)
	abstract fun onClick()
	abstract fun onDestroy()
	@Composable
	abstract fun PermissionsRequired()

	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	abstract fun LeftOpenedComposable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	)
	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	abstract fun RightOpenedComposable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	)

	abstract fun onRightSwipe()
	abstract fun onLeftSwipe()

	val allPermissionsGranted: Boolean
		get() = permissions.all { permission ->
			// Check if permission is granted
			ExportedPlugins.permissions[permission]?.granted?.value ?: false
		}

	fun isPluginEnabled(context: Context): Boolean {
		val preferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
		return preferences.getBoolean(id, false)
	}

	fun switchEnabled(context: Context, enabled: Boolean = !this.enabled.value): Boolean {

		// Check if all permissions are granted
		return if (allPermissionsGranted || !enabled) {
			// If all permissions are granted, we can enable the plugin
			// Save value in settings preferences
			val editor = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE).edit()
			editor.putBoolean(id, enabled)
			editor.apply()

			context.sendBroadcast(Intent(SETTINGS_CHANGED))
			this.enabled.value = enabled

			true
		} else {
			// If not, we can't enable the plugin
			false
		}
	}
}