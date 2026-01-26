package fr.angel.dynamicisland.plugins.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import fr.angel.dynamicisland.R
import fr.angel.dynamicisland.model.BATTERY_SHOW_PERCENTAGE
import fr.angel.dynamicisland.model.service.IslandOverlayService
import fr.angel.dynamicisland.plugins.BasePlugin
import fr.angel.dynamicisland.plugins.PluginSettingsItem
import fr.angel.dynamicisland.ui.theme.BatteryEmpty
import fr.angel.dynamicisland.ui.theme.BatteryFull

class BatteryPlugin(
	override val id: String = "BatteryPlugin",
	override val name: String = "Battery",
	override val description: String = "Show the current battery level when charging",
	override val permissions: ArrayList<String> = arrayListOf(),
	override var enabled: MutableState<Boolean> = mutableStateOf(false),
	override var pluginSettings: MutableMap<String, PluginSettingsItem> = mutableMapOf(
		BATTERY_SHOW_PERCENTAGE to PluginSettingsItem.SwitchSettingsItem(
			title = "Show percentage",
			description = "Show the battery percentage",
			id = BATTERY_SHOW_PERCENTAGE,
			value = mutableStateOf(true),
		),
	),
) : BasePlugin() {

	var batteryPercent by mutableStateOf(0)

	override fun canExpand(): Boolean { return false } // TODO: Add expandable function
	
	private var batteryReceiver: BatteryReceiver? = null

	override fun onPluginCreate() {
		val context = host as? Context ?: return
		batteryReceiver = BatteryReceiver()
		context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_EXPORTED)

		// Check for plugin internal settings
		pluginSettings.values.forEach {
			if (it is PluginSettingsItem.SwitchSettingsItem) {
				it.value.value = it.isSettingEnabled(context, it.id)
			}
		}
	}

	inner class BatteryReceiver : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			// Get battery status extra
			val status = intent.extras!!.getInt(BatteryManager.EXTRA_STATUS)
			val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING

			// If charging, add plugin else remove it
			if (isCharging) {
				host?.requestDisplay(this@BatteryPlugin)
				val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
				val maxBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
				// Set battery percent
				batteryPercent = (batteryLevel * 100 / maxBatteryLevel)
			} else {
				host?.requestDismiss(this@BatteryPlugin)
			}
		}
	}

	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	override fun Composable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	) {
		BatteryView(batteryPercent)
	}

	@Composable
	private fun BatteryView(
		batteryPercent: Int
	) {
		Row(
			modifier = Modifier
				.fillMaxSize(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			// TODO: Replace with wave animation when jitpack will be fixed
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.fillMaxWidth(batteryPercent / 100f)
					.background(MaterialTheme.colorScheme.tertiary),
				contentAlignment = Alignment.Center
			) {
				if (batteryPercent == 100) {
					Icon(
						imageVector = Icons.Default.BatteryChargingFull,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onTertiary,
						modifier = Modifier
							.rotate(90f)
							.fillMaxSize(.35f)
					)
				}
			}
		}
	}

	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	override fun LeftOpenedComposable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	) {
		WaveLoading(
			progress = animateFloatAsState(targetValue = batteryPercent.toFloat() / 100).value,
			foreDrawType = DrawType.DrawColor(pointBetweenColors(BatteryEmpty, BatteryFull, batteryPercent.toFloat() / 100)),
			backDrawType = DrawType.DrawColor(pointBetweenColors(pointBetweenColors(BatteryEmpty, BatteryFull, batteryPercent.toFloat() / 100), MaterialTheme.colorScheme.surface, .75f)),
			modifier = Modifier
				.fillMaxHeight()
				.aspectRatio(1f)
		) {
			Image(
				painter = painterResource(id = R.drawable.ic_charging_full),
				contentDescription = "Battery level: $batteryPercent%",
			)
		}
	}

	override fun onClick() {}

	override fun onLeftSwipe() {}
	override fun onRightSwipe() {}

	@OptIn(ExperimentalSharedTransitionApi::class)
	@Composable
	override fun RightOpenedComposable(
		sharedTransitionScope: SharedTransitionScope,
		animatedContentScope: AnimatedContentScope
	) {
		if ((pluginSettings[BATTERY_SHOW_PERCENTAGE] as PluginSettingsItem.SwitchSettingsItem).value.value) {
			Text(
				text = "$batteryPercent%",
				modifier = Modifier.padding(end = 4.dp),
				style = MaterialTheme.typography.labelLarge
			)
		}
	}

	override fun onDestroy() {
		val context = host as? Context ?: return
		try {
			batteryReceiver?.let { context.unregisterReceiver(it) }
		} catch (_: Exception) {} // Ignore exception if receiver is not registered
	}

	@Composable
	override fun PermissionsRequired() {

	}
}

fun pointBetweenColors(from: Float, to: Float, percent: Float): Float =
	from + percent * (to - from)

fun pointBetweenColors(from: Color, to: Color, percent: Float) =
	Color(
		red = pointBetweenColors(from.red, to.red, percent),
		green = pointBetweenColors(from.green, to.green, percent),
		blue = pointBetweenColors(from.blue, to.blue, percent),
		alpha = pointBetweenColors(from.alpha, to.alpha, percent),
	)