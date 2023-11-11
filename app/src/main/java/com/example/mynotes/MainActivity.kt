package com.example.mynotes

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.mynotes.database.NotesDatabase
import com.example.mynotes.databinding.ActivityMainBinding
import com.example.mynotes.repository.NotesRepository
import com.example.mynotes.viewmodel.NoteActivityViewModel
import com.example.mynotes.viewmodel.NoteActivityViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        try {
            setContentView(binding.root)
            val noteRepository = NotesRepository(NotesDatabase.invoke(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this, noteActivityViewModelFactory).get(NoteActivityViewModel::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}