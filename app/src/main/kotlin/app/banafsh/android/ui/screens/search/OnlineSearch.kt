package app.banafsh.android.ui.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.banafsh.android.LocalPlayerAwareWindowInsets
import app.banafsh.android.R
import app.banafsh.android.data.models.SearchQuery
import app.banafsh.android.db.Database
import app.banafsh.android.db.query
import app.banafsh.android.persist.persist
import app.banafsh.android.persist.persistList
import app.banafsh.android.preferences.DataPreferences
import app.banafsh.android.providers.innertube.Innertube
import app.banafsh.android.providers.innertube.models.bodies.SearchSuggestionsBody
import app.banafsh.android.providers.innertube.requests.searchSuggestions
import app.banafsh.android.ui.components.themed.FloatingActionsContainerWithScrollToTop
import app.banafsh.android.ui.components.themed.Header
import app.banafsh.android.ui.components.themed.SecondaryTextButton
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.utils.align
import app.banafsh.android.utils.center
import app.banafsh.android.utils.disabled
import app.banafsh.android.utils.medium
import app.banafsh.android.utils.secondary
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun OnlineSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    onViewPlaylist: (String) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    var history by persistList<SearchQuery>("search/online/history")

    LaunchedEffect(textFieldValue.text) {
        if (!DataPreferences.pauseSearchHistory) Database.queries("%${textFieldValue.text}%")
            .distinctUntilChanged { old, new -> old.size == new.size }
            .collect { history = it.toImmutableList() }
    }

    var suggestionsResult by persist<Result<List<String>?>?>("search/online/suggestionsResult")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.isEmpty()) return@LaunchedEffect

        delay(200)
        suggestionsResult = Innertube.searchSuggestions(
            body = SearchSuggestionsBody(input = textFieldValue.text)
        )
    }

    val playlistId = remember(textFieldValue.text) {
        val isPlaylistUrl = listOf(
            "https://www.youtube.com/playlist?",
            "https://youtube.com/playlist?",
            "https://music.youtube.com/playlist?",
            "https://m.youtube.com/playlist?"
        ).any(textFieldValue.text::startsWith)

        if (isPlaylistUrl) textFieldValue.text.toUri().getQueryParameter("list")
        else null
    }

    val rippleIndication = ripple(bounded = false)
    val timeIconPainter = painterResource(R.drawable.time)
    val closeIconPainter = painterResource(R.drawable.close)
    val arrowForwardIconPainter = painterResource(R.drawable.arrow_forward)

    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = typography.xxl.medium.align(TextAlign.End),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (textFieldValue.text.isNotEmpty())
                                        onSearch(textFieldValue.text)
                                }
                            ),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox,
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    },
                    actionsContent = {
                        if (playlistId != null) {
                            val isAlbum = playlistId.startsWith("OLAK5uy_")

                            SecondaryTextButton(
                                text = if (isAlbum) stringResource(R.string.view_album)
                                else stringResource(R.string.view_playlist),
                                onClick = { onViewPlaylist(textFieldValue.text) }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (textFieldValue.text.isNotEmpty()) SecondaryTextButton(
                            text = stringResource(R.string.clear),
                            onClick = { onTextFieldValueChange(TextFieldValue()) }
                        )
                    }
                )
            }

            items(
                items = history,
                key = SearchQuery::id
            ) { searchQuery ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = { onSearch(searchQuery.query) })
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                            .paint(
                                painter = timeIconPainter,
                                colorFilter = ColorFilter.disabled
                            )
                    )

                    BasicText(
                        text = searchQuery.query,
                        style = typography.s.secondary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                    )

                    Image(
                        painter = closeIconPainter,
                        contentDescription = null,
                        colorFilter = ColorFilter.disabled,
                        modifier = Modifier
                            .clickable(
                                indication = rippleIndication,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    query {
                                        Database.delete(searchQuery)
                                    }
                                }
                            )
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                    )

                    Image(
                        painter = arrowForwardIconPainter,
                        contentDescription = null,
                        colorFilter = ColorFilter.disabled,
                        modifier = Modifier
                            .clickable(
                                indication = rippleIndication,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onTextFieldValueChange(
                                        TextFieldValue(
                                            text = searchQuery.query,
                                            selection = TextRange(searchQuery.query.length)
                                        )
                                    )
                                }
                            )
                            .rotate(225f)
                            .padding(horizontal = 8.dp)
                            .size(22.dp)
                    )
                }
            }

            suggestionsResult?.getOrNull()?.let { suggestions ->
                items(items = suggestions) { suggestion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = { onSearch(suggestion) })
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp)
                        )

                        BasicText(
                            text = suggestion,
                            style = typography.s.secondary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                        )

                        Image(
                            painter = arrowForwardIconPainter,
                            contentDescription = null,
                            colorFilter = ColorFilter.disabled,
                            modifier = Modifier
                                .clickable(
                                    indication = rippleIndication,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        onTextFieldValueChange(
                                            TextFieldValue(
                                                text = suggestion,
                                                selection = TextRange(suggestion.length)
                                            )
                                        )
                                    }
                                )
                                .rotate(225f)
                                .padding(horizontal = 8.dp)
                                .size(22.dp)
                        )
                    }
                }
            } ?: suggestionsResult?.exceptionOrNull()?.let {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BasicText(
                            text = stringResource(R.string.error_message),
                            style = typography.s.secondary.center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}
