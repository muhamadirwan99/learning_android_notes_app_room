package com.dicoding.mynoteappsroom.ui.insert

import android.app.Application
import androidx.lifecycle.ViewModel
import com.dicoding.mynoteappsroom.database.Note
import com.dicoding.mynoteappsroom.repository.NoteRepository

// ViewModel untuk layar tambah/edit/hapus catatan.
// Memisahkan logika bisnis dari Activity agar Activity hanya mengurus tampilan.
class NoteAddUpdateViewModel(application: Application) : ViewModel(){
    // Repository sebagai satu-satunya pintu masuk ke operasi database dari ViewModel ini.
    private val mNoteRepository: NoteRepository = NoteRepository(application)

    // Ketiga fungsi di bawah hanya mendelegasikan ke Repository.
    // Ini menjaga ViewModel tetap tipis (thin) dan Repository yang mengurus detail eksekusi thread.
    fun insert(note: Note){
        mNoteRepository.insert(note)
    }

    fun update(note: Note) {
        mNoteRepository.update(note)
    }

    fun delete(note: Note) {
        mNoteRepository.delete(note)
    }
}