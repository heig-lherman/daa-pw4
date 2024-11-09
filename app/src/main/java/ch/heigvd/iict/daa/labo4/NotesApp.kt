package ch.heigvd.iict.daa.labo4

import android.app.Application
import ch.heigvd.iict.daa.labo4.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Main application class, it handles the creation of the note repository singleton with its
 * associated database, and provides a coroutine scope for the repositories to run in.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NotesApp : Application() {

    // Initialize the coroutine scope for database operations
    private val scope = CoroutineScope(SupervisorJob())

    // Database singletons
    val noteRepository by lazy {
        NoteRepository(
            NotesDatabase.getInstance(this, scope).noteDao(),
            scope
        )
    }
}