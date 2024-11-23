package app.banafsh.android.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.banafsh.android.R
import app.banafsh.android.ui.theme.disable
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> ValueSelectorDialog(
    onDismiss: () -> Unit,
    title: String,
    selectedValue: T,
    values: ImmutableList<T>,
    onValueSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    valueText: @Composable (T) -> String = { it.toString() },
) = Dialog(onDismissRequest = onDismiss) {
    Column(
        modifier =
        modifier
            .padding(all = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp),
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            values.forEach { value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier =
                    Modifier
                        .clickable(
                            onClick = {
                                onDismiss()
                                onValueSelect(value)
                            },
                        )
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                        .fillMaxWidth(),
                ) {
                    if (selectedValue == value) {
                        val onCircleColor = MaterialTheme.colorScheme.onPrimary
                        Canvas(
                            modifier =
                            Modifier
                                .size(18.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                ),
                        ) {
                            drawCircle(
                                color = onCircleColor,
                                radius = 4.dp.toPx(),
                                center = size.center,
                                shadow =
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    blurRadius = 4.dp.toPx(),
                                    offset = Offset(x = 0f, y = 1.dp.toPx()),
                                ),
                            )
                        }
                    } else {
                        Spacer(
                            modifier =
                            Modifier
                                .size(18.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.disable,
                                    shape = CircleShape,
                                ),
                        )
                    }

                    Text(
                        text = valueText(value),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }

        Box(
            modifier =
            Modifier
                .align(Alignment.End)
                .padding(end = 24.dp),
        ) {
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
            )
        }
    }
}
