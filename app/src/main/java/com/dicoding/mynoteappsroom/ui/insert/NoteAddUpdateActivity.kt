package com.dicoding.mynoteappsroom.ui.insert

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mynoteappsroom.R
import com.dicoding.mynoteappsroom.database.Note
import com.dicoding.mynoteappsroom.databinding.ActivityNoteAddUpdateBinding
import com.dicoding.mynoteappsroom.helper.DateHelper
import com.dicoding.mynoteappsroom.helper.ViewModelFactory

// Activity ini digunakan untuk dua mode: TAMBAH catatan baru & EDIT catatan yang sudah ada.
// Mode ditentukan dari ada tidaknya data Note yang dikirim lewat Intent.
class NoteAddUpdateActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTE = "extra_note" // Key Intent untuk mengirim/menerima objek Note antar Activity.
        const val ALERT_DIALOG_CLOSE = 10   // Kode penanda untuk dialog konfirmasi batalkan perubahan.
        const val ALERT_DIALOG_DELETE = 20  // Kode penanda untuk dialog konfirmasi hapus catatan.
    }

    // isEdit sebagai flag mode: false = mode tambah, true = mode edit.
    // Digunakan untuk menentukan label, tombol, dan aksi yang ditampilkan.
    private var isEdit = false
    private var note: Note? = null // null jika mode tambah, berisi data jika mode edit.

    private lateinit var noteAddUpdateViewModel: NoteAddUpdateViewModel
    private lateinit var binding: ActivityNoteAddUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_form_add_update)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        noteAddUpdateViewModel = obtainViewModel(this@NoteAddUpdateActivity)

        // Cara mengambil Parcelable berbeda antara Android 13+ dan di bawahnya.
        // Build.VERSION_CODES.TIRAMISU = API 33. Pengecekan ini menghindari deprecation warning
        // sambil tetap mendukung perangkat lama.
        note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<Note>(EXTRA_NOTE, Note::class.java)
        } else {
            @Suppress("Deprecation")
            intent.getParcelableExtra<Note>(EXTRA_NOTE)
        }

        // Jika note tidak null berarti Activity dibuka dari klik item (mode Edit).
        // Jika null, buat objek Note kosong sebagai wadah data yang akan diisi user.
        if (note != null) {
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            actionBarTitle = getString(R.string.change)
            btnTitle = getString(R.string.update)
            // Pre-fill form dengan data note yang sudah ada agar user bisa langsung mengedit.
            if (note != null) {
                note?.let { note ->
                    binding.edtTitle.setText(note.title)
                    binding.edtDescription.setText(note.description)
                }
            }
        } else {
            actionBarTitle = getString(R.string.add)
            btnTitle = getString(R.string.save)
        }

        supportActionBar?.title = actionBarTitle
        // Menampilkan tombol panah kembali di ActionBar agar user bisa navigasi ke belakang.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle

        binding.btnSubmit.setOnClickListener {
            val title = binding.edtTitle.text.toString().trim()       // trim() menghapus spasi di awal/akhir.
            val description = binding.edtDescription.text.toString().trim()
            when {
                // Validasi dilakukan sebelum menyimpan agar tidak ada catatan tanpa judul/deskripsi.
                title.isEmpty() -> {
                    binding.edtTitle.error = getString(R.string.empty)
                }

                description.isEmpty() -> {
                    binding.edtDescription.error = getString(R.string.empty)
                }

                else -> {
                    // Update data note dengan input terbaru dari user sebelum disimpan.
                    note.let { note ->
                        note?.title = title
                        note?.description = description
                    }
                    if (isEdit) {
                        // Mode edit: update baris yang sudah ada di database berdasarkan id-nya.
                        noteAddUpdateViewModel.update(note as Note)
                        showToast(getString(R.string.changed))
                    } else {
                        // Mode tambah: set tanggal hanya saat pertama kali dibuat,
                        // bukan saat diedit — agar tanggal mencerminkan waktu pembuatan asli.
                        note.let { note ->
                            note?.date = DateHelper.getCurrentDate()
                        }
                        noteAddUpdateViewModel.insert(note as Note)
                        showToast(getString(R.string.added))
                    }
                    finish() // Tutup Activity dan kembali ke MainActivity setelah operasi berhasil.
                }
            }
        }

        // Menggunakan OnBackPressedCallback (API modern) sebagai pengganti onBackPressed() yang deprecated.
        // Tujuannya: mencegat tombol Back dan menampilkan konfirmasi sebelum keluar,
        // agar user tidak kehilangan perubahan secara tidak sengaja.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showAlertDialog(ALERT_DIALOG_CLOSE)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Menu hapus hanya dimunculkan saat mode Edit — tidak masuk akal menghapus catatan
        // yang belum tersimpan (mode tambah).
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE) // Tombol hapus di menu.
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)   // Tombol panah kembali di ActionBar.
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(type: Int) {
        // Satu fungsi untuk dua jenis dialog, dibedakan lewat parameter 'type'
        // agar tidak ada duplikasi kode pembuatan AlertDialog.
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = getString(R.string.cancel)
            dialogMessage = getString(R.string.message_cancel)
        } else {
            dialogMessage = getString(R.string.message_delete)
            dialogTitle = getString(R.string.delete)
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        with(alertDialogBuilder) {
            setTitle(dialogTitle)
            setMessage(dialogMessage)
            setCancelable(false) // Mencegah dialog tertutup saat user tap di luar dialog — memaksa pilihan eksplisit.
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (!isDialogClose) {
                    // Hapus data dari database hanya jika user mengkonfirmasi dialog hapus.
                    noteAddUpdateViewModel.delete(note as Note)
                    showToast(getString(R.string.deleted))
                }
                finish() // Baik untuk dialog tutup maupun hapus, keluar dari Activity setelahnya.
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() } // Batal: tutup dialog, tetap di layar.
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showToast(message: String) {
        // Fungsi helper kecil ini menghindari pengulangan boilerplate Toast.makeText setiap kali
        // ingin menampilkan pesan singkat.
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun obtainViewModel(activity: AppCompatActivity): NoteAddUpdateViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(NoteAddUpdateViewModel::class.java)
    }
}