package fr.angel.dynamicisland.ui.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.angel.dynamicisland.island.*
import fr.angel.dynamicisland.model.service.IslandOverlayService
import fr.angel.dynamicisland.ui.theme.DynamicIslandTheme
import fr.angel.dynamicisland.ui.theme.Theme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IslandApp(
	islandOverlayService: IslandOverlayService
) {
	val context = LocalContext.current
	Theme.instance.Init()
	LaunchedEffect(Unit) {
		IslandSettings.instance.loadSettings(context = context)
	}

	val islandView = islandOverlayService.islandState
	val bindedPlugin = islandOverlayService.bindedPlugins.firstOrNull()

	val height by animateDpAsState(
		targetValue = islandView.height,
		animationSpec =
		spring(
			dampingRatio = DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		)
	)
	val width by animateDpAsState(
		targetValue = islandView.width,
		animationSpec = spring(
			dampingRatio = DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		)
	)
	val cornerPercentage by animateFloatAsState(targetValue = islandView.cornerPercentage)

	AnimatedVisibility(
		visible = (Island.isScreenOn
				|| IslandSettings.instance.showOnLockScreen)
				&& (!Island.isInLandscape || IslandSettings.instance.showInLandscape),
		modifier = Modifier
			.fillMaxWidth()
			//.background(Color.Red)
	) {
		DynamicIslandTheme(
			darkTheme = if (islandOverlayService.invertedTheme) !Theme.instance.isDarkTheme else Theme.instance.isDarkTheme,
			style = Theme.instance.themeStyle
		) {
			Box(
				modifier = Modifier
					.padding(top = islandView.yPosition)
					.height(height)
					/*.wrapContentHeight()
					.height(IntrinsicSize.Min)
					.defaultMinSize(minHeight = height)*/
					.width(width + 16.dp)
					.fillMaxWidth()
					.offset(x = islandView.xPosition)
					.clip(RoundedCornerShape(cornerPercentage)),
				contentAlignment = when	(IslandSettings.instance.gravity) {
					IslandGravity.Center -> Alignment.TopCenter
					IslandGravity.Left -> Alignment.TopStart
					IslandGravity.Right -> Alignment.TopEnd
				}
			) {
				val clickModifier =
					if (islandView is IslandViewState.Opened || islandView is IslandViewState.Expanded) {
						Modifier
							.clip(RoundedCornerShape(cornerPercentage))
							.combinedClickable(
								onClick = { bindedPlugin?.onClick() },
								onLongClick = {
									if (bindedPlugin?.canExpand() == true) {
										islandOverlayService.expand()
									}
								}
							)
					} else {
						Modifier
					}

				val borderModifier = if (IslandSettings.instance.showBorders) {
					Modifier
						.border(
							width = 1.dp,
							color = MaterialTheme.colorScheme.primary,
							shape = RoundedCornerShape(cornerPercentage)
						)
				} else {
					Modifier
				}

				Card(
					shape = RoundedCornerShape(cornerPercentage),
					modifier = Modifier
						.then(clickModifier)
						.then(borderModifier)
						.width(width)
						.height(height)
						/*.wrapContentHeight()
					.height(IntrinsicSize.Min)*/
						.defaultMinSize(minHeight = height),
					colors = CardDefaults.cardColors(
						containerColor = MaterialTheme.colorScheme.surface,
					)
				) {
					Crossfade(
						targetState = islandOverlayService.islandState.state,
						animationSpec = tween(100)
					) {
						when (it) {
							IslandStates.Opened -> {
								val boxModifier = Modifier
									.fillMaxHeight()

								Row(
									modifier = Modifier
										.fillMaxSize()
										.padding(4.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {

									// Left side
									Box(
										modifier = boxModifier,
										contentAlignment = Alignment.CenterEnd
									) {
										Crossfade(
											targetState = bindedPlugin,
										) { plugin -> plugin?.LeftOpenedComposable() }
									}

									// Right side
									Box(
										modifier = boxModifier,
										contentAlignment = Alignment.CenterStart
									) {
										Crossfade(
											targetState = bindedPlugin,
										) { plugin -> plugin?.RightOpenedComposable() }
									}
								}
							}
							IslandStates.Expanded -> {
								Crossfade(
									targetState = bindedPlugin,
								) { plugin -> plugin?.Composable() }
							}
							else -> {}
						}
					}
				}
			}
		}
	}
}