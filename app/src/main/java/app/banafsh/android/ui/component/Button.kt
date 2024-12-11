package app.banafsh.android.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.banafsh.android.ui.theme.disable

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) = Text(
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    modifier =
    modifier
        .clip(MaterialTheme.shapes.extraLarge)
        .background(if (primary) MaterialTheme.colorScheme.primary else Color.Transparent)
        .clickable(enabled = enabled, onClick = onClick)
        .padding(all = 8.dp)
        .padding(horizontal = 8.dp),
)

@Composable
fun IconButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indication: Indication? = ripple(bounded = false),
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier =
        Modifier
            .clickable(
                indication = indication,
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick,
            )
            .then(modifier),
    )
}

@Composable
fun IconButton(onClick: () -> Unit, @DrawableRes icon: Int, modifier: Modifier = Modifier, enabled: Boolean = true) =
    IconButton(
        onClick = onClick,
        icon = icon,
        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.disable,
        modifier = modifier,
        enabled = true,
    )

@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indication: Indication? = ripple(bounded = false),
) {
    val color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.disable

    IconButton(
        onClick = onClick,
        icon = icon,
        indication = indication,
        enabled = true,
        color = color,
        modifier =
        modifier
            .padding(all = 4.dp)
            .size(18.dp),
    )
}
