package app.banafsh.android.db

import androidx.media3.common.MediaItem
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import app.banafsh.android.Dependencies
import app.banafsh.android.data.enums.AlbumSortBy
import app.banafsh.android.data.enums.ArtistSortBy
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Album
import app.banafsh.android.data.model.Artist
import app.banafsh.android.data.model.Event
import app.banafsh.android.data.model.QueuedSong
import app.banafsh.android.data.model.Song
import app.banafsh.android.data.model.SongAlbumMap
import app.banafsh.android.data.model.SongArtistMap
import app.banafsh.android.util.toSong
import kotlinx.coroutines.flow.Flow

@Dao
interface Database {
    companion object : Database by DatabaseInitializer.instance.database

    @Transaction
    @Query("SELECT * FROM Song ORDER BY title ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY title DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY dateModified ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByDateAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY dateModified DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByDateDesc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY totalPlayTimeMs ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeDesc(): Flow<List<Song>>

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder) = when (sortBy) {
        SongSortBy.PlayTime ->
            when (sortOrder) {
                SortOrder.Ascending -> songsByPlayTimeAsc()
                SortOrder.Descending -> songsByPlayTimeDesc()
            }

        SongSortBy.Title ->
            when (sortOrder) {
                SortOrder.Ascending -> songsByTitleAsc()
                SortOrder.Descending -> songsByTitleDesc()
            }

        SongSortBy.DateAdded ->
            when (sortOrder) {
                SortOrder.Ascending -> songsByDateAsc()
                SortOrder.Descending -> songsByDateDesc()
            }
    }

    @Transaction
    @Query("SELECT * FROM Song WHERE id IN (:songIds)")
    @RewriteQueriesToDropUnusedColumns
    fun songs(songIds: List<String>): List<Song>

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun like(songId: String, likedAt: Long?): Int

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String): Flow<Long?>

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        JOIN SongArtistMap ON Song.id = SongArtistMap.songId
        WHERE SongArtistMap.artistId = :artistId
        ORDER BY Song.ROWID DESC
        """,
    )
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<Song>>

    @Query("SELECT * FROM Artist ORDER BY name ASC")
    fun artistsByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY name DESC")
    fun artistsByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY bookmarkedAt ASC")
    fun artistsByRowIdAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist ORDER BY bookmarkedAt DESC")
    fun artistsByRowIdDesc(): Flow<List<Artist>>

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
        ORDER BY position
        """,
    )
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Query("SELECT * FROM Album ORDER BY title ASC")
    fun albumsByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY title DESC")
    fun albumsByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY year ASC")
    fun albumsByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY year DESC")
    fun albumsByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY bookmarkedAt ASC")
    fun albumsByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album ORDER BY bookmarkedAt DESC")
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

    @Query("SELECT COUNT (*) FROM Event")
    fun eventsCount(): Flow<Int>

    @Query("DELETE FROM Event")
    fun clearEvents()

    @Query("SELECT * FROM QueuedSong")
    fun queue(): List<QueuedSong>

    @Query("DELETE FROM QueuedSong")
    fun clearQueue()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artist: Artist, songArtistMap: SongArtistMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album, songAlbumMaps: SongAlbumMap)

    @Transaction
    fun insert(item: Triple<Song, Pair<Artist, SongArtistMap>, Pair<Album, SongAlbumMap>>) {
        val (song, artist, album) = item
        insert(song)
        insert(artist.first, artist.second)
        insert(album.first, album.second)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(queuedSongs: List<QueuedSong>)

    @Transaction
    fun insert(mediaItem: MediaItem, block: (Song) -> Song) {
        mediaItem.toSong().let(block).also { song ->
            if (insert(song) == -1L) return
        }
    }

    @Update
    fun update(song: Song)

    @Delete
    fun delete(song: Song)
}

@androidx.room.Database(
    entities = [
        Song::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SongAlbumMap::class,
        Event::class,
        QueuedSong::class,
    ],
    version = 1,
)
abstract class DatabaseInitializer protected constructor() : RoomDatabase() {
    abstract val database: Database

    companion object {
        @Volatile
        lateinit var instance: DatabaseInitializer

        private fun buildDatabase() = Room
            .inMemoryDatabaseBuilder(
                context = Dependencies.application.applicationContext,
                klass = DatabaseInitializer::class.java,
            )
            .build()

        operator fun invoke() {
            if (!::instance.isInitialized) reload()
        }

        private fun reload() = synchronized(this) {
            instance = buildDatabase()
        }
    }
}

fun query(block: () -> Unit) = DatabaseInitializer.instance.queryExecutor.execute(block)

fun transaction(block: () -> Unit) = with(DatabaseInitializer.instance) {
    transactionExecutor.execute {
        runInTransaction(block)
    }
}
