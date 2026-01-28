package fr.angel.dynamicisland.plugins.media

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import com.skydoves.landscapist.rememberDrawablePainter
import fr.angel.dynamicisland.model.service.NotificationService
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
        if (callbackMap.isEmpty()) {
            host?.requestDismiss(this)
        }
    }

    fun addMedia() {
        host?.requestDisplay(this)
    }

    override fun canExpand(): Boolean {
        return true
    }

    override fun onPluginCreate() {
        val context = host as? Context ?: return

        // Get the media session manager
        mediaSessionManager =
            context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        // Register the listener for active sessions
        mediaSessionManager.addOnActiveSessionsChangedListener(
            listenerForActiveSessions,
            ComponentName(context, NotificationService::class.java)
        )
        mediaSessionManager.getActiveSessions(
            ComponentName(
                context,
                NotificationService::class.java
            )
        ).forEach { controller ->
            // Cancel if already exists
            if (callbackMap[controller.packageName] != null) return@forEach

            val callback = MediaCallback(controller, this)
            callbackMap[controller.packageName] = callback
            controller.registerCallback(callback)
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Composable(
        sharedTransitionScope: SharedTransitionScope,
        animatedContentScope: AnimatedContentScope
    ) {
        val mediaCallback = callbackMap.values.firstOrNull() ?: return
        val context = LocalContext.current

        val controller = mediaCallback.mediaController
        val controls = controller.transportControls

        var isDragging by remember { mutableStateOf(false) }
        var draggedPosition by remember { mutableStateOf(0f) }

        LaunchedEffect(controller.playbackState?.position) {
            elapsed = controller.playbackState?.position ?: 0
            duration = controller.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0

            songPosition = (elapsed / duration.toFloat()) * 100
        }

        with(sharedTransitionScope) {
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
                    Box(
                        modifier = Modifier
                            .sharedBounds(
                                rememberSharedContentState(key = "cover"),
                                animatedVisibilityScope = animatedContentScope
                            )
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mediaCallback.mediaStruct.cover.value != null) {
                            Image(
                                bitmap = mediaCallback.mediaStruct.cover.value!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Crossfade(
                            targetState = mediaCallback.mediaStruct.title.value,
                            label = "title"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Crossfade(
                            targetState = mediaCallback.mediaStruct.artist.value,
                            label = "artist"
                        ) { artist ->
                            Text(
                                text = artist,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    IconButton(
                        onClick = { host?.requestShrink() },
                        modifier = Modifier.align(Alignment.Top)
                    ) {
                        with(sharedTransitionScope) {
                            Box(
                                modifier = Modifier
                                    .sharedBounds(
                                        rememberSharedContentState(key = "media_icon"),
                                        animatedVisibilityScope = animatedContentScope
                                    )
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = context.packageManager.getApplicationIcon(
                                    mediaCallback.mediaController.packageName
                                        ?: "fr.angel.dynamicisland"
                                )
                                Image(
                                    painter = rememberDrawablePainter(drawable = icon),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Slider controlling the position in the song
                Slider(
                    value = if (isDragging) draggedPosition else animateFloatAsState(
                        targetValue = songPosition,
                        label = "songPos"
                    ).value,
                    onValueChange = { value ->
                        draggedPosition = value
                        isDragging = true
                    },
                    onValueChangeFinished = {
                        controls.seekTo(((draggedPosition / 100) * duration).toLong())
                        isDragging = false
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
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous track"
                        )
                    }
                    FilledIconButton(onClick = {
                        if (mediaCallback.mediaStruct.isPlaying()) {
                            controls.pause()
                        } else {
                            controls.play()
                        }
                    }
                    ) {
                        val icon = if (mediaCallback.mediaStruct.isPlaying()) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        }
                        Icon(imageVector = icon, contentDescription = "Play/Pause")
                    }
                    IconButton(onClick = { controls.skipToNext() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next track"
                        )
                    }
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

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun LeftOpenedComposable(
        sharedTransitionScope: SharedTransitionScope,
        animatedContentScope: AnimatedContentScope
    ) {
        val mediaCallback = callbackMap.values.firstOrNull() ?: return


        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "cover"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .size(34.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (mediaCallback.mediaStruct.cover.value != null) {
                    Image(
                        bitmap = mediaCallback.mediaStruct.cover.value!!.asImageBitmap(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun RightOpenedComposable(
        sharedTransitionScope: SharedTransitionScope,
        animatedContentScope: AnimatedContentScope
    ) {
        val context = LocalContext.current
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

        val icon = context.packageManager.getApplicationIcon(
            mediaCallback.mediaController.packageName ?: "fr.angel.dynamicisland"
        )

        with(sharedTransitionScope) {
            WaveLoading(
                progress = animateFloatAsState(targetValue = songPosition / 100).value,
                backDrawType = DrawType.DrawImage,
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = "media_icon"),
                        animatedVisibilityScope = animatedContentScope
                    )
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
}