package com.dicoding.mynoteappsroom.helper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// object (singleton) digunakan karena DateHelper tidak menyimpan state apapun —
// tidak perlu instansiasi baru setiap kali dipakai, cukup panggil DateHelper.getCurrentDate().
object DateHelper {
    fun getCurrentDate(): String {
        // Format "yyyy/MM/dd HH:mm:ss" dipilih agar tanggal bisa diurutkan secara alfabet
        // (dari yang terlama ke terbaru) karena urutannya dari komponen terbesar ke terkecil.
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        // Date() mengambil waktu saat ini dari sistem — snapshot waktu tepat saat fungsi dipanggil.
        val date = Date()
        // Mengubah objek Date menjadi String sesuai format yang telah ditentukan.
        return dateFormat.format(date)
    }
}