package app.banafsh.android.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import app.banafsh.android.ui.theme.disable

fun DrawScope.drawCircle(
    color: Color,
    shadow: Shadow = Shadow.None,
    radius: Float = size.minDimension / 2.0f,
    center: Offset = this.center,
    alpha: Float = 1.0f,
    style: PaintingStyle = PaintingStyle.Fill,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
) = drawContext.canvas.nativeCanvas.drawCircle(
    // cx =
    center.x,
    // cy =
    center.y,
    // radius =
    radius,
    // paint =
    Paint().also {
        it.color = color
        it.alpha = alpha
        it.blendMode = blendMode
        it.colorFilter = colorFilter
        it.style = style
    }.asFrameworkPaint().also {
        if (shadow != Shadow.None) {
            it.setShadowLayer(
                shadow.blurRadius,
                shadow.offset.x,
                shadow.offset.y,
                shadow.color.toArgb(),
            )
        }
    },
)

@Composable
fun Switch(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = isChecked, label = null)

    val backgroundColor by transition.animateColor(label = "") {
        if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    }

    val color by transition.animateColor(label = "") {
        if (it) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.disable
        }
    }

    val offset by transition.animateDp(label = "") {
        if (it) 36.dp else 12.dp
    }

    Canvas(modifier = modifier.size(width = 48.dp, height = 24.dp)) {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx()),
        )

        drawCircle(
            color = color,
            radius = 8.dp.toPx(),
            center = size.center.copy(x = offset.toPx()),
            shadow =
                Shadow(
                    color = Color.Black.copy(alpha = if (isChecked) 0.4f else 0.1f),
                    blurRadius = 8.dp.toPx(),
                    offset = Offset(x = -1.dp.toPx(), y = 1.dp.toPx()),
                ),
        )
    }
}
