package app.banafsh.android.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.ui.modifiers.pressable
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.ui.theme.utils.roundedShape

@Composable
fun BigIconButton(
    @DrawableRes iconId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPress: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    backgroundColor: Color = LocalAppearance.current.colorPalette.primaryContainer,
    contentColor: Color = LocalAppearance.current.colorPalette.text,
    shape: Shape = 32.dp.roundedShape
) = Box(
    modifier
        .clip(shape)
        .let {
            if (onPress == null && onCancel == null) it.clickable(onClick = onClick)
            else it.pressable(
                onPress = { onPress?.invoke() },
                onCancel = { onCancel?.invoke() },
                onRelease = onClick
            )
        }
        .background(backgroundColor)
        .height(64.dp),
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(iconId),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        colorFilter = ColorFilter.tint(contentColor)
    )
}
