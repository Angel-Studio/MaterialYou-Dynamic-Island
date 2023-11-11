package fr.angel.dynamicisland.ui.settings.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.R
import fr.angel.dynamicisland.ui.settings.SettingsDivider


@Composable
fun AboutSettingsScreen() {
	val context = LocalContext.current

	val manager = context.packageManager
	val info = manager.getPackageInfo(
		context.packageName, 0
	)
	val version = info.versionName

	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		contentPadding = PaddingValues(16.dp)
	) {
		item {
			TextSettingsItem(
				title = "Developer",
				description = "Jules P",
				icon = Icons.Filled.Code,
			)
			TextSettingsItem(
				icon = Icons.Default.Mail,
				title = "Mail me",
				description = "studioangel.fr.customerservice@gmail.com",
				onClick = {
					val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Dynamic Island Contact", "studioangel.fr.customerservice@gmail.com")
					clipboard.setPrimaryClip(clip)
					Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
				}
			)
			TextSettingsItem(
				icon = Icons.Default.BugReport,
				title = "GitHub",
				description = "https://github.com/angel-studio",
				onClick = {
					val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Dynamic Island GitHub", "https://github.com/angel-studio")
					clipboard.setPrimaryClip(clip)
					Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
				}
			)
			TextSettingsItem(
				icon = Icons.Default.Info,
				title = "Version",
				description = version
			)
			SettingsDivider(modifier = Modifier.padding(vertical = 4.dp))
			TextSettingsItem(
				icon = Icons.Default.VerifiedUser,
				title = "Disclosure",
				description = context.getString(R.string.disclosure),
			)
			TextSettingsItem(
				icon = Icons.Default.Policy,
				title = "Privacy Policy",
				description = "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/privacy-policy",
				onClick = {
					val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Dynamic Island Privacy Policy", "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/privacy-policy")
					clipboard.setPrimaryClip(clip)
					Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
				}
			)
			TextSettingsItem(
				icon = Icons.Default.Shield,
				title = "Terms & Conditions",
				description = "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/terms-conditions",
				onClick = {
					val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
					val clip = ClipData.newPlainText("Dynamic Island Terms & Conditions", "https://sites.google.com/view/angel-studio-fr/material-you-dynamic-island/terms-conditions")
					clipboard.setPrimaryClip(clip)
					Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
				}
			)
		}
	}
}

@Composable
fun TextSettingsItem(
	icon: ImageVector? = null,
	title: String,
	description: String? = null,
	onClick: () -> Unit = {}
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (icon != null) {
			Icon(
				imageVector = icon,
				contentDescription = null,
			)
			Spacer(modifier = Modifier.width(16.dp))
		}
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
			if (description != null) {
				Text(
					text = description,
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
	}
}