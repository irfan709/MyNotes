package com.example.mynotes.repository

import com.example.mynotes.database.NotesDatabase
import com.example.mynotes.models.Note

class NotesRepository(private val  db: NotesDatabase) {

    fun getNotes() = db.getNotesDao().getAllNotes()

    fun searchNote(query: String) = db.getNotesDao().searchNotes(query)

    suspend fun addNote(note: Note) = db.getNotesDao().addNote(note)

    suspend fun updateNote(note: Note) = db.getNotesDao().updateNote(note)

    suspend fun deleteNote(note: Note) = db.getNotesDao().deleteNote(note)

}