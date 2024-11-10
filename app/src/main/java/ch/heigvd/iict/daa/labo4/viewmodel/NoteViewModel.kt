package ch.heigvd.iict.daa.labo4.viewmodel

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.heigvd.iict.daa.labo4.NotesApp
import ch.heigvd.iict.daa.labo4.lifecycle.SharedPreferencesEnumeratedLiveData
import ch.heigvd.iict.daa.labo4.model.NoteAndSchedule

private const val NOTES_PREFS_CONTEXT_KEY = "notes_prefs"
private const val NOTES_PREFS_KEY_SORT_ORDER = "sort_order"

/**
 * ViewModel for notes, it handles the business logic and the communication between the repository
 * and the UI fragments that have to reference them.
 *
 * @param application the application, used to access the shared preferences and get the repository
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NoteViewModel(application: NotesApp) : AndroidViewModel(application) {

    private val repository = application.noteRepository

    // Mutable data binding allowing to save [SortOrder] in the shared preferences
    private val sortOrder = SharedPreferencesEnumeratedLiveData(
        application.getSharedPreferences(NOTES_PREFS_CONTEXT_KEY, Context.MODE_PRIVATE),
        NOTES_PREFS_KEY_SORT_ORDER,
        SortOrder.NONE
    )

    /** Reference to the observable note list */
    val allNotes = repository.notes

    /** Reference to the observable note count */
    val noteCount = repository.noteCount

    /** Reference to the observable note list sorted by the saved sort order */
    val sortedNotes = sortOrder.switchMap { allNotes.map { notes -> it.sorter(notes) } }

    /**
     * Set the sort order for the notes.
     *
     * @param sortOrder the sort order to set
     */
    fun setSortOrder(sortOrder: SortOrder) {
        this.sortOrder.setValue(sortOrder)
    }

    /**
     * Generate a random note and schedule and insert them in the database.
     */
    fun generateNote() {
        repository.generateNote()
    }

    /**
     * Delete all notes and their schedules.
     */
    fun deleteAllNotes() {
        repository.deleteNotes()
    }

    /**
     * Sort possibilities for the notes.
     */
    enum class SortOrder(val sorter: (List<NoteAndSchedule>) -> List<NoteAndSchedule>) {
        BY_ETA({ notes ->
            notes.sortedWith(compareBy(nullsLast()) { note -> note.schedule?.date })
        }),
        BY_CREATION_DATE({ notes ->
            notes.sortedByDescending { note -> note.note.creationDate }
        }),
        NONE({ notes -> notes }),
    }

    companion object {

        /**
         * Factory for the [NoteViewModel].
         *
         * @author Emilie Bressoud
         * @author Loïc Herman
         * @author Sacha Butty
         */
        val Factory = viewModelFactory {
            initializer { NoteViewModel(requireNotNull(this[APPLICATION_KEY]) as NotesApp) }
        }
    }
}