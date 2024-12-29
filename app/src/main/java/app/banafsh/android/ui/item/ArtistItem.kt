package app.banafsh.android.ui.item

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import app.banafsh.android.data.model.Artist
import coil.compose.AsyncImage

@Composable
fun ArtistItem(artist: Artist, modifier: Modifier = Modifier) {
    ItemContainer(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        AsyncImage(
            model = artist.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape),
        )

        ItemInfoContainer(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = artist.name.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
