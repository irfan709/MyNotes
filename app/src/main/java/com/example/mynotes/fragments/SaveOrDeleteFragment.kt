package com.example.mynotes.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.mynotes.MainActivity
import com.example.mynotes.R
import com.example.mynotes.databinding.BottomSheetBinding
import com.example.mynotes.databinding.FragmentSaveOrDeleteBinding
import com.example.mynotes.models.Note
import com.example.mynotes.utils.hideKeyboard
import com.example.mynotes.viewmodel.NoteActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class SaveOrDeleteFragment : Fragment(R.layout.fragment_save_or_delete) {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentSaveOrDeleteBinding
    private var note: Note? = null
    private var noteColor = -1
    private lateinit var result: String
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: SaveOrDeleteFragmentArgs by navArgs()
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentcontainer
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }
        sharedElementEnterTransition = animation
        sharedElementEnterTransition = animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSaveOrDeleteBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        ViewCompat.setTransitionName(
            binding.notefragmentparent,
            "recyclerView_${args.note?.id}"
        )

        binding.backarrow.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        binding.savenote.setOnClickListener {
            saveNote()
        }

        try {
            binding.etnotecontent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.bottombar.visibility = View.VISIBLE
                    binding.etnotecontent.setStylesBar(binding.stylebar)
                } else {
                    binding.bottombar.visibility = View.GONE
                }
            }
        } catch (e: Throwable) {
            Log.d("Error", e.toString())
        }

        binding.colorfab.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet,
                null
            )
            with(bottomSheetDialog) {
                this?.setContentView(bottomSheetView)
                this?.show()
            }
            val bottomSheetBinding = BottomSheetBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                colorpicker.apply {
                    setSelectedColor(noteColor)
                    setOnColorSelectedListener { value ->
                        noteColor = value
                        binding.apply {
                            notefragmentparent.setBackgroundColor(noteColor)
                            topbar.setBackgroundColor(noteColor)
                            bottombar.setBackgroundColor(noteColor)
                            activity.window.statusBarColor = noteColor
                        }
                        bottomSheetBinding.bottomparent.setCardBackgroundColor(noteColor)
                    }
                }
                bottomparent.setCardBackgroundColor(noteColor)
            }
            bottomSheetView.post {
                bottomSheetDialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        setUpNote()
    }

    private fun setUpNote() {
        val note = args.note
        val title = binding.etnotetitle
        val content = binding.etnotecontent
        val lastEdited = binding.lastedited

        if (note == null) {
            binding.lastedited.text = getString(R.string.edited_on, currentDate)
        }
        if (note != null) {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on, note.date)
            noteColor = note.color

            binding.apply {
                job.launch {
                    delay(10)
                    notefragmentparent.setBackgroundColor(noteColor)
                }
                topbar.setBackgroundColor(noteColor)
                bottombar.setBackgroundColor(noteColor)
            }
            activity?.window?.statusBarColor = note.color
        }
    }

    private fun saveNote() {
        if (binding.etnotetitle.text.toString().isEmpty() || binding.etnotecontent.text.toString()
                .isEmpty()
        ) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
        } else {
            note = args.note
            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            binding.etnotetitle.text.toString(),
                            binding.etnotecontent.text.toString(),
                            currentDate,
                            noteColor
                        )
                    )

                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(SaveOrDeleteFragmentDirections.actionSaveOrDeleteFragmentToNoteFragment())
                }

                else -> {
                    updateNote()
                    navController.popBackStack()
                }
            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    binding.etnotetitle.text.toString(),
                    binding.etnotecontent.text.toString(),
                    currentDate,
                    noteColor
                )
            )
        }
    }
}