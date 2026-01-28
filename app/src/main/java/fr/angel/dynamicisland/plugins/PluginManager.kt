package fr.angel.dynamicisland.plugins

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import fr.angel.dynamicisland.plugins.battery.BatteryPlugin
import fr.angel.dynamicisland.plugins.media.MediaSessionPlugin
import fr.angel.dynamicisland.plugins.notification.NotificationPlugin

class PluginManager(
    private val context: Context,
    private val host: PluginHost
) {
    val allPlugins = listOf(
        NotificationPlugin(),
        MediaSessionPlugin(),
        BatteryPlugin()
    )

    val activePlugins = mutableStateListOf<BasePlugin>()

    fun initialize() {
        allPlugins.forEach { plugin ->
            plugin.enabled.value = plugin.isPluginEnabled(context)
            if (plugin.active) {
                plugin.onCreate(host)
            }
        }
    }

    fun onDestroy() {
        allPlugins.forEach { it.onDestroy() }
    }

    fun requestDisplay(plugin: BasePlugin) {
        if (!activePlugins.contains(plugin)) {
            // Simple priority: Notification > Media > Battery
            val index = allPlugins.indexOfFirst { it.id == plugin.id }
            var insertAt = activePlugins.size
            for (i in activePlugins.indices) {
                val activeIndex = allPlugins.indexOfFirst { it.id == activePlugins[i].id }
                if (index < activeIndex) {
                    insertAt = i
                    break
                }
            }
            activePlugins.add(insertAt, plugin)
        }
    }

    fun requestDismiss(plugin: BasePlugin) {
        activePlugins.remove(plugin)
    }
}
