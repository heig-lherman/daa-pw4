package ch.heigvd.iict.daa.labo4.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ch.heigvd.iict.daa.labo4.R
import ch.heigvd.iict.daa.labo4.databinding.FragmentNotesListBinding
import ch.heigvd.iict.daa.labo4.databinding.ListNoteViewItemBinding
import ch.heigvd.iict.daa.labo4.databinding.ListScheduledNoteViewItemBinding
import ch.heigvd.iict.daa.labo4.model.NoteAndSchedule
import ch.heigvd.iict.daa.labo4.model.State
import ch.heigvd.iict.daa.labo4.model.Type
import ch.heigvd.iict.daa.labo4.viewmodel.NoteViewModel
import java.util.Calendar

/**
 * Fragment that displays the notes using a recycler, the adapter of which is provided by the
 * [NotesRecyclerAdapter] class defined below.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class NotesListFragment : Fragment() {

    // Bindings to the fragment content
    private lateinit var viewBinding: FragmentNotesListBinding

    private val viewModel: NoteViewModel by activityViewModels { NoteViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentNotesListBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recycler view with its adapter
        val adapter = NotesRecyclerAdapter(viewModel.sortedNotes.value ?: emptyList())
        viewBinding.notesListRecycler.adapter = adapter

        // Observe the notes and update the adapter
        viewModel.sortedNotes.observe(viewLifecycleOwner) {
            adapter.items = it
        }
    }
}

/**
 * Custom adapter for the recycler view that displays the notes.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
private class NotesRecyclerAdapter(
    initNotes: List<NoteAndSchedule> = emptyList()
) : RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>() {

    private val differ = AsyncListDiffer(this, NoteDiffCallback())

    var items: List<NoteAndSchedule>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    init {
        items = initNotes
    }

    companion object {
        private const val VIEW_TYPE_NOTE = 0
        private const val VIEW_TYPE_NOTE_SCHEDULE = 1
    }

    // Returns the view type of the item at the specified position
    override fun getItemViewType(position: Int): Int {
        return if (items[position].schedule == null) {
            VIEW_TYPE_NOTE
        } else {
            VIEW_TYPE_NOTE_SCHEDULE
        }
    }

    // Selects the view layout based on the view type and inflates it
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NOTE -> {
                ViewHolder(
                    ListNoteViewItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            VIEW_TYPE_NOTE_SCHEDULE -> {
                ViewHolder(
                    ListScheduledNoteViewItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // Binds the view holder to the data at the specified position
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    /**
     * View holder for the recycler view, which binds the data to the view,
     * takes care of selecting the layout and filling out the data.
     */
    inner class ViewHolder(
        private val binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteAndSchedule) {
            when (binding) {
                is ListNoteViewItemBinding -> bind(binding, note)
                is ListScheduledNoteViewItemBinding -> bind(binding, note)
            }
        }

        private fun bind(layout: ListNoteViewItemBinding, note: NoteAndSchedule) {
            layout.apply {
                with(note.note) {
                    listItemTitle.text = title
                    listItemText.text = text
                    noteViewItemIcon.setImageResource(getTypeIcon(type))
                    noteViewItemIcon.imageTintList = getStateTint(noteViewItemIcon.context, state)
                }
            }
        }

        private fun bind(layout: ListScheduledNoteViewItemBinding, note: NoteAndSchedule) {
            layout.apply {
                with(note.note) {
                    listItemTitle.text = title
                    listItemText.text = text
                    noteViewItemIcon.setImageResource(getTypeIcon(type))
                    noteViewItemIcon.imageTintList = getStateTint(noteViewItemIcon.context, state)

                    val (scheduleText, late) = computeDateDiff(
                        listItemTextScheduleText.context,
                        state,
                        note.schedule!!.date
                    )

                    listItemTextScheduleText.text = scheduleText
                    listItemTextScheduleIcon.imageTintList = getScheduleTint(
                        listItemTextScheduleIcon.context,
                        late
                    )
                }
            }
        }

        private fun getTypeIcon(type: Type): Int {
            return when (type) {
                Type.NONE -> R.drawable.note
                Type.TODO -> R.drawable.todo
                Type.SHOPPING -> R.drawable.shopping
                Type.WORK -> R.drawable.work
                Type.FAMILY -> R.drawable.family
            }
        }

        private fun getStateTint(context: Context, state: State): ColorStateList? {
            return when (state) {
                State.IN_PROGRESS -> ContextCompat.getColorStateList(context, R.color.gray)
                State.DONE -> ContextCompat.getColorStateList(context, R.color.green)
            }
        }

        private fun getScheduleTint(context: Context, late: Boolean): ColorStateList? {
            return if (late) {
                ContextCompat.getColorStateList(context, R.color.red)
            } else {
                ContextCompat.getColorStateList(context, R.color.gray)
            }
        }

        private fun computeDateDiff(
            context: Context,
            state: State,
            date: Calendar
        ): Pair<CharSequence, Boolean> {
            val now = Calendar.getInstance()
            if (date.before(now) && state != State.DONE) {
                return Pair(context.getString(R.string.note_view_item_schedule_late), true)
            }

            return Pair(
                DateUtils.getRelativeTimeSpanString(
                    date.timeInMillis,
                    now.timeInMillis,
                    DateUtils.MINUTE_IN_MILLIS
                ),
                false
            )
        }
    }

    /**
     * [DiffUtil.ItemCallback] for the [AsyncListDiffer], used to compute the difference between two
     * lists of notes.
     */
    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteAndSchedule>() {
        override fun areItemsTheSame(oldItem: NoteAndSchedule, newItem: NoteAndSchedule): Boolean {
            return oldItem.note.noteId == newItem.note.noteId
        }

        override fun areContentsTheSame(
            oldItem: NoteAndSchedule,
            newItem: NoteAndSchedule
        ): Boolean {
            // Makes use of automatic data class equality
            return oldItem == newItem
        }
    }
}
