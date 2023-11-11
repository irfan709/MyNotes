package com.example.mynotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mynotes.repository.NotesRepository

class NoteActivityViewModelFactory(private val repository: NotesRepository): ViewModelProvider.NewInstanceFactory() {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return NoteActivityViewModel(repository) as T
//    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}