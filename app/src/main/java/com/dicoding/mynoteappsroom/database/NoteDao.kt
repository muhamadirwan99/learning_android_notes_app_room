package com.dicoding.mynoteappsroom.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// @Dao (Data Access Object) adalah kontrak/interface yang mendefinisikan semua operasi
// database yang boleh dilakukan. Room akan men-generate implementasinya secara otomatis
// saat kompilasi, sehingga kita tidak perlu menulis kode SQL secara langsung di Activity/ViewModel.
@Dao
interface NoteDao {
    // OnConflictStrategy.IGNORE: jika ada data duplikat (misal id sama), abaikan saja
    // dan jangan crash — berguna untuk mencegah data ganda tanpa harus cek manual.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(note: Note)

    // @Update mencocokkan data berdasarkan Primary Key (id) lalu memperbarui baris yang sesuai.
    @Update
    fun update(note: Note)

    // @Delete menghapus baris yang Primary Key-nya cocok dengan objek Note yang diberikan.
    @Delete
    fun delete(note: Note)

    // Mengambil semua catatan dan mengurutkannya dari id terkecil (paling lama dibuat).
    // Return-nya LiveData agar UI otomatis di-refresh setiap ada perubahan data di database —
    // tidak perlu polling manual.
    @Query("SELECT * from note ORDER BY id ASC")
    fun getAllNotes(): LiveData<List<Note>>
}