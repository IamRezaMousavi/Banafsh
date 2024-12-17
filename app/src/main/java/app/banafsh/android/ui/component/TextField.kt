package app.banafsh.android.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ColumnScope.TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    singleLine: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = if (singleLine) ImeAction.Done else ImeAction.None,
    ),
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = { },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    hintText: String? = null,
) = BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    textStyle = textStyle.copy(color = colorScheme.onSurface),
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    visualTransformation = visualTransformation,
    onTextLayout = onTextLayout,
    interactionSource = interactionSource,
    cursorBrush = SolidColor(colorScheme.surface),
    decorationBox = { innerTextField ->
        hintText?.let { text ->
            Box(modifier = Modifier.weight(1f)) {
                this@TextField.AnimatedVisibility(
                    visible = value.isEmpty(),
                    enter = fadeIn(tween(100)),
                    exit = fadeOut(tween(100)),
                ) {
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = textStyle,
                        color = colorScheme.onSurface,
                    )
                }
            }
        }

        innerTextField()
    },
)

@Composable
fun RowScope.TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    singleLine: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = if (singleLine) ImeAction.Done else ImeAction.None,
    ),
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = { },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    hintText: String? = null,
) = BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    textStyle = textStyle.copy(color = colorScheme.onSurface),
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    visualTransformation = visualTransformation,
    onTextLayout = onTextLayout,
    interactionSource = interactionSource,
    cursorBrush = SolidColor(colorScheme.surface),
    decorationBox = { innerTextField ->
        hintText?.let { text ->
            Box(modifier = Modifier.weight(1f)) {
                this@TextField.AnimatedVisibility(
                    visible = value.isEmpty(),
                    enter = fadeIn(tween(100)),
                    exit = fadeOut(tween(100)),
                ) {
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = textStyle,
                        color = colorScheme.onSurface,
                    )
                }
            }
        }

        innerTextField()
    },
)
