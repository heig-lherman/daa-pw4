package ch.heigvd.iict.daa.labo4.viewmodel

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import ch.heigvd.iict.daa.labo4.NotesApp
import ch.heigvd.iict.daa.labo4.lifecycle.SharedPreferencesEnumeratedLiveData
import ch.heigvd.iict.daa.labo4.model.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.repository.NoteRepository
import kotlin.reflect.KClass

private const val NOTES_PREFS_CONTEXT_KEY = "notes_prefs"
private const val NOTES_PREFS_KEY_SORT_ORDER = "sort_order"

/**
 * ViewModel for notes, it handles the business logic and the communication between the repository
 * and the UI fragments that have to reference them.
 *
 * @param repository the repository that provides the data
 * @param context the context of the application, used to access the shared preferences
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NoteViewModel(
    private val repository: NoteRepository,
    application: Application
) : AndroidViewModel(application) {

    // Mutable data binding allowing to save [SortOrder] in the shared preferences
    private val sortOrder = SharedPreferencesEnumeratedLiveData<SortOrder>(
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
        this.sortOrder.value = sortOrder
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
}

/**
 * Factory for the [NoteViewModel].
 *
 * @param repository the repository that provides the data
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NoteViewModelFactory(
    private val repository: NoteRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.java.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository, application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Get a [NoteViewModel] for a [Fragment]. This abstracts away the call to define the factory and
 * fetching the context from the application.
 */
inline fun Fragment.noteViewModel(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline extrasProducer: (() -> CreationExtras)? = null,
): Lazy<NoteViewModel> {
    return viewModels<NoteViewModel>(ownerProducer, extrasProducer) {
        (requireActivity().application as NotesApp).let {
            NoteViewModelFactory(it.noteRepository, it)
        }
    }
}

/**
 * Get a [NoteViewModel] for a [ComponentActivity]. This abstracts away the call to define the
 * factory and fetching the context from the application.
 */
inline fun ComponentActivity.noteViewModel(
    noinline extrasProducer: (() -> CreationExtras)? = null,
): Lazy<NoteViewModel> {
    return viewModels<NoteViewModel>(extrasProducer) {
        (application as NotesApp).let {
            NoteViewModelFactory(it.noteRepository, it)
        }
    }
}
