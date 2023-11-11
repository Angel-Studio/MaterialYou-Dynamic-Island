package fr.angel.dynamicisland.plugins

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

open class PluginPermission(
	val name: String,
	val description: String,
	val requestIntent: Intent,
	val granted: MutableState<Boolean> = mutableStateOf(false),
) {
	open fun checkPermission(context: Context) : Boolean { return false }
}