package app.banafsh.android.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.banafsh.android.ui.theme.Dimensions

@Composable
inline fun ItemContainer(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (centeredModifier: Modifier) -> Unit,
) = Column(
    horizontalAlignment = horizontalAlignment,
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = modifier
        .padding(
            vertical = Dimensions.items.verticalPadding,
            horizontal = Dimensions.items.horizontalPadding,
        )
        .fillMaxWidth(),
) { content(Modifier.align(Alignment.CenterHorizontally)) }

@Composable
inline fun ItemInfoContainer(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) = Column(
    horizontalAlignment = horizontalAlignment,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier,
    content = content,
)
