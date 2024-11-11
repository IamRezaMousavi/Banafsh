package app.banafsh.android.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.ui.theme.Dimensions

@Composable
inline fun NavigationRail(
    topIconButtonId: Int,
    noinline onTopIconButtonClick: () -> Unit,
    tabIndex: Int,
    crossinline onTabIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
) {
    val paddingValues =
        LocalPlayerAwareWindowInsets.current
            .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
            .asPaddingValues()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .fillMaxHeight(),
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier =
                Modifier
                    .size(
                        width = Dimensions.navigationRail.width,
                        height = Dimensions.items.headerHeight,
                    ),
        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                modifier =
                    Modifier
                        .offset(
                            x = Dimensions.navigationRail.iconOffset,
                            y = 48.dp,
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onTopIconButtonClick)
                        .padding(all = 12.dp)
                        .size(22.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            val transition = updateTransition(targetState = tabIndex, label = null)

            content { index, text, icon ->
                val dothAlpha by transition.animateFloat(label = "") {
                    if (it == index) 1f else 0f
                }

                val textColor by transition.animateColor(label = "") {
                    if (it == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f,
                        )
                    }
                }

                val contentModifier =
                    Modifier
                        .clickable(onClick = { onTabIndexChange(index) })

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = contentModifier.padding(horizontal = 8.dp),
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier
                                .vertical(true)
                                .graphicsLayer {
                                    alpha = dothAlpha
                                    translationX = (1f - dothAlpha) * -48.dp.toPx()
                                    rotationZ = -90f
                                }
                                .size(6.dp * 2),
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier =
                            Modifier
                                .vertical(true)
                                .rotate(-90f)
                                .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

fun Modifier.vertical(enabled: Boolean = true) =
    if (enabled) {
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
            layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2),
                )
            }
        }
    } else {
        this
    }
