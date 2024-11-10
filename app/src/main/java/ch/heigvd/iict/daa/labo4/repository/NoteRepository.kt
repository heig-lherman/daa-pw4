package ch.heigvd.iict.daa.labo4.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ch.heigvd.iict.daa.labo4.model.Note
import ch.heigvd.iict.daa.labo4.model.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Repository for notes, it is responsible for fetching and storing notes.
 * Allows to provide a simplified API to the view models, and to abstract the data source away.
 *
 * @param noteDao the data access object for notes
 * @param applicationScope the scope in which the repository will run
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NoteRepository(
    private val noteDao: NoteDAO,
    private val applicationScope: CoroutineScope
) {

    /**
     * LiveData of all notes and their schedules.
     */
    val notes = noteDao.findAll()

    /**
     * LiveData of the number of notes.
     */
    val noteCount = noteDao.count()

    /**
     * Generate a random note and schedule and insert them in the database.
     */
    fun generateNote() {
        applicationScope.launch {
            noteDao.generateNote()
        }
    }

    /**
     * Delete all notes and their schedules.
     */
    fun deleteNotes() {
        applicationScope.launch {
            noteDao.deleteNotes()
            noteDao.deleteSchedules()
        }
    }
}

/**
 * Data access object for notes, it defines the queries that can be made on the notes table.
 * It is used solely by the repository to fetch and store notes.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
@Dao
interface NoteDAO {

    /**
     * Get all notes and their schedules.
     *
     * @return an observable list of notes and their schedules
     */
    @Query("SELECT * FROM note")
    @Transaction
    fun findAll(): LiveData<List<NoteAndSchedule>>

    /**
     * Get the number of notes.
     *
     * @return an observable count of the notes
     */
    @Query("SELECT COUNT(*) FROM note")
    fun count(): LiveData<Int>

    /**
     * Insert a note.
     *
     * @param note the note to insert
     * @return the id of the inserted note
     */
    @Insert
    fun insert(note: Note): Long

    /**
     * Insert a schedule.
     *
     * @param schedule the schedule to insert
     * @return the id of the inserted schedule
     */
    @Insert
    fun insert(schedule: Schedule): Long

    /**
     * Deletes all notes.
     */
    @Query("DELETE FROM note")
    fun deleteNotes()

    /**
     * Deletes all schedules. Note: there are no foreign keys,
     * it is recommended to delete notes first.
     */
    @Query("DELETE FROM schedule")
    fun deleteSchedules()

    /**
     * Generate a random note and schedule, and insert it.
     *
     * @return the id of the inserted note
     */
    @Transaction
    fun generateNote(): Long {
        return insert(
            Note.generateRandomNote()
        ).also { noteId ->
            Note.generateRandomSchedule()?.let {
                it.ownerId = noteId
                insert(it)
            }
        }
    }
}
