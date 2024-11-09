package ch.heigvd.iict.daa.labo4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ch.heigvd.iict.daa.labo4.databinding.ActivityMainBinding
import ch.heigvd.iict.daa.labo4.viewmodel.NoteViewModel
import ch.heigvd.iict.daa.labo4.viewmodel.NoteViewModel.SortOrder
import ch.heigvd.iict.daa.labo4.viewmodel.noteViewModel

/**
 * Main activity for our application, it initializes the main view and viewmodel, as well as
 * binding context options to the viewmodel actions.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class MainActivity : AppCompatActivity() {

    // Bindings to the activity content
    private lateinit var viewBinding: ActivityMainBinding

    private val viewModel: NoteViewModel by noteViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter_creation_date -> viewModel.setSortOrder(SortOrder.BY_CREATION_DATE)
            R.id.menu_filter_eta -> viewModel.setSortOrder(SortOrder.BY_ETA)
            R.id.menu_actions_generate -> viewModel.generateNote()
            R.id.menu_actions_delete_all -> viewModel.deleteAllNotes()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}