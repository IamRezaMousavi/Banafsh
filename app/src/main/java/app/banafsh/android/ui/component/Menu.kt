package app.banafsh.android.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
inline fun Menu(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(top = 2.dp)
        .padding(vertical = 8.dp)
        .navigationBarsPadding(),
    content = content,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuEntry(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    modifier = modifier
        .combinedClickable(
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        .fillMaxWidth()
        .alpha(if (enabled) 1f else 0.4f)
        .padding(horizontal = 24.dp),
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.size(15.dp),
    )

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .weight(1f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        secondaryText?.let { secondaryText ->
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    trailingContent?.invoke()
}
