package fr.angel.dynamicisland.model.service

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import fr.angel.dynamicisland.model.ACTION_CLOSE
import fr.angel.dynamicisland.model.ACTION_OPEN_CLOSE
import fr.angel.dynamicisland.model.NOTIFICATION_POSTED
import fr.angel.dynamicisland.model.NOTIFICATION_REMOVED
import fr.angel.dynamicisland.island.IslandSettings


class NotificationService : NotificationListenerService() {

	var notifications = mutableStateListOf<StatusBarNotification>()

	companion object {
		private var instance: NotificationService? = null

		fun getInstance(): NotificationService? {
			return instance
		}
	}

	private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {

			val statusBarNotification = notifications.firstOrNull { it.id == intent.getIntExtra("id", 0) } ?: return
			Log.d("NotificationService", "onReceive: ${statusBarNotification.id}, ${statusBarNotification.id}, ${statusBarNotification.notification.actions?.size}")
			val notification = statusBarNotification.notification

			if (intent.action == ACTION_OPEN_CLOSE) {
				// Logic to remove notification
				if (notification.deleteIntent != null) {
					// Delete notification
					notification.deleteIntent.send()
				} else {
					// If notification is not deletable, cancel it
					cancelNotification(statusBarNotification.key)
				}

				// Start content intent from notification
				notification.contentIntent.send()
			}
			if (intent.action == ACTION_CLOSE) {
				// Logic to remove notification
				if (notification.deleteIntent != null) {
					notification.deleteIntent.send()
				} else {
					cancelNotification(statusBarNotification.key)
				}
			}
		}
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
		Log.d("NotificationService", "onCreate: ")

		// Register broadcast receiver
		registerReceiver(mBroadcastReceiver, IntentFilter().apply {
			addAction(ACTION_OPEN_CLOSE)
			addAction(ACTION_CLOSE)
		}, RECEIVER_EXPORTED)
	}

	override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
		super.onNotificationPosted(statusBarNotification)

		val notification = statusBarNotification.notification

		// Check if notification is in the enabled apps list
		if ((statusBarNotification.packageName !in IslandSettings.instance.enabledApps) && !IslandSettings.instance.enabledApps.isEmpty()) return

		Log.d("NotificationService", "Notification Category: ${notification.category}")
		// Ignore notifications from ->
		when (notification.category) {
			Notification.CATEGORY_SYSTEM, // Ignore system notifications
			Notification.CATEGORY_SERVICE, // Ignore service notifications
			Notification.CATEGORY_TRANSPORT, // Ignore media player controls notifications
			-> return
		}

		// Add notification to list
		notifications.add(statusBarNotification)
		Log.d("NotificationService", "Posted: $notifications")
		Log.d("NotificationService", "Posted: ${notifications.size}")

		sendBroadcast(Intent(NOTIFICATION_POSTED).apply {
			putExtra("id", statusBarNotification.id)
			putExtra("package_name", statusBarNotification.packageName)
			putExtra("category", notification.category)

			putExtra("time", statusBarNotification.postTime)
			putExtra("icon_large", notification.getLargeIcon())
			putExtra("icon_small", notification.smallIcon)

			putExtra("title", notification.extras.getString("android.title") ?: "Empty title")
			putExtra("body", notification.extras.getString("android.text") ?: "Empty body")
		})
	}

	override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {

		// Remove notification from list
		notifications.removeIf { it.id == statusBarNotification.id }
		Log.d("NotificationService", "Removed: $notifications")
		Log.d("NotificationService", "Latest notification: ${notifications.firstOrNull()}")

		// Send broadcast
		sendBroadcast(Intent(NOTIFICATION_REMOVED).apply {
			putExtra("id", statusBarNotification.id)
		})
	}

	override fun onDestroy() {
		super.onDestroy()
		instance = null
		unregisterReceiver(mBroadcastReceiver)
	}
}