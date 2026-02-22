package com.dicoding.mynoteappsroom.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// @Entity memberitahu Room bahwa class ini adalah representasi sebuah tabel di database SQLite.
// Nama tabel defaultnya mengikuti nama class (yaitu "note").
@Entity
// @Parcelize memungkinkan objek Note dikirim antar Activity lewat Intent secara efisien
// tanpa harus menulis manual proses serialisasi (Parcel write/read).
@Parcelize
data class Note(
    // @PrimaryKey menjadikan field ini sebagai kunci unik tiap baris.
    // autoGenerate = true agar Room otomatis menentukan nilainya (auto-increment),
    // sehingga kita tidak perlu mengisi id secara manual saat membuat Note baru.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") // Nama kolom di tabel SQLite; dieksplisitkan agar tidak bergantung nama variabel.
    var id: Int = 0,

    @ColumnInfo(name = "title") // Memetakan field Kotlin ini ke kolom "title" di tabel.
    var title: String? = null,

    @ColumnInfo(name = "description") // Menyimpan isi catatan di kolom "description".
    var description: String? = null,

    @ColumnInfo(name = "date") // Menyimpan tanggal pembuatan/modifikasi catatan.
    var date: String? = null
) : Parcelable // Menandakan class ini bisa di-pass lewat Intent sebagai Parcel.
