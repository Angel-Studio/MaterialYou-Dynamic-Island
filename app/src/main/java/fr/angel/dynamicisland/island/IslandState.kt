package fr.angel.dynamicisland.island

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface IslandState {
	val yPosition: Dp
		get() = IslandSettings.instance.positionY.dp
	val xPosition: Dp
		get() = IslandSettings.instance.positionX.dp
	val height: Dp
	val width: Dp
	val cornerPercentage: Float
	val state: IslandStates
}

sealed class IslandViewState : IslandState {

	object Closed : IslandViewState() {
		override val height: Dp = 34.dp
		override val width: Dp = 34.dp
		override val cornerPercentage: Float = 100f
		override val state: IslandStates = IslandStates.Closed
	}

	object Opened : IslandViewState() {
		override val height: Dp = 34.dp
		override val width: Dp
			get() = if (IslandSettings.instance.width.dp < 34.dp) 34.dp else IslandSettings.instance.width.dp
		override val cornerPercentage: Float = 100f
		override val state: IslandStates = IslandStates.Opened
	}

	class Expanded(configuration: Configuration) : IslandViewState() {
		override val height: Dp
			get() = IslandSettings.instance.height.dp
		override val width: Dp = configuration.screenWidthDp.dp - 16.dp
		override val cornerPercentage: Float
			get() = IslandSettings.instance.cornerRadius.toFloat()
		override val state: IslandStates = IslandStates.Expanded
	}
}

enum class IslandStates {
	Closed,
	Opened,
	Expanded
}