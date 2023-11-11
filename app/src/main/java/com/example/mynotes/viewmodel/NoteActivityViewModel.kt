package com.example.mynotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.models.Note
import com.example.mynotes.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActivityViewModel(private val repository: NotesRepository): ViewModel() {

    fun saveNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.addNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(note)
    }

//    fun searchNote(query: String) = viewModelScope.launch(Dispatchers.IO) {
//        repository.searchNote(query)
//    }

    fun searchNote(query: String): LiveData<List<Note>> {
        return repository.searchNote(query)
    }

//    fun getAllNotes(): LiveData<List<Note>> = repository.getNotes()
fun getAllNotes(): LiveData<List<Note>> {
    return repository.getNotes()
}
}