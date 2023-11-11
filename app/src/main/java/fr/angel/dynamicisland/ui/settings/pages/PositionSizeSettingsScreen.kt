package fr.angel.dynamicisland.ui.settings.pages

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.model.GRAVITY
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.island.IslandGravity
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.island.IslandViewState
import fr.angel.dynamicisland.ui.settings.SettingsDivider
import java.math.RoundingMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionSizeSettingsScreen() {

	val context = LocalContext.current

	// Shared Preferences
	val settingsPreferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

	val (gravitySelectedOption, onGravityOptionSelected) = remember { mutableStateOf(settingsPreferences.getString(GRAVITY, IslandGravity.Center.name)) }

	var expanded by remember { mutableStateOf(false) }
	var gravity by rememberSaveable(stateSaver = TextFieldValue.Saver) {
		mutableStateOf(TextFieldValue(settingsPreferences.getString(GRAVITY, IslandGravity.Center.name) ?: ""))
	}

	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		contentPadding = PaddingValues(16.dp)
	) {
		item {
			SettingsHeader(title = "Position")
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 0.dp)
					.wrapContentSize(Alignment.TopEnd)
			) {
				OutlinedTextField(
					value = gravity,
					onValueChange = {
						expanded = true
					},
					label = { Text("Island Gravity") },
					modifier = Modifier
						.fillMaxWidth(),
					readOnly = true,
					trailingIcon = {
						IconButton(onClick = { expanded = !expanded }) {
							Icon(
								imageVector = Icons.Filled.ArrowDropDown,
								contentDescription = "Expand"
							)
						}
					},
				)
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
				) {
					IslandGravity.values().forEach { islandGravity ->
						DropdownMenuItem(
							text = { Text(text = islandGravity.name) },
							onClick = {
								gravity = TextFieldValue(islandGravity.name)
								expanded = false

								settingsPreferences
									.edit()
									.putString(GRAVITY, islandGravity.name)
									.apply()
								IslandSettings.instance.gravity = IslandGravity.valueOf(islandGravity.name)
							},
						)
					}
				}
			}

			SettingsSlider(
				onValueChange = { IslandSettings.instance.positionX = it.roundToInt() },
				onReset = { IslandSettings.instance.positionX = 0 },
				title = "Position X",
				extension = ".dp",
				value = IslandSettings.instance.positionX.toFloat(),
				range = -LocalConfiguration.current.screenWidthDp.toFloat() / 2..LocalConfiguration.current.screenWidthDp.toFloat() / 2
			)
			SettingsSlider(
				onValueChange = { IslandSettings.instance.positionY = it.roundToInt() },
				onReset = { IslandSettings.instance.positionY = 5 },
				title = "Position Y",
				extension = ".dp",
				value = IslandSettings.instance.positionY.toFloat(),
				range = 0f..50f
			)
		}
		item {
			SettingsHeader(title = "Size")
			SettingsSlider(
				onValueChange = { IslandSettings.instance.width = it.roundToInt() },
				onReset = { IslandSettings.instance.width = 150 },
				title = "Width",
				extension = ".dp",
				value = IslandSettings.instance.width.toFloat(),
				range = IslandViewState.Opened.height.value * 3..LocalConfiguration.current.screenWidthDp.toFloat() - IslandViewState.Opened.yPosition.value * 2
			)
			SettingsSlider(
				onValueChange = { IslandSettings.instance.height = it.roundToInt() },
				onReset = { IslandSettings.instance.height = 200 },
				title = "Height",
				extension = ".dp",
				value = IslandSettings.instance.height.toFloat(),
				range = 1f..LocalConfiguration.current.screenHeightDp.toFloat() / 2
			)
		}
		item {
			SettingsHeader(title = "Corner")
			SettingsSlider(
				onValueChange = { IslandSettings.instance.cornerRadius = it.roundToInt() },
				onReset = { IslandSettings.instance.cornerRadius = 60 },
				title = "Corner radius",
				extension = ".dp",
				value = IslandSettings.instance.cornerRadius.toFloat(),
				range = 0f..100f
			)
		}
	}
}

@Composable
fun SettingsHeader(
	title: String,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall,
		)
		SettingsDivider(modifier = Modifier.padding(vertical = 8.dp))
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsSlider(
	onValueChange: (Float) -> Unit,
	onReset: () -> Unit,
	roundedTo: Int = 0,
	preciseValue: Float = 1f,
	title: String,
	extension: String = "",
	value: Float,
	range: ClosedFloatingPointRange<Float>,
) {
	val context = LocalContext.current
	val islandSettings = IslandSettings.instance

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.height(IntrinsicSize.Min),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			val textStyle = MaterialTheme.typography.labelLarge
			Row {
				Text(
					text = "$title - (",
					style = textStyle,
				)
				AnimatedContent(
					targetState = value,
					transitionSpec = {
						if (targetState > initialState) {
							fadeIn(
								initialAlpha = 0.2f,
							) with
									slideOutVertically { height -> -height } + fadeOut()
						} else {
							fadeIn(
								initialAlpha = 0.2f
							) with
									slideOutVertically { height -> height } + fadeOut()
						}.using(
							SizeTransform(clip = false)
						)
					}
				) { value ->
					Text(
						text = "${value.toBigDecimal().setScale(roundedTo, RoundingMode.UP)}",
						style = textStyle,
						color = MaterialTheme.colorScheme.primary,
					)
				}
				Text(
					text = "$extension)",
					style = textStyle,
				)
			}
			TextButton(
				onClick = {
					onReset()
					IslandSettings.instance.applySettings(context)
				},
			) {
				Text(
					text = "Reset"
				)
			}
		}
		Row {
			IconButton(
				onClick = {
					if (value > range.start) {
						onValueChange(value - preciseValue)
						IslandSettings.instance.applySettings(context)
					}
				}
			) {
				Icon(
					imageVector = Icons.Default.ArrowLeft,
					contentDescription = null,
				)
			}
			Slider(
				modifier = Modifier
					.weight(1f),
				value = animateFloatAsState(targetValue = value).value,
				onValueChange = {
					onValueChange(
						it.toBigDecimal().setScale(roundedTo, RoundingMode.UP).toFloat()
					)
				},
				valueRange = range,
				onValueChangeFinished = { islandSettings.applySettings(context) },
			)
			IconButton(
				onClick = {
					if (value < range.endInclusive) {
						onValueChange(value + preciseValue)
						IslandSettings.instance.applySettings(context)
					}
				}
			) {
				Icon(
					imageVector = Icons.Default.ArrowRight,
					contentDescription = null,
				)
			}
		}
	}
}