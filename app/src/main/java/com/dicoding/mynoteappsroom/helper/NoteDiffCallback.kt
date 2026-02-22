package com.dicoding.mynoteappsroom.helper

import androidx.recyclerview.widget.DiffUtil
import com.dicoding.mynoteappsroom.database.Note

// DiffUtil.Callback digunakan untuk menghitung perbedaan antara dua daftar secara efisien.
// Tujuannya: RecyclerView hanya me-render ulang item yang BENAR-BENAR berubah,
// bukan me-render ulang seluruh list — membuat animasi update lebih mulus dan hemat resource.
class NoteDiffCallback(private val oldNoteList: List<Note>, private val newNoteList: List<Note>) : DiffUtil.Callback() {
    // DiffUtil butuh tahu ukuran masing-masing list untuk menghitung rentang perbandingan.
    override fun getOldListSize(): Int = oldNoteList.size

    override fun getNewListSize(): Int = newNoteList.size

    // Tahap pertama: cek apakah dua item merepresentasikan OBJEK yang sama (identitas).
    // Menggunakan id karena id adalah identifier unik setiap catatan di database.
    // Jika id berbeda, item pasti berbeda — tidak perlu cek konten.
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldNoteList[oldItemPosition].id == newNoteList[newItemPosition].id
    }

    // Tahap kedua (hanya dijalankan jika areItemsTheSame = true):
    // cek apakah KONTEN item berubah. Jika title & description sama, tidak perlu re-render.
    // date sengaja tidak dicek karena date tidak ditampilkan sebagai konten yang bisa diedit.
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldNote = oldNoteList[oldItemPosition]
        val newNote = newNoteList[newItemPosition]

        return oldNote.title == newNote.title && oldNote.description == newNote.description
    }

}