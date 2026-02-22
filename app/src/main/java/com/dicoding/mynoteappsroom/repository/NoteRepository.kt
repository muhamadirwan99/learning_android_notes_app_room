package com.dicoding.mynoteappsroom.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.mynoteappsroom.database.Note
import com.dicoding.mynoteappsroom.database.NoteDao
import com.dicoding.mynoteappsroom.database.NoteRoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Repository adalah lapisan abstraksi antara ViewModel dan sumber data (database/network).
// ViewModel tidak perlu tahu data berasal dari mana — cukup minta ke Repository.
// Pola ini memudahkan penggantian sumber data di masa depan tanpa mengubah ViewModel.
class NoteRepository(application: Application) {
    private val mNotesDao: NoteDao

    // ExecutorService dengan single thread digunakan karena operasi database (insert/update/delete)
    // TIDAK boleh dijalankan di Main Thread (UI Thread) — akan menyebabkan crash/ANR.
    // Single thread juga memastikan operasi dieksekusi secara berurutan, menghindari konflik data.
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        // Mengambil instance database yang sudah ada (Singleton) lalu mendapatkan DAO-nya.
        // Dilakukan di init agar DAO siap digunakan segera setelah Repository dibuat.
        val db = NoteRoomDatabase.getDatabase(application)
        mNotesDao = db.noteDao()
    }

    // getAllNotes() mengembalikan LiveData langsung dari DAO — Room sudah menangani
    // background query secara internal, jadi tidak perlu executorService di sini.
    fun getAllNotes(): LiveData<List<Note>> = mNotesDao.getAllNotes()

    // Operasi write (insert/delete/update) dibungkus executorService.execute { }
    // agar dijalankan di background thread, menjaga UI tetap responsif.
    fun insert(note: Note) {
        executorService.execute { mNotesDao.insert(note) }
    }

    fun delete(note: Note) {
        executorService.execute { mNotesDao.delete(note) }
    }

    fun update(note: Note) {
        executorService.execute { mNotesDao.update(note) }
    }
}