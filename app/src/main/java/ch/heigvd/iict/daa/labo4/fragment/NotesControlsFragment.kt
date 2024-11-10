package ch.heigvd.iict.daa.labo4.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.daa.labo4.R
import ch.heigvd.iict.daa.labo4.databinding.FragmentNotesControlsBinding
import ch.heigvd.iict.daa.labo4.viewmodel.NoteViewModel

/**
 * Fragment that displays the controls for the notes, allowing to generate and delete notes.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NotesControlsFragment : Fragment() {

    // Bindings to the fragment content
    private lateinit var viewBinding: FragmentNotesControlsBinding

    private val viewModel: NoteViewModel by activityViewModels { NoteViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentNotesControlsBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View count observer
        viewModel.noteCount.observe(viewLifecycleOwner) {
            viewBinding.notesControlsCounter.text = getString(
                R.string.notes_controls_counter
            ).format(it)
        }

        // Button listeners
        viewBinding.notesControlsButtonGenerate.setOnClickListener { viewModel.generateNote() }
        viewBinding.notesControlsButtonDelete.setOnClickListener { viewModel.deleteAllNotes() }
    }
}