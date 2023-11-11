package fr.angel.dynamicisland.plugins

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.angel.dynamicisland.model.PLUGIN_SETTINGS_KEY

sealed class PluginSettingsItem {
	abstract val title: String
	abstract val description: String

	class SwitchSettingsItem(
		override val title: String,
		override val description: String,
		var id: String,
		var value: MutableState<Boolean> = mutableStateOf(false),
		val onValueChange: (Context, Boolean) -> Unit = { context, enabled ->
			val editor = context.getSharedPreferences(PLUGIN_SETTINGS_KEY, Context.MODE_PRIVATE).edit()
			editor.putBoolean(id, enabled)
			editor.apply()

			value.value = enabled
		},
	) : PluginSettingsItem() {
		fun isSettingEnabled(context: Context, id: String): Boolean {
			val preferences = context.getSharedPreferences(PLUGIN_SETTINGS_KEY, Context.MODE_PRIVATE)
			return preferences.getBoolean(id, value.value)
		}
	}
}