package fr.angel.dynamicisland.island

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.angel.dynamicisland.model.*

class IslandSettings {

	companion object {
		val instance = IslandSettings()
	}

	var positionX by mutableIntStateOf(0)
	var positionY by mutableIntStateOf(5)
	var width by mutableIntStateOf(150)
	var height by mutableIntStateOf(200)
	var cornerRadius by mutableIntStateOf(60)
	var gravity by mutableStateOf(IslandGravity.Center)

	var enabledApps = mutableStateListOf<String>()

	var showOnLockScreen by mutableStateOf(false)
	var showInLandscape by mutableStateOf(false)
	var showBorders by mutableStateOf(false)

	var autoHideOpenedAfter by mutableFloatStateOf(5000f)

	fun applySettings(context: Context) {
		val settings = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
		settings.edit()
			.putInt(POSITION_X, positionX)
			.putInt(POSITION_Y, positionY)
			.putInt(SIZE_X, width)
			.putInt(SIZE_Y, height)
			.putInt(CORNER_RADIUS, cornerRadius)
			.putStringSet(ENABLED_APPS, enabledApps.toSet())
			.putBoolean(SHOW_ON_LOCK_SCREEN, showOnLockScreen)
			.putBoolean(SHOW_IN_LANDSCAPE, showInLandscape)
			.putFloat(AUTO_HIDE_OPENED_AFTER, autoHideOpenedAfter)
			.putBoolean(SHOW_BORDER, showBorders)
			.putString(GRAVITY, gravity.name)
			.apply()
	}

	fun loadSettings(context: Context) {
		val settings = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
		positionX = settings.getInt(POSITION_X, 0)
		positionY = settings.getInt(POSITION_Y, 5)
		width = settings.getInt(SIZE_X, 150)
		height = settings.getInt(SIZE_Y, 200)
		cornerRadius = settings.getInt(CORNER_RADIUS, 60)
		enabledApps.clear()
		enabledApps.addAll(settings.getStringSet(ENABLED_APPS, setOf()) ?: setOf())
		showOnLockScreen = settings.getBoolean(SHOW_ON_LOCK_SCREEN, false)
		showInLandscape = settings.getBoolean(SHOW_IN_LANDSCAPE, false)
		autoHideOpenedAfter = settings.getFloat(AUTO_HIDE_OPENED_AFTER, 5000f)
		showBorders = settings.getBoolean(SHOW_BORDER, false)
		gravity = IslandGravity.valueOf(settings.getString(GRAVITY, IslandGravity.Center.name) ?: IslandGravity.Center.name)
	}
}

enum class IslandGravity {
	Left,
	Right,
	Center
}