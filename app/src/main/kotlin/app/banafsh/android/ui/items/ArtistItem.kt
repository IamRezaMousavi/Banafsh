package app.banafsh.android.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.banafsh.android.data.models.Artist
import app.banafsh.android.providers.innertube.Innertube
import app.banafsh.android.ui.components.themed.TextPlaceholder
import app.banafsh.android.ui.theme.LocalAppearance
import app.banafsh.android.ui.theme.shimmer
import app.banafsh.android.ui.theme.utils.px
import app.banafsh.android.utils.secondary
import app.banafsh.android.utils.semiBold
import app.banafsh.android.utils.thumbnail
import coil.compose.AsyncImage

@Composable
fun ArtistItem(
    artist: Artist,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = ArtistItem(
    thumbnailUrl = artist.thumbnailUrl,
    name = artist.name,
    subscribersCount = null,
    thumbnailSize = thumbnailSize,
    modifier = modifier,
    alternative = alternative
)

@Composable
fun ArtistItem(
    artist: Innertube.ArtistItem,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = ArtistItem(
    thumbnailUrl = artist.thumbnail?.url,
    name = artist.info?.name,
    subscribersCount = artist.subscribersCountText,
    thumbnailSize = thumbnailSize,
    modifier = modifier,
    alternative = alternative
)

@Composable
fun ArtistItem(
    thumbnailUrl: String?,
    name: String?,
    subscribersCount: String?,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (_, typography) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSize = thumbnailSize,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = thumbnailUrl?.thumbnail(thumbnailSize.px),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .requiredSize(thumbnailSize)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start
        ) {
            BasicText(
                text = name.orEmpty(),
                style = typography.xs.semiBold,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )

            subscribersCount?.let {
                BasicText(
                    text = subscribersCount,
                    style = typography.xxs.semiBold.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistItemPlaceholder(
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (colorPalette) = LocalAppearance.current

    ItemContainer(
        alternative = alternative,
        thumbnailSize = thumbnailSize,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = CircleShape)
                .size(thumbnailSize)
        )

        ItemInfoContainer(
            horizontalAlignment = if (alternative) Alignment.CenterHorizontally else Alignment.Start
        ) {
            TextPlaceholder()
            TextPlaceholder(modifier = Modifier.padding(top = 4.dp))
        }
    }
}
