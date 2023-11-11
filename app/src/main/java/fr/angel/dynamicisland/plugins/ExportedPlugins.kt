package fr.angel.dynamicisland.plugins

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import fr.angel.dynamicisland.model.packageName
import fr.angel.dynamicisland.plugins.battery.BatteryPlugin
import fr.angel.dynamicisland.plugins.media.MediaSessionPlugin
import fr.angel.dynamicisland.plugins.notification.NotificationPlugin

class ExportedPlugins {

	companion object {

		val permissions: SnapshotStateMap<String, PluginPermission> = mutableStateMapOf(
			Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS to object : PluginPermission(
				name = "Notification access",
				description = "Allow Dynamic Island to listen to notifications and display them",
				requestIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
			) { override fun checkPermission(context: Context) : Boolean {
					val contentResolver = context.contentResolver
					val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
					val packageName = packageName
					return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName)
				} }
		)

		val plugins = arrayListOf(
			NotificationPlugin(),
			MediaSessionPlugin(),
			BatteryPlugin(),
		)

		fun setupPlugins(context: Context) {
			for (plugin in plugins) {
				plugin.permissions.forEach { permissionId ->
					val permission = permissions[permissionId] ?: return@forEach
					permission.granted.value = permission.checkPermission(context)
				}
				plugin.enabled.value = plugin.isPluginEnabled(context)
			}

			permissions.forEach { (id, permission) ->
				permission.granted.value = permission.checkPermission(context)
			}
		}

		fun getPlugin(pluginId: String): BasePlugin {
			return plugins.first { it.id == pluginId }
		}
	}
}