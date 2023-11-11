package fr.angel.dynamicisland.plugins.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import com.skydoves.landscapist.rememberDrawablePainter
import fr.angel.dynamicisland.model.service.NotificationService
import fr.angel.dynamicisland.model.service.IslandOverlayService
import fr.angel.dynamicisland.plugins.BasePlugin
import fr.angel.dynamicisland.plugins.PluginSettingsItem

class MediaSessionPlugin(
	override val id: String = "MediaSessionPlugin",
	override val name: String = "MediaSession",
	override val description: String = "Show the current media session playing",
	override val permissions: ArrayList<String> = arrayListOf(
		Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
	),
	override var enabled: MutableState<Boolean> = mutableStateOf(false),
	override var pluginSettings: MutableMap<String, PluginSettingsItem> = mutableMapOf(),
) : BasePlugin() {

	lateinit var context: IslandOverlayService
	private lateinit var mediaSessionManager: MediaSessionManager

	private var callbackMap = mutableStateMapOf<String, MediaCallback>()

	// private var mediaStruct by mutableStateOf<MediaStruct?>(null)
	private var songPosition by mutableStateOf(0f)
	private var duration: Long by mutableStateOf(0)
	private var elapsed: Long by mutableStateOf(0)

	private val listenerForActiveSessions =
		OnActiveSessionsChangedListener { controllers ->
			if (controllers != null) {
				for (controller in controllers) {
					// Cancel if already exists
					if (callbackMap[controller.packageName] != null) return@OnActiveSessionsChangedListener

					// Create callback for this controller and add it to the map of callbacks
					val callback = MediaCallback(controller, this)
					callbackMap[controller.packageName] = callback
					controller.registerCallback(callback)
				}
			}
		}

	fun removeMedia(mediaController: MediaController) {
		callbackMap.remove(mediaController.packageName)
		if (callbackMap.isEmpty()) { context.removePlugin(this) }
	}

	override fun canExpand(): Boolean { return true }

	override fun onCreate(context: IslandOverlayService?) {
		this.context = context ?: return

		// Get the media session manager
		mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

		// Register the listener for active sessions
		mediaSessionManager.addOnActiveSessionsChangedListener(listenerForActiveSessions, ComponentName(context, NotificationService::class.java))
		mediaSessionManager.getActiveSessions(ComponentName(context, NotificationService::class.java)).forEach { controller ->
			// Cancel if already exists
			if (callbackMap[controller.packageName] != null) return@forEach

			val callback = MediaCallback(controller, this)
			callbackMap[controller.packageName] = callback
			controller.registerCallback(callback)
		}
	}

	@Composable
	override fun Composable() {
		val mediaCallback = callbackMap.values.firstOrNull() ?: return

		val controller = mediaCallback.mediaController
		val controls = controller.transportControls

		var isDragging by remember { mutableStateOf(false) }
		var draggedPosition by remember { mutableStateOf(0f) }
		var draggedOffset by remember { mutableStateOf(0f) }

		LaunchedEffect(controller.playbackState?.position) {
			elapsed = controller.playbackState?.position ?: 0
			duration = controller.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0

			songPosition = (elapsed / duration.toFloat()) * 100
		}

		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			// Cover + title + artist
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Crossfade(targetState = mediaCallback.mediaStruct.cover.value) { cover ->
					if (cover != null) {
						Image(
							bitmap = cover.asImageBitmap(),
							contentDescription = null,
							modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.size(64.dp)
						)
					} else {
						Icon(
							imageVector = Icons.Default.MusicNote,
							contentDescription = null,
							modifier = Modifier
								.size(128.dp)
								.clip(RoundedCornerShape(8.dp))
								.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
						)
					}
				}

				Spacer(modifier = Modifier.width(16.dp))
				Column(modifier = Modifier.weight(1f)) {
					Crossfade(targetState = mediaCallback.mediaStruct.title.value) { title ->
						Text(
							text = title,
							style = MaterialTheme.typography.titleMedium
						)
					}
					Crossfade(targetState = mediaCallback.mediaStruct.artist.value) { artist ->
						Text(
							text = artist,
							style = MaterialTheme.typography.labelMedium
						)
					}
				}
				IconButton(
					onClick = { context.shrink() },
					modifier = Modifier.align(Alignment.Top)
				) { Icon(imageVector = Icons.Default.ExpandLess, contentDescription = null) }
			}
			Spacer(modifier = Modifier.height(8.dp))

			// Slider controlling the position in the song
			Slider(
				value = if (isDragging) draggedPosition else animateFloatAsState(targetValue = songPosition).value,
				onValueChange = { value ->
					draggedPosition = value
					isDragging = true
				},
				onValueChangeFinished = {
					controls.seekTo(((draggedPosition / 100) * duration).toLong())
					isDragging = false
					draggedOffset = songPosition - draggedPosition
				},
				valueRange = 0f..100f,
			)

			// Controls
			Spacer(modifier = Modifier.height(8.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center.apply { Arrangement.spacedBy(16.dp) },
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = { controls.skipToPrevious() }) {
					Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous track")
				}
				FilledIconButton(onClick = { if (mediaCallback.mediaStruct.isPlaying()) { controls.pause() } else { controls.play() } }
				) {
					val icon = if (mediaCallback.mediaStruct.isPlaying()) { Icons.Default.Pause } else { Icons.Default.PlayArrow }
					Icon(imageVector = icon, contentDescription = "Play/Pause")
				}
				IconButton(onClick = { controls.skipToNext() }) {
					Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next track")
				}
			}
		}
	}

	override fun onClick() {
		val current = callbackMap.values.firstOrNull() ?: return

		val controller = current.mediaController

		Log.d("MediaSessionPlugin", "onClick: ${controller.sessionActivity}")
		if (controller.sessionActivity != null) {
			controller.sessionActivity!!.send(0)
		}
	}

	override fun onDestroy() {
		if (!::mediaSessionManager.isInitialized) return
		// Unregister the listener for active sessions
		mediaSessionManager.removeOnActiveSessionsChangedListener(listenerForActiveSessions)
	}

	@Composable
	override fun PermissionsRequired() {

	}

	override fun onLeftSwipe() {}
	override fun onRightSwipe() {}

	@Composable
	override fun LeftOpenedComposable() {
		val mediaCallback = callbackMap.values.firstOrNull() ?: return

		if (mediaCallback.mediaStruct.cover.value != null) {
			Crossfade(targetState = mediaCallback.mediaStruct.cover.value!!) {
				Image(
					bitmap = it.asImageBitmap(),
					contentDescription = "Pause",
					modifier = Modifier.clip(CircleShape)
				)
			}
		} else {
			Icon(
				imageVector = Icons.Default.MusicNote,
				contentDescription = null,
			)
		}
	}

	@Composable
	override fun RightOpenedComposable() {
		val mediaCallback = callbackMap.values.firstOrNull()
		if (mediaCallback == null) {
			Log.d("MediaSessionPlugin", "RightOpenedComposable: No media callback")
			return
		}

		LaunchedEffect(mediaCallback.mediaStruct.playbackState.value.position) {
			elapsed = mediaCallback.mediaStruct.playbackState.value.position
			duration = mediaCallback.mediaStruct.duration.value

			songPosition = (elapsed / duration.toFloat()) * 100
		}

		val icon = context.packageManager.getApplicationIcon(mediaCallback.mediaController.packageName ?: "fr.angel.dynamicisland")

		WaveLoading(
			progress = animateFloatAsState(targetValue = songPosition / 100).value,
			backDrawType = DrawType.DrawImage,
			modifier = Modifier
				.fillMaxHeight()
				.clip(CircleShape)
				.aspectRatio(1f)
				.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
		) {
			Image(
				painter = rememberDrawablePainter(drawable = icon),
				contentDescription = null
			)
		}
	}
}