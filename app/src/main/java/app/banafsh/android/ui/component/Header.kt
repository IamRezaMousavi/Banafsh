package app.banafsh.android.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.banafsh.android.ui.theme.Dimensions

@Composable
fun Header(
    titleContent: @Composable () -> Unit,
    actionsContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    contentAlignment = Alignment.CenterEnd,
    modifier =
    modifier
        .padding(horizontal = 16.dp)
        .height(Dimensions.items.headerHeight)
        .fillMaxWidth(),
) {
    titleContent()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
        Modifier
            .align(Alignment.BottomEnd)
            .heightIn(min = 48.dp),
        content = actionsContent,
    )
}

@Composable
fun Header(title: String, modifier: Modifier = Modifier, actionsContent: @Composable RowScope.() -> Unit = {}) = Header(
    modifier = modifier,
    titleContent = {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    },
    actionsContent = actionsContent,
)
