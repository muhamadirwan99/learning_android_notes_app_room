package com.dicoding.mynoteappsroom.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.mynoteappsroom.database.Note
import com.dicoding.mynoteappsroom.databinding.ItemNoteBinding
import com.dicoding.mynoteappsroom.helper.NoteDiffCallback
import com.dicoding.mynoteappsroom.ui.insert.NoteAddUpdateActivity

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    // Menyimpan data list secara internal agar Adapter punya kontrol penuh atas perubahannya.
    // Diinisialisasi sebagai ArrayList kosong agar tidak null saat adapter pertama kali dibuat.
    private val listNotes = ArrayList<Note>()

    fun setListNotes(listNotes: List<Note>) {
        // DiffUtil menghitung perbedaan antara list lama dan baru secara cerdas
        // sebelum data di-update, sehingga animasi perubahan item bisa ditampilkan dengan tepat.
        val diffCallback = NoteDiffCallback(this.listNotes, listNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Bersihkan list lama lalu isi dengan data baru sebelum dispatching update ke RecyclerView.
        this.listNotes.clear()
        this.listNotes.addAll(listNotes)

        // dispatchUpdatesTo memberitahu RecyclerView item mana yang berubah, ditambah, atau dihapus
        // berdasarkan hasil kalkulasi DiffUtil — lebih efisien dari notifyDataSetChanged().
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Inflate layout item menggunakan ViewBinding — lebih type-safe daripada findViewById.
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        // RecyclerView memanggil ini setiap kali item perlu ditampilkan atau di-recycle.
        holder.bind(listNotes[position])
    }

    override fun getItemCount(): Int {
        // RecyclerView membutuhkan ini untuk mengetahui berapa item yang perlu dirender.
        return listNotes.size
    }

    // inner class agar NoteViewHolder bisa mengakses listNotes milik Adapter (binding context).
    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            with(binding) {
                // Mengisi setiap view dengan data dari objek Note yang sesuai posisinya.
                tvItemTitle.text = note.title
                tvItemDate.text = note.date
                tvItemDescription.text = note.description

                // Saat item diklik, buka NoteAddUpdateActivity dengan data note yang diklik
                // dikirim lewat Intent (memanfaatkan Parcelable) agar Activity tahu mode Edit.
                cvItemNote.setOnClickListener {
                    val intent = Intent(it.context, NoteAddUpdateActivity::class.java)
                    intent.putExtra(NoteAddUpdateActivity.EXTRA_NOTE, note) // note dikirim sebagai Parcelable
                    it.context.startActivity(intent)
                }
            }
        }
    }
}
