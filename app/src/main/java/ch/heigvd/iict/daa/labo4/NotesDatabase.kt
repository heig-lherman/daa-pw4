package ch.heigvd.iict.daa.labo4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.heigvd.iict.daa.labo4.converter.CalendarConverter
import ch.heigvd.iict.daa.labo4.model.Note
import ch.heigvd.iict.daa.labo4.model.Schedule
import ch.heigvd.iict.daa.labo4.repository.NoteDAO
import kotlinx.coroutines.CoroutineScope

/**
 * Room database definition for the notes application.
 * Singleton initialized by the application class ([NotesApp]).
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
@Database(entities = [Note::class, Schedule::class], version = 1, exportSchema = true)
@TypeConverters(CalendarConverter::class)
abstract class NotesDatabase : RoomDatabase() {

    /**
     * Data access object for notes.
     */
    abstract fun noteDao(): NoteDAO;

    companion object {
        private var instance: NotesDatabase? = null

        /**
         * Get the singleton instance of the database.
         *
         * @param context the context in which the database is created
         * @param scope the scope in which the database will run
         * @return the singleton instance of the database
         */
        fun getInstance(context: Context, scope: CoroutineScope): NotesDatabase {
            return instance ?: synchronized(this) {
                return Room
                    .databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java,
                        "notes_database.db"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(databasePopulateCallback)
                    .build()
                    .also { instance = it }
            }
        }

        // callback that will populate the database with some notes
        private val databasePopulateCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
            }

            override fun onCreate(db: SupportSQLiteDatabase) {
                instance?.let { database ->
                    val dao = database.noteDao()
                    repeat(10) {
                        dao.generateNote()
                    }
                }
            }
        }
    }
}
