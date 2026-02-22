package com.dicoding.mynoteappsroom.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.mynoteappsroom.R
import com.dicoding.mynoteappsroom.databinding.ActivityMainBinding
import com.dicoding.mynoteappsroom.helper.ViewModelFactory
import com.dicoding.mynoteappsroom.ui.insert.NoteAddUpdateActivity

class MainActivity : AppCompatActivity() {

    // ViewBinding: referensi ke semua view di layout tanpa perlu findViewById,
    // lebih aman dari NullPointerException karena binding hanya null sebelum setContentView.
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Membuat konten meluas ke area system bar (status bar & navigation bar).
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memastikan konten tidak tertutup system bar dengan menambahkan padding dinamis.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mendapatkan ViewModel melalui Factory agar ViewModel bisa menerima parameter Application.
        val mainViewModel = obtainViewModel(this@MainActivity)

        // observe() mendaftarkan Activity sebagai pendengar perubahan data.
        // Setiap kali data di database berubah, blok ini otomatis dipanggil — tidak perlu refresh manual.
        mainViewModel.getAllNotes().observe(this) { noteList ->
            if (noteList != null) {
                adapter.setListNotes(noteList) // Kirim data terbaru ke adapter untuk di-render.
            }
        }

        adapter = NoteAdapter()
        // LinearLayoutManager mengatur item ditampilkan dalam satu kolom vertikal.
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        // setHasFixedSize(true) adalah optimasi: RecyclerView tidak perlu re-measure ukurannya
        // setiap kali item berubah, karena ukuran RecyclerView itu sendiri tidak berubah.
        binding.rvNotes.setHasFixedSize(true)
        binding.rvNotes.adapter = adapter

        // FAB (Floating Action Button) sebagai aksi utama: membuka form tambah catatan baru.
        // Tidak ada data yang perlu dikirim karena ini mode tambah (bukan edit).
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi ini diekstrak agar logika pembuatan ViewModel tidak mencemari onCreate.
    // Penggunaan ViewModelFactory.getInstance() memastikan hanya satu instance Factory yang dibuat.
    private fun obtainViewModel(activity: AppCompatActivity): MainViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(MainViewModel::class.java)
    }
}