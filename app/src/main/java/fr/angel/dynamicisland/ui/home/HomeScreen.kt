package fr.angel.dynamicisland.ui.home

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.rounded.ExtensionOff
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import fr.angel.dynamicisland.R
import fr.angel.dynamicisland.model.BATTERY_OPTIMIZATION_DISMISSED
import fr.angel.dynamicisland.model.DISCLOSURE_ACCEPTED
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.model.packageName
import fr.angel.dynamicisland.model.service.IslandOverlayService
import fr.angel.dynamicisland.plugins.ExportedPlugins


@Composable
fun HomeScreen(
    onGetStartedClick: () -> Unit,
    onShowDisclosureClick: () -> Unit,
) {

    val context = LocalContext.current

    val settingsPreferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)
    var optimizationDismissed by remember {
        mutableStateOf(
            settingsPreferences.getBoolean(
                BATTERY_OPTIMIZATION_DISMISSED,
                false
            )
        )
    }
    var disclosureAccepted by remember {
        mutableStateOf(
            settingsPreferences.getBoolean(
                DISCLOSURE_ACCEPTED,
                false
            )
        )
    }

    // Celebration animation
    val celebrateComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebrate))
    var isCelebrating by remember { mutableStateOf(false) }

    // Permissions
    var isOverlayGranted by remember { mutableStateOf(canDrawOverlays(context)) }
    var isAccessibilityGranted by remember {
        mutableStateOf(
            isAccessibilityServiceEnabled(
                IslandOverlayService::class.java,
                context
            )
        )
    }

    // Permissions request
    val startForPermissionResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isAccessibilityGranted =
                isAccessibilityServiceEnabled(IslandOverlayService::class.java, context)
            isOverlayGranted = canDrawOverlays(context)

            if (isAccessibilityGranted && isOverlayGranted) {
                isCelebrating = true
                Handler(Looper.getMainLooper()).postDelayed({
                    isCelebrating = false
                }, celebrateComposition?.duration?.toLong() ?: 0)
            }
        }

    fun switchAccessibilityService() {
        if (!isAccessibilityGranted) {
            // Start the accessibility service settings activity
            startForPermissionResult.launch(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                null
            )
        } else {
            // Automatically disable the accessibility service
            IslandOverlayService.getInstance()?.disableSelf()
            isAccessibilityGranted = false
        }
    }

    // Battery optimization
    val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            !powerManager.isIgnoringBatteryOptimizations(
                packageName
            )
        )
    }
    val startForBatteryOptimizationResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations =
                !powerManager.isIgnoringBatteryOptimizations(packageName)
        }

    // UI
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            PermissionsCard(
                isOverlayGranted = isOverlayGranted,
                isAccessibilityGranted = isAccessibilityGranted,
                startForResult = startForPermissionResult,
                switchAccessibility = { switchAccessibilityService() }
            )
        }

        item {
            ServiceStatusCard(
                isAccessibilityGranted = isAccessibilityGranted,
                switchAccessibility = { switchAccessibilityService() }
            )
        }

        item {
            AnimatedVisibility(
                visible = !disclosureAccepted,
            ) {
                DisclosureCard(
                    onAcceptClick = {
                        disclosureAccepted = true
                        settingsPreferences.edit().putBoolean(DISCLOSURE_ACCEPTED, true).apply()
                    },
                    onShowClick = onShowDisclosureClick
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = ExportedPlugins.plugins.all { !it.active },
            ) {
                NoPluginsActivatedCard(
                    onGetStartedClick = onGetStartedClick
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = isIgnoringBatteryOptimizations && !optimizationDismissed,
            ) {
                OptimizationCard(
                    startForResult = startForBatteryOptimizationResult,
                    onDismiss = {
                        optimizationDismissed = true
                        settingsPreferences.edit().putBoolean(BATTERY_OPTIMIZATION_DISMISSED, true)
                            .apply()
                    }
                )
            }
        }
    }

    // Animates the celebration on top of the screen
    LottieAnimation(
        composition = celebrateComposition,
        isPlaying = isCelebrating,
        restartOnPlay = true,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun DisclosureCard(
    onAcceptClick: () -> Unit,
    onShowClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Policy,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Disclosure",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                text = "By using this app, you agree to the terms and conditions of the app and the plugins you use.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onShowClick,
                ) {
                    Text(text = "Disclosures")
                }
                Button(
                    onClick = onAcceptClick
                ) {
                    Text(text = "I understand")
                }
            }
        }
    }
}

@Composable
fun NoPluginsActivatedCard(
    onGetStartedClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExtensionOff,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No plugins activated",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                text = "You need to activate at least one plugin to use Dynamic Island.\n" +
                        "Go to the plugins page to activate one.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Get started")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ServiceStatusCard(
    isAccessibilityGranted: Boolean,
    switchAccessibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .height(64.dp)
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.medium)
            .clickable { switchAccessibility() },
        colors = CardDefaults.cardColors(
            containerColor = animateColorAsState(
                targetValue =
                    if (isAccessibilityGranted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ).value
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Add dynamic properties
            val compositionEnabled by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    R.raw.service_enabled
                )
            )
            val compositionDisabled by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    R.raw.service_disabled
                )
            )

            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth(2 / 5f)
                    .fillMaxHeight(),
                targetState = isAccessibilityGranted,
                transitionSpec = {
                    if (isAccessibilityGranted) {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    }
                }
            ) {
                Crossfade(
                    targetState = isAccessibilityGranted,
                ) {
                    if (it) {
                        LottieAnimation(
                            composition = compositionEnabled,
                            iterations = LottieConstants.IterateForever,
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        LottieAnimation(
                            composition = compositionDisabled,
                            iterations = LottieConstants.IterateForever,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            AnimatedContent(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                targetState = isAccessibilityGranted,
                transitionSpec = {
                    if (isAccessibilityGranted) {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut()
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { isGranted ->
                Text(
                    text = if (isGranted) {
                        "ACTIVE"
                    } else {
                        "DISABLED"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun OptimizationCard(
    startForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Battery optimization",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                text = "To prevent the app from being killed by the system, you need to disable battery optimization for Dynamic Island.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(text = "Dismiss")
                }
                Button(
                    onClick = {
                        startForResult.launch(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                ) {
                    Text(text = "Disable battery optimization")
                }
            }
        }
    }
}


fun canDrawOverlays(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

fun isAccessibilityServiceEnabled(service: Class<*>, context: Context): Boolean {
    val expectedComponentName = ComponentName(context, service)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService == expectedComponentName) {
            return true
        }
    }
    return false
}

@Composable
fun PermissionsCard(
    isOverlayGranted: Boolean,
    isAccessibilityGranted: Boolean,
    startForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
    switchAccessibility: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOverlayGranted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsAccessibility,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accessibility permission",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Switch(
                        checked = isAccessibilityGranted,
                        onCheckedChange = { switchAccessibility() },
                    )

                }
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Overlay permission",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Switch(
                    checked = isOverlayGranted,
                    onCheckedChange = {
                        startForResult.launch(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            ), null
                        )
                    }
                )
            }
        }
    }
}