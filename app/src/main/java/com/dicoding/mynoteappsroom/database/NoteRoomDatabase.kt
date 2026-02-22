package com.dicoding.mynoteappsroom.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database mendaftarkan semua Entity (tabel) yang dimiliki database ini.
// version = 1 adalah versi schema; jika struktur tabel berubah, versi harus dinaikkan
// dan migration harus disediakan agar data lama tidak hilang.
@Database(entities = [Note::class], version = 1)
abstract class NoteRoomDatabase : RoomDatabase() {
    // Fungsi abstrak ini diimplementasikan oleh Room secara otomatis.
    // Digunakan sebagai titik masuk untuk mengakses semua operasi DAO.
    abstract fun noteDao(): NoteDao

    companion object {
        // @Volatile memastikan nilai INSTANCE selalu dibaca dari memori utama (bukan cache CPU),
        // sehingga semua thread selalu melihat nilai terbaru — krusial untuk thread safety.
        @Volatile
        private var INSTANCE: NoteRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context) : NoteRoomDatabase {
            // Cek null di luar synchronized untuk efisiensi:
            // jika instance sudah ada, tidak perlu masuk blok synchronized (lebih cepat).
            if (INSTANCE == null){
                // synchronized memastikan hanya satu thread yang boleh membuat instance baru
                // pada saat yang sama — mencegah dua instance database terbuat secara bersamaan
                // (Double-Checked Locking pattern).
                synchronized(NoteRoomDatabase::class.java){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, // applicationContext digunakan agar database tidak bocor ke Activity
                        NoteRoomDatabase::class.java, "note_database") // "note_database" adalah nama file .db yang tersimpan di storage internal.
                        .build()
                }
            }
            return INSTANCE as NoteRoomDatabase
        }
    }
}