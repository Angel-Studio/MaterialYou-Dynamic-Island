package fr.angel.dynamicisland.plugins

interface PluginHost {
    fun requestDisplay(plugin: BasePlugin)
    fun requestDismiss(plugin: BasePlugin)
    fun requestExpand()
    fun requestShrink()
}
