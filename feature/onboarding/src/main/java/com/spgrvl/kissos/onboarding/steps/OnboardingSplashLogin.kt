package com.spgrvl.kissos.onboarding.steps

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spgrvl.kissos.design.l0_system.UI
import com.spgrvl.kissos.design.l0_system.style
import com.spgrvl.kissos.legacy.Constants
import com.spgrvl.kissos.legacy.KissosWalletCtx
import com.spgrvl.kissos.legacy.KissosWalletPreview
import com.spgrvl.kissos.legacy.kissosWalletCtx
import com.spgrvl.kissos.legacy.utils.clickableNoIndication
import com.spgrvl.kissos.legacy.utils.drawColoredShadow
import com.spgrvl.kissos.legacy.utils.lerp
import com.spgrvl.kissos.legacy.utils.openUrl
import com.spgrvl.kissos.legacy.utils.springBounceSlow
import com.spgrvl.kissos.design.utils.thenIf
import com.spgrvl.kissos.legacy.utils.rememberInteractionSource
import com.spgrvl.kissos.legacy.utils.toDensityDp
import com.spgrvl.kissos.legacy.utils.toDensityPx
import com.spgrvl.kissos.onboarding.OnboardingState
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.Gradient
import com.spgrvl.kissos.wallet.ui.theme.Gray
import com.spgrvl.kissos.wallet.ui.theme.Green
import com.spgrvl.kissos.wallet.ui.theme.components.KissosIcon
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.OnboardingSplashLogin(
    onboardingState: OnboardingState,
    onSkip: () -> Unit,
) {
    var internalSwitch by remember { mutableStateOf(true) }

    val transition = updateTransition(
        targetState = if (!internalSwitch) OnboardingState.LOGIN else onboardingState,
        label = "Splash"
    )

    val logoWidth by transition.animateDp(
        transitionSpec = {
            springBounceSlow()
        },
        label = "logoWidth"
    ) {
        when (it) {
            OnboardingState.SPLASH -> 113.dp
            else -> 76.dp
        }
    }

    val logoHeight by transition.animateDp(
        transitionSpec = {
            springBounceSlow()
        },
        label = "logoHeight"
    ) {
        when (it) {
            OnboardingState.SPLASH -> 96.dp
            else -> 64.dp
        }
    }

    val kissosContext = kissosWalletCtx()

    val spacerTop by transition.animateDp(
        transitionSpec = {
            springBounceSlow()
        },
        label = "spacerTop"
    ) {
        when (it) {
            OnboardingState.SPLASH -> {
                (kissosContext.screenHeight / 2f - logoHeight.toDensityPx() / 2f).toDensityDp()
            }

            else -> 56.dp
        }
    }

    val percentTransition by transition.animateFloat(
        transitionSpec = {
            springBounceSlow()
        },
        label = "percentTransition"
    ) {
        when (it) {
            OnboardingState.SPLASH -> 0f
            else -> 1f
        }
    }

    val marginTextTop by transition.animateDp(
        transitionSpec = {
            springBounceSlow()
        },
        label = "marginTextTop"
    ) {
        when (it) {
            OnboardingState.SPLASH -> 64.dp
            else -> 40.dp
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UI.colors.pure)
            .systemBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(spacerTop))

        Image(
            modifier = Modifier
                .size(
                    width = logoWidth,
                    height = logoHeight
                )
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    val xSplash = kissosContext.screenWidth / 2f - placeable.width / 2
                    val xLogin = 24.dp.toPx()

                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(
                            x = lerp(xSplash, xLogin, percentTransition).roundToInt(),
                            y = 0,
                        )
                    }
                }
                .clickableNoIndication(rememberInteractionSource()) {
                    internalSwitch = !internalSwitch
                },
            painter = painterResource(id = R.drawable.kissos_logo),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Kissos logo"
        )

        Spacer(Modifier.height(marginTextTop))

        Text(
            modifier = Modifier.animateXCenterToLeft(
                kissosContext = kissosContext,
                percentTransition = percentTransition
            ),
            text = "Kissos",
            style = UI.typo.h2.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.animateXCenterToLeft(
                kissosContext = kissosContext,
                percentTransition = percentTransition
            ),
            text = stringResource(R.string.your_personal_money_manager),
            style = UI.typo.b2.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.SemiBold
            )
        )

        val uriHandler = LocalUriHandler.current
        Text(
            modifier = Modifier
                .animateXCenterToLeft(
                    kissosContext = kissosContext,
                    percentTransition = percentTransition
                )
                .clickable {
                    openUrl(
                        uriHandler = uriHandler,
                        url = Constants.URL_KISSOS_REPO
                    )
                }
                .padding(vertical = 8.dp)
                .padding(end = 8.dp),
            text = stringResource(R.string.opensource),
            style = UI.typo.c.style(
                color = Green,
                fontWeight = FontWeight.Bold
            )
        )

        LoginSection(
            percentTransition = percentTransition,
            onSkip = onSkip
        )
    }
}

private fun Modifier.animateXCenterToLeft(
    kissosContext: KissosWalletCtx,
    percentTransition: Float
): Modifier {
    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            val xSplash = kissosContext.screenWidth / 2f - placeable.width / 2
            val xLogin = 32.dp.toPx()

            placeable.placeRelative(
                x = lerp(xSplash, xLogin, percentTransition).roundToInt(),
                y = 0
            )
        }
    }
}

@Composable
private fun LoginSection(
    percentTransition: Float,
    onSkip: () -> Unit
) {
    if (percentTransition > 0.01f) {
        Column(
            modifier = Modifier
                .alpha(percentTransition),
        ) {
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.weight(1f))

            LocalAccountExplanation()

            Spacer(Modifier.height(16.dp))

            LoginButton(
                icon = R.drawable.ic_local_account,
                text = stringResource(R.string.offline_account),
                textColor = UI.colors.pureInverse,
                backgroundGradient = Gradient.solid(UI.colors.medium),
                hasShadow = false
            ) {
                onSkip()
            }

            Spacer(Modifier.weight(3f))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocalAccountExplanation() {
    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.enter_with_offline_account),
        style = UI.typo.c.style(
            color = Gray,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(4.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp),
        text = stringResource(R.string.your_data_will_be_saved_only_locally),
        style = UI.typo.c.style(
            color = Gray,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun LoginButton(
    @DrawableRes icon: Int,
    text: String,
    textColor: Color,
    backgroundGradient: Gradient,
    hasShadow: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .thenIf(hasShadow) {
                drawColoredShadow(backgroundGradient.startColor)
            }
            .clip(UI.shapes.r4)
            .background(backgroundGradient.asHorizontalBrush(), UI.shapes.r4)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        KissosIcon(
            icon = icon,
            tint = textColor
        )

        Spacer(Modifier.width(16.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = text,
            style = UI.typo.b2.style(
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.width(20.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    KissosWalletPreview {
        OnboardingSplashLogin(
            onboardingState = OnboardingState.SPLASH,
            onSkip = {}
        )
    }
}
