package app.banafsh.android.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import app.banafsh.android.Dependencies
import app.banafsh.android.data.enums.SongSortBy
import app.banafsh.android.data.enums.SortOrder
import app.banafsh.android.data.model.Song
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Delete
    fun delete(song: Song)
}

@androidx.room.Database(
    entities = [
        Song::class,
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
