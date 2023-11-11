package fr.angel.dynamicisland.plugins.notification

import android.app.Notification
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.service.notification.StatusBarNotification

class NotificationMeta(
	var title: String?,
	var body: String,
	var id: Int,
	var iconDrawable: Drawable,
	var packageName: String,
	var actions: List<Notification.Action>,
	var all: Bundle,
	var statusBarNotification: StatusBarNotification
) {
	fun getAppName(context: Context): String {
		val applicationInfo: ApplicationInfo = context.packageManager?.getApplicationInfo(packageName, 0) ?: return "Error getting app name"
		return (context.packageManager?.getApplicationLabel(applicationInfo) ?: "Error getting app name").toString()
	}
}