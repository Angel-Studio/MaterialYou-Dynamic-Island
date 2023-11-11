package fr.angel.dynamicisland.island

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Island {
	var isScreenOn by mutableStateOf(true)
	var isInLandscape by mutableStateOf(false)
}