package app.banafsh.android.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.banafsh.android.data.model.Album
import app.banafsh.android.ui.theme.Dimensions
import coil.compose.AsyncImage

@Composable
fun AlbumItem(album: Album, modifier: Modifier = Modifier) = ItemContainer(modifier = modifier) {
    AsyncImage(
        model = album.thumbnailUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .size(Dimensions.thumbnails.album),
    )

    ItemInfoContainer {
        Text(
            text = album.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        album.authors?.let {
            Text(
                text = album.authors,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = album.year.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
