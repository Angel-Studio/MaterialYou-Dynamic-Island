package fr.angel.dynamicisland.ui.settings.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.model.STYLE
import fr.angel.dynamicisland.model.THEME
import fr.angel.dynamicisland.island.IslandSettings
import fr.angel.dynamicisland.ui.settings.SettingsDivider
import fr.angel.dynamicisland.ui.settings.radioOptions
import fr.angel.dynamicisland.ui.theme.Theme

@Composable
fun ThemeSettingsScreen() {

	val context = LocalContext.current

	val isSystemInDarkTheme = isSystemInDarkTheme()

	// Shared Preferences
	val settingsPreferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

	val (themeSelectedOption, onThemeOptionSelected) = remember { mutableStateOf(settingsPreferences.getString(THEME, "System")) }
	val (styleSelectedOption, onStyleOptionSelected) = remember { mutableStateOf(Theme.instance.themeStyle) }


	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		SwitchSettingsItem(
			title = "Show borders",
			description = "Show borders around the island",
			checked = IslandSettings.instance.showBorders
		) {
			IslandSettings.instance.showBorders = it
			IslandSettings.instance.applySettings(context)
		}
		OutlinedCard(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp)
				.height(IntrinsicSize.Min),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
			) {
				Text(
					text = "Theme preference",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
				SettingsDivider(modifier = Modifier
					.padding(vertical = 8.dp)
					.padding(horizontal = 16.dp))
				Column(Modifier.selectableGroup()) {
					radioOptions.forEach { text ->
						Row(
							Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.medium)
								.selectable(
									selected = (text == themeSelectedOption),
									onClick = {
										onThemeOptionSelected(text)
										settingsPreferences
											.edit()
											.putString(THEME, text)
											.apply()
										Theme.instance.isDarkTheme = when (text) {
											"System" -> {
												isSystemInDarkTheme
											}
											"Dark" -> {
												true
											}
											"Light" -> {
												false
											}
											else -> {
												isSystemInDarkTheme
											}
										}
									},
									role = Role.RadioButton
								)
								.padding(horizontal = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = (text == themeSelectedOption),
								onClick = null // null recommended for accessibility with screenreaders
							)
							Text(
								text = text,
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.padding(start = 16.dp)
							)
						}
					}
				}
			}
		}
		OutlinedCard(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp)
				.height(IntrinsicSize.Min),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
			) {
				Text(
					text = "Style preference",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
				SettingsDivider(modifier = Modifier
					.padding(vertical = 8.dp)
					.padding(horizontal = 16.dp))
				Column(Modifier.selectableGroup()) {
					Theme.ThemeStyle.values().forEach { themeStyle ->
						Row(
							Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.medium)
								.selectable(
									selected = (themeStyle == styleSelectedOption),
									onClick = {
										onStyleOptionSelected(themeStyle)
										Theme.instance.themeStyle = themeStyle
										settingsPreferences
											.edit()
											.putString(STYLE, themeStyle.name)
											.apply()
									},
									role = Role.RadioButton
								)
								.padding(horizontal = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							val stylePreviewColor =
								if (Theme.instance.themeStyle.name != Theme.ThemeStyle.MaterialYou.name) {
									if (Theme.instance.isDarkTheme) {
										if (Theme.instance.themeStyle.darkScheme != null) {
											themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
										} else {
											themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
										}
									} else {
										if (Theme.instance.themeStyle.lightScheme != null) {
											themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
										} else {
											themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
										}
									}
								} else {
									if (Theme.instance.isDarkTheme) {
										themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
									} else {
										themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
									}
								}

							RadioButton(
								selected = (themeStyle == styleSelectedOption),
								onClick = null, // null recommended for accessibility with screenreaders
								colors = RadioButtonDefaults.colors(
									selectedColor = stylePreviewColor,
									unselectedColor = stylePreviewColor,
								)
							)
							Text(
								text = themeStyle.styleName,
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.padding(start = 16.dp).weight(1f)
							)
							Box(
								modifier = Modifier
									.size(24.dp)
									.aspectRatio(1f)
									.background(
										color = stylePreviewColor,
										shape = CircleShape
									)
							)
						}
					}
				}
			}
		}
	}
}