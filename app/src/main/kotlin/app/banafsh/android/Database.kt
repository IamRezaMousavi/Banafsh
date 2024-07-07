package app.banafsh.android

import android.database.SQLException
import androidx.media3.common.MediaItem
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import app.banafsh.android.lib.core.data.enums.AlbumSortBy
import app.banafsh.android.lib.core.data.enums.ArtistSortBy
import app.banafsh.android.lib.core.data.enums.PlaylistSortBy
import app.banafsh.android.lib.core.data.enums.SongSortBy
import app.banafsh.android.lib.core.data.enums.SortOrder
import app.banafsh.android.models.Album
import app.banafsh.android.models.Artist
import app.banafsh.android.models.Event
import app.banafsh.android.models.EventWithSong
import app.banafsh.android.models.Format
import app.banafsh.android.models.Info
import app.banafsh.android.models.Lyrics
import app.banafsh.android.models.PipedSession
import app.banafsh.android.models.Playlist
import app.banafsh.android.models.PlaylistPreview
import app.banafsh.android.models.PlaylistWithSongs
import app.banafsh.android.models.QueuedSong
import app.banafsh.android.models.SearchQuery
import app.banafsh.android.models.Song
import app.banafsh.android.models.SongAlbumMap
import app.banafsh.android.models.SongArtistMap
import app.banafsh.android.models.SongPlaylistMap
import app.banafsh.android.models.SongWithContentLength
import app.banafsh.android.models.SortedSongPlaylistMap
import app.banafsh.android.service.LOCAL_KEY_PREFIX
import app.banafsh.android.utils.songBundle
import app.banafsh.android.utils.toSong
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface Database {
    companion object : Database by DatabaseInitializer.instance.database

    @Transaction
    @Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY ROWID ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY title ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY title DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleDesc(): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%'
        ORDER BY totalPlayTimeMs ASC
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeAsc(): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%'
        ORDER BY totalPlayTimeMs DESC
        LIMIT :limit
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeDesc(limit: Int = -1): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id IN (:songIds)")
    @RewriteQueriesToDropUnusedColumns
    fun songs(songIds: List<String>): List<Song>

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder) = when (sortBy) {
        SongSortBy.PlayTime -> when (sortOrder) {
            SortOrder.Ascending -> songsByPlayTimeAsc()
            SortOrder.Descending -> songsByPlayTimeDesc()
        }

        SongSortBy.Title -> when (sortOrder) {
            SortOrder.Ascending -> songsByTitleAsc()
            SortOrder.Descending -> songsByTitleDesc()
        }

        SongSortBy.DateAdded -> when (sortOrder) {
            SortOrder.Ascending -> songsByRowIdAsc()
            SortOrder.Descending -> songsByRowIdDesc()
        }
    }

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun favorites(): Flow<List<Song>>

    @Query("SELECT * FROM QueuedSong")
    fun queue(): List<QueuedSong>

    @Query("DELETE FROM QueuedSong")
    fun clearQueue()

    @Query("SELECT * FROM SearchQuery WHERE `query` LIKE :query ORDER BY id DESC")
    fun queries(query: String): Flow<List<SearchQuery>>

    @Query("SELECT COUNT (*) FROM SearchQuery")
    fun queriesCount(): Flow<Int>

    @Query("DELETE FROM SearchQuery")
    fun clearQueries()

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String): Flow<Song?>

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String): Flow<Long?>

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun like(songId: String, likedAt: Long?): Int

    @Query("UPDATE Song SET durationText = :durationText WHERE id = :songId")
    fun updateDurationText(songId: String, durationText: String): Int

    @Query("SELECT * FROM Lyrics WHERE songId = :songId")
    fun lyrics(songId: String): Flow<Lyrics?>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Query("SELECT * FROM Artist ORDER BY name DESC")
    fun artistsByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY name ASC")
    fun artistsByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY ROWID DESC")
    fun artistsByRowIdDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY ROWID ASC")
    fun artistsByRowIdAsc(): Flow<List<Artist>>

    fun artists(sortBy: ArtistSortBy, sortOrder: SortOrder) = when (sortBy) {
        ArtistSortBy.Name -> when (sortOrder) {
            SortOrder.Ascending -> artistsByNameAsc()
            SortOrder.Descending -> artistsByNameDesc()
        }

        ArtistSortBy.DateAdded -> when (sortOrder) {
            SortOrder.Ascending -> artistsByRowIdAsc()
            SortOrder.Descending -> artistsByRowIdDesc()
        }
    }

    @Query("SELECT * FROM Album WHERE id = :id")
    fun album(id: String): Flow<Album?>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId
        WHERE SongAlbumMap.albumId = :albumId
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Query("SELECT * FROM Album ORDER BY title ASC")
    fun albumsByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY year ASC")
    fun albumsByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY ROWID ASC")
    fun albumsByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY title DESC")
    fun albumsByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY year DESC")
    fun albumsByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY ROWID DESC")
    fun albumsByRowIdDesc(): Flow<List<Album>>

    fun albums(sortBy: AlbumSortBy, sortOrder: SortOrder) = when (sortBy) {
        AlbumSortBy.Title -> when (sortOrder) {
            SortOrder.Ascending -> albumsByTitleAsc()
            SortOrder.Descending -> albumsByTitleDesc()
        }

        AlbumSortBy.Year -> when (sortOrder) {
            SortOrder.Ascending -> albumsByYearAsc()
            SortOrder.Descending -> albumsByYearDesc()
        }

        AlbumSortBy.DateAdded -> when (sortOrder) {
            SortOrder.Ascending -> albumsByRowIdAsc()
            SortOrder.Descending -> albumsByRowIdDesc()
        }
    }

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Query("SELECT * FROM PipedSession")
    fun pipedSessions(): Flow<List<PipedSession>>

    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlist(id: Long): Flow<Playlist?>

    // TODO: apparently this is an edge-case now?
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM SortedSongPlaylistMap
        INNER JOIN Song on Song.id = SortedSongPlaylistMap.songId
        WHERE playlistId = :id
        ORDER BY SortedSongPlaylistMap.position
        """
    )
    fun playlistSongs(id: Long): Flow<List<Song>?>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist 
        ORDER BY name ASC
        """
    )
    fun playlistPreviewsByNameAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist
        ORDER BY ROWID ASC
        """
    )
    fun playlistPreviewsByDateAddedAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist
        ORDER BY songCount ASC
        """
    )
    fun playlistPreviewsByDateSongCountAsc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist
        ORDER BY name DESC
        """
    )
    fun playlistPreviewsByNameDesc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist
        ORDER BY ROWID DESC
        """
    )
    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>>

    @Transaction
    @Query(
        """
        SELECT id, name, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, thumbnail FROM Playlist
        ORDER BY songCount DESC
        """
    )
    fun playlistPreviewsByDateSongCountDesc(): Flow<List<PlaylistPreview>>

    fun playlistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ) = when (sortBy) {
        PlaylistSortBy.Name -> when (sortOrder) {
            SortOrder.Ascending -> playlistPreviewsByNameAsc()
            SortOrder.Descending -> playlistPreviewsByNameDesc()
        }

        PlaylistSortBy.SongCount -> when (sortOrder) {
            SortOrder.Ascending -> playlistPreviewsByDateSongCountAsc()
            SortOrder.Descending -> playlistPreviewsByDateSongCountDesc()
        }

        PlaylistSortBy.DateAdded -> when (sortOrder) {
            SortOrder.Ascending -> playlistPreviewsByDateAddedAsc()
            SortOrder.Descending -> playlistPreviewsByDateAddedDesc()
        }
    }

    @Query(
        """
        SELECT thumbnailUrl FROM Song
        JOIN SongPlaylistMap ON id = songId
        WHERE playlistId = :id
        ORDER BY position
        LIMIT 4
        """
    )
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        JOIN SongArtistMap ON Song.id = SongArtistMap.songId
        WHERE SongArtistMap.artistId = :artistId AND
        totalPlayTimeMs > 0
        ORDER BY Song.ROWID DESC
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<Song>>

    @Query("SELECT * FROM Format WHERE songId = :songId")
    fun format(songId: String): Flow<Format?>

    @Transaction
    @Query(
        """
        SELECT Song.*, contentLength FROM Song
        JOIN Format ON id = songId
        WHERE contentLength IS NOT NULL
        ORDER BY Song.ROWID DESC
        """
    )
    fun songsWithContentLength(): Flow<List<SongWithContentLength>>

    @Query("SELECT id FROM Song WHERE blacklisted")
    suspend fun blacklistedIds(): List<String>

    @Query("SELECT blacklisted FROM Song WHERE id = :songId")
    fun blacklisted(songId: String): Flow<Boolean>

    @Query("SELECT COUNT (*) FROM Song where blacklisted")
    fun blacklistLength(): Flow<Int>

    @Transaction
    @Query("UPDATE Song SET blacklisted = NOT blacklisted WHERE blacklisted")
    fun resetBlacklist()

    @Transaction
    @Query("UPDATE Song SET blacklisted = NOT blacklisted WHERE id = :songId")
    fun toggleBlacklist(songId: String)

    suspend fun filterBlacklistedSongs(songs: List<MediaItem>): List<MediaItem> {
        val blacklistedIds = blacklistedIds()
        return songs.filter { it.mediaId !in blacklistedIds }
    }

    @Transaction
    @Query(
        """
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
        """
    )
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :id")
    fun clearPlaylist(id: Long)

    @Query("DELETE FROM SongAlbumMap WHERE albumId = :id")
    fun clearAlbum(id: String)

    @Query("SELECT loudnessDb FROM Format WHERE songId = :songId")
    fun loudnessDb(songId: String): Flow<Float?>

    @Query("SELECT Song.loudnessBoost FROM Song WHERE id = :songId")
    fun loudnessBoost(songId: String): Flow<Float?>

    @Query("UPDATE Song SET loudnessBoost = :loudnessBoost WHERE id = :songId")
    fun setLoudnessBoost(songId: String, loudnessBoost: Float?)

    @Query("SELECT * FROM Song WHERE title LIKE :query OR artistsText LIKE :query")
    fun search(query: String): Flow<List<Song>>

    @Query("SELECT albumId AS id, NULL AS name FROM SongAlbumMap WHERE songId = :songId")
    fun songAlbumInfo(songId: String): Info

    @Query("SELECT id, name FROM Artist LEFT JOIN SongArtistMap ON id = artistId WHERE songId = :songId")
    fun songArtistInfo(songId: String): List<Info>

    @Transaction
    @Query(
        """
        SELECT Song.* FROM Event
        JOIN Song ON Song.id = songId
        WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%'
        GROUP BY songId
        ORDER BY SUM(playTime)
        DESC LIMIT :limit
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun trending(limit: Int = 3): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT Song.* FROM Event
        JOIN Song ON Song.id = songId
        WHERE (:now - Event.timestamp) <= :period AND
        Song.id NOT LIKE '$LOCAL_KEY_PREFIX%'
        GROUP BY songId
        ORDER BY SUM(playTime) DESC
        LIMIT :limit
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun trending(
        limit: Int = 3,
        now: Long = System.currentTimeMillis(),
        period: Long
    ): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Event ORDER BY timestamp DESC")
    fun events(): Flow<List<EventWithSong>>

    @Query("SELECT COUNT (*) FROM Event")
    fun eventsCount(): Flow<Int>

    @Query("DELETE FROM Event")
    fun clearEvents()

    @Query("DELETE FROM Event WHERE songId = :songId")
    fun clearEventsFor(songId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Throws(SQLException::class)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(format: Format)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMap): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(songArtistMap: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(queuedSongs: List<QueuedSong>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongPlaylistMaps(songPlaylistMaps: List<SongPlaylistMap>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album, songAlbumMap: SongAlbumMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artists: List<Artist>, songArtistMaps: List<SongArtistMap>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pipedSession: PipedSession)

    @Transaction
    fun insert(mediaItem: MediaItem, block: (Song) -> Song = { it }) {
        val extras = mediaItem.mediaMetadata.extras?.songBundle
        val song = mediaItem.toSong().let(block).also { song ->
            if (insert(song) == -1L) return
        }

        extras?.albumId?.let { albumId ->
            insert(
                Album(id = albumId, title = mediaItem.mediaMetadata.albumTitle?.toString()),
                SongAlbumMap(songId = song.id, albumId = albumId, position = null)
            )
        }

        extras?.artistNames?.let { artistNames ->
            extras.artistIds?.let { artistIds ->
                if (artistNames.size == artistIds.size) insert(
                    artistNames.mapIndexed { index, artistName ->
                        Artist(
                            id = artistIds[index],
                            name = artistName
                        )
                    },
                    artistIds.map { artistId ->
                        SongArtistMap(
                            songId = song.id,
                            artistId = artistId
                        )
                    }
                )
            }
        }
    }

    @Transaction
    fun insert(item: Triple<Song, Pair<Album, SongAlbumMap>, Pair<Artist, SongArtistMap>>) {
        val (song, album, artist) = item
        insert(song)
        insert(album.first, album.second)
        insert(listOf(artist.first), listOf(artist.second))
    }

    @Update
    fun update(artist: Artist)

    @Update
    fun update(album: Album)

    @Update
    fun update(playlist: Playlist)

    @Upsert
    fun upsert(lyrics: Lyrics)

    @Upsert
    fun upsert(album: Album, songAlbumMaps: List<SongAlbumMap>)

    @Upsert
    fun upsert(artist: Artist)

    @Delete
    fun delete(song: Song)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)

    @Delete
    fun delete(pipedSession: PipedSession)

    @RawQuery
    fun raw(supportSQLiteQuery: SupportSQLiteQuery): Int

    fun checkpoint() {
        raw(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
    }
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SongAlbumMap::class,
        SearchQuery::class,
        QueuedSong::class,
        Format::class,
        Event::class,
        Lyrics::class,
        PipedSession::class
    ],
    views = [SortedSongPlaylistMap::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DatabaseInitializer protected constructor() : RoomDatabase() {
    abstract val database: Database

    companion object {
        @Volatile
        lateinit var instance: DatabaseInitializer

        private fun buildDatabase() = Room
            .databaseBuilder(
                context = Dependencies.application.applicationContext,
                klass = DatabaseInitializer::class.java,
                name = "data.db"
            )
            .build()

        operator fun invoke() {
            if (!::instance.isInitialized) reload()
        }

        fun reload() = synchronized(this) {
            instance = buildDatabase()
        }
    }
}

@Suppress("unused")
@TypeConverters
object Converters {

    @TypeConverter
    fun urlToString(url: Url) = url.toString()

    @TypeConverter
    fun stringToUrl(string: String) = Url(string)
}

@Suppress("UnusedReceiverParameter")
val Database.internal: RoomDatabase
    get() = DatabaseInitializer.instance

fun query(block: () -> Unit) = DatabaseInitializer.instance.queryExecutor.execute(block)

fun transaction(block: () -> Unit) = with(DatabaseInitializer.instance) {
    transactionExecutor.execute {
        runInTransaction(block)
    }
}

val RoomDatabase.path: String?
    get() = openHelper.writableDatabase.path
