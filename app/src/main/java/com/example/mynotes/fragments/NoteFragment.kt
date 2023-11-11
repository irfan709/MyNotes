package com.example.mynotes.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotes.MainActivity
import com.example.mynotes.R
import com.example.mynotes.adapter.NotesAdapter
import com.example.mynotes.database.NotesDatabase
import com.example.mynotes.databinding.FragmentNoteBinding
import com.example.mynotes.repository.NotesRepository
import com.example.mynotes.utils.SwipeToDelete
import com.example.mynotes.utils.hideKeyboard
import com.example.mynotes.viewmodel.NoteActivityViewModel
import com.example.mynotes.viewmodel.NoteActivityViewModelFactory
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding

    //    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private fun getNoteActivityViewModel(): NoteActivityViewModel {
        val activity = requireActivity() as MainActivity
        val noteRepository = NotesRepository(NotesDatabase.invoke(activity))
        val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
        return ViewModelProvider(
            activity,
            noteActivityViewModelFactory
        ).get(NoteActivityViewModel::class.java)
    }

    private lateinit var noteAdapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        noteBinding = FragmentNoteBinding.inflate(inflater, container, false)
        return noteBinding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }

        noteBinding.addnote.setOnClickListener {
            noteBinding.appbar.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }

        noteBinding.addfab.setOnClickListener {
            noteBinding.addfab.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }

        recyclerViewDisplay()
        swipeToDelete(noteBinding.rvnote)

        noteBinding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                noteBinding.nodata.isVisible = false
            }

//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s.toString().isNotEmpty()) {
//                    val text = s.toString()
//                    val query = "%$text%"
//                    if (query.isNotEmpty()) {
//                        getNoteActivityViewModel().searchNote(query).observe(viewLifecycleOwner) {
//                            noteAdapter.submitList(it)
//                        }
//                    } else {
//                        observerDataChanges()
//                    }
//                } else {
//                    observerDataChanges()
//                }
//            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    val text = s.toString()
                    val query = "%$text%"
                    if (query.isNotEmpty()) {
                        getNoteActivityViewModel().searchNote(query).observe(viewLifecycleOwner) {
                            // Filter the list based on your criteria
                            val filteredList = it.filter { note ->
                                note.title.contains(
                                    text,
                                    ignoreCase = true
                                ) || note.content.contains(text, ignoreCase = true)
                            }
                            noteAdapter.submitList(filteredList)
                        }
                    } else {
                        observerDataChanges()
                    }
                } else {
                    observerDataChanges()
                }
            }


            override fun afterTextChanged(s: Editable?) {

            }

        })

        noteBinding.search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

        noteBinding.rvnote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    noteBinding.chatfabtext.isVisible = false
                }

                scrollX == scrollY -> {
                    noteBinding.chatfabtext.isVisible = true
                }

                else -> {
                    noteBinding.chatfabtext.isVisible = true
                }
            }
        }
    }

    private fun swipeToDelete(rvnote: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = noteAdapter.currentList[position]

                val snackBar = Snackbar.make(
                    requireView(),
                    "Note Deleted...", Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)

                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction("Undo") {
                            // Do nothing here, as the note will be restored if Undo is clicked
                            getNoteActivityViewModel().saveNote(note)
                            observerDataChanges()
                        }
                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.addfab)
                }

                snackBar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackBar.show()

                // Delete the note after showing the Snackbar
                getNoteActivityViewModel().deleteNote(note)
                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvnote)
    }


    private fun observerDataChanges() {
        getNoteActivityViewModel().getAllNotes().observe(viewLifecycleOwner) { list ->
            noteBinding.nodata.isVisible = list.isEmpty()
            noteAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
        noteBinding.rvnote.apply {
            val layoutManager =
                StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            noteBinding.rvnote.layoutManager = layoutManager
            noteBinding.rvnote.setHasFixedSize(true)
            noteAdapter = NotesAdapter()
            noteAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = noteAdapter
            postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }
}