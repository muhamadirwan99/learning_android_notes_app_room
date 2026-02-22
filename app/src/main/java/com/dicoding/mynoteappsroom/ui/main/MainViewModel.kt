package com.dicoding.mynoteappsroom.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.mynoteappsroom.database.Note
import com.dicoding.mynoteappsroom.repository.NoteRepository

// ViewModel bertanggung jawab menyediakan data ke UI dan bertahan saat terjadi
// perubahan konfigurasi (rotasi layar). Activity bisa hancur-dibuat ulang, tapi
// ViewModel tetap hidup — data tidak hilang dan tidak perlu fetch ulang.
class MainViewModel(application: Application) : ViewModel() {
    // Repository diinisialisasi di sini agar ViewModel tidak langsung bergantung pada
    // implementasi database — sesuai prinsip separation of concerns.
    private val mNoteRepository : NoteRepository = NoteRepository(application)

    // Meneruskan LiveData dari Repository ke Activity/Fragment.
    // ViewModel hanya menjadi perantara; observer di Activity yang bereaksi saat data berubah.
    fun getAllNotes(): LiveData<List<Note>> = mNoteRepository.getAllNotes()
}