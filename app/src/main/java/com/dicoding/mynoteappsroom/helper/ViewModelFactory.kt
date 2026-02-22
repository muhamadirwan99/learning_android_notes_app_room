package com.dicoding.mynoteappsroom.helper

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.mynoteappsroom.ui.insert.NoteAddUpdateViewModel
import com.dicoding.mynoteappsroom.ui.main.MainViewModel

// ViewModelFactory diperlukan karena ViewModel yang membutuhkan parameter di constructor
// (seperti Application) tidak bisa dibuat oleh ViewModelProvider secara default.
// Factory ini bertindak sebagai "pabrik" yang tahu cara membuat ViewModel dengan parameter tersebut.
class ViewModelFactory private constructor(private val mApplication: Application) :
    ViewModelProvider.NewInstanceFactory() {
    companion object {
        // @Volatile + synchronized = pola Singleton yang aman untuk multi-thread.
        // Kita hanya butuh satu instance Factory di seluruh aplikasi.
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(application: Application): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(application)
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }

    // @Suppress("UNCHECKED_CAST") diperlukan karena kita melakukan cast generic (T),
    // tapi cast ini aman karena sudah divalidasi dengan isAssignableFrom sebelumnya.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // isAssignableFrom mengecek apakah modelClass yang diminta adalah MainViewModel
        // atau subclass-nya — lebih aman daripada perbandingan langsung dengan ==.
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mApplication) as T
        } else if (modelClass.isAssignableFrom(NoteAddUpdateViewModel::class.java)) {
            return NoteAddUpdateViewModel(mApplication) as T
        }
        // Lempar exception jika ada ViewModel baru yang belum didaftarkan di sini,
        // sehingga developer langsung sadar ada yang kurang daripada silent fail.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}