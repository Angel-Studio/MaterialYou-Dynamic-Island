package fr.angel.dynamicisland.ui.disclosure

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants.IterateForever
import com.airbnb.lottie.compose.rememberLottieComposition
import fr.angel.dynamicisland.R
import fr.angel.dynamicisland.ui.animation.WavesLoadingIndicator

@Composable
fun DisclosureScreen(
	modifier: Modifier = Modifier,
	screens: ArrayList<Disclosure> = arrayListOf(
		Disclosure(
			title = "Welcome to Dynamic Island",
			description = "Dynamic Island is a new way to discover the world around you. " +
				"By using augmented reality, you can discover new places, new people and new " +
				"things. You can also create your own island and share it with your friends.",
			illustration = R.raw.privacy,
		),
	),
	step: Int = 0,
	onNext: () -> Unit,
	onPrevious: () -> Unit,
	onStart: () -> Unit,
) {
	val maxStep = screens.size - 1

	Box(modifier = modifier.fillMaxSize()
	) {
		WavesLoadingIndicator(
			modifier = Modifier
				.fillMaxSize()
				.alpha(0.4f),
			color = MaterialTheme.colorScheme.primaryContainer,
			progress = animateFloatAsState(
				targetValue = step.toFloat() / maxStep.toFloat(),
				animationSpec = tween(
					durationMillis = 300,
					easing = EaseInOutCubic
				)
			).value
		)

		ConstraintLayout(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			val (nextButton, previousButton, content) = createRefs()
			Button(
				onClick = {
					if (step < maxStep) {
						onNext()
					} else {
						onStart()
					}
				},
				modifier = Modifier
					.animateContentSize()
					.constrainAs(nextButton) {
						bottom.linkTo(parent.bottom)
						end.linkTo(parent.end)
					}
			) {
				Text(if (step == maxStep) "I agree" else "Next")
			}

			TextButton(
				onClick = {
					onPrevious()
				},
				modifier = Modifier
					.constrainAs(previousButton) {
						bottom.linkTo(parent.bottom)
						start.linkTo(parent.start)
					},
				enabled = step > 0
			) {
				Text("Previous")
			}

			Box(
				modifier = Modifier
					.constrainAs(content) {
						top.linkTo(parent.top)
						bottom.linkTo(nextButton.top, 16.dp)
						start.linkTo(parent.start)
						end.linkTo(parent.end)
					}
			) {
				Crossfade(targetState = step) {
					DisclosurePage(
						disclosure = screens[it]
					)
				}
			}
		}
	}
}

@Composable
fun DisclosurePage(
	modifier: Modifier = Modifier,
	disclosure: Disclosure,
) {
	val uriHandler = LocalUriHandler.current

	ConstraintLayout(
		modifier = modifier
			.fillMaxSize()
	) {
		val (description, illustration) = createRefs()

		val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(disclosure.illustration))
		LottieAnimation(
			composition = composition,
			modifier = Modifier
				.constrainAs(illustration) {
					top.linkTo(parent.top, 16.dp)
					bottom.linkTo(description.top, 16.dp)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					height = Dimension.percent(0.2f)
					width = Dimension.fillToConstraints
				},
			iterations = IterateForever,
			contentScale = if (disclosure.cropIllustration) ContentScale.Crop else ContentScale.Fit,
		)
		Column(
			modifier = Modifier.constrainAs(description) {
				linkTo(
					top = illustration.bottom,
					bottom = parent.bottom,
					topMargin = 16.dp,
					bottomMargin = 96.dp,
					bias = 1f
				)
				start.linkTo(parent.start, 16.dp)
				end.linkTo(parent.end, 16.dp)
				width = Dimension.fillToConstraints
			},
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = disclosure.description,

				textAlign = TextAlign.Center,
				style = MaterialTheme.typography.bodyMedium
			)
			if (disclosure.link != null) {
				Spacer(modifier = Modifier.height(16.dp))
				TextButton(
					onClick = {
						uriHandler.openUri(disclosure.link.url)
					}
				) {
					Text(disclosure.link.text)
				}
			}
		}
	}
}

data class Disclosure(
	val title: String,
	val description: String,
	val illustration: Int,
	val cropIllustration: Boolean = true,
	val link: Link? = null,
)

data class Link(
	val text: String,
	val url: String,
)