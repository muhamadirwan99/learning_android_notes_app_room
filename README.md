# 📝 MyNoteAppsRoom

Aplikasi catatan sederhana untuk Android yang dibangun menggunakan **Android Room Database** dengan arsitektur **MVVM (Model-View-ViewModel)**.

---

## 🏗️ Arsitektur Proyek

Proyek ini mengikuti pola arsitektur **MVVM + Repository**, yang membagi tanggung jawab kode menjadi lapisan-lapisan yang jelas:

```
UI Layer          →  Activity / Adapter
ViewModel Layer   →  MainViewModel / NoteAddUpdateViewModel
Repository Layer  →  NoteRepository
Data Layer        →  Room Database (NoteDao, NoteRoomDatabase, Note)
```

### Kenapa MVVM?
- **Activity** hanya mengurus tampilan (UI), bukan logika data.
- **ViewModel** bertahan saat rotasi layar — data tidak hilang.
- **Repository** menjadi satu pintu masuk ke semua sumber data.
- **LiveData** memperbarui UI otomatis saat data berubah, tanpa polling manual.

---

## 📁 Struktur File

```
app/src/main/java/com/dicoding/mynoteappsroom/
│
├── database/
│   ├── Note.kt                  → Entity (model tabel database)
│   ├── NoteDao.kt               → Data Access Object (query SQL)
│   └── NoteRoomDatabase.kt      → Konfigurasi & Singleton database
│
├── helper/
│   ├── DateHelper.kt            → Utilitas untuk mendapatkan tanggal saat ini
│   ├── NoteDiffCallback.kt      → Kalkulasi diff untuk efisiensi RecyclerView
│   └── ViewModelFactory.kt      → Factory untuk membuat ViewModel dengan parameter
│
├── repository/
│   └── NoteRepository.kt        → Jembatan antara ViewModel dan database
│
└── ui/
    ├── main/
    │   ├── MainActivity.kt      → Layar utama, menampilkan daftar catatan
    │   ├── MainViewModel.kt     → ViewModel untuk layar utama
    │   └── NoteAdapter.kt       → Adapter RecyclerView untuk item catatan
    │
    └── insert/
        ├── NoteAddUpdateActivity.kt    → Layar tambah/edit/hapus catatan
        └── NoteAddUpdateViewModel.kt   → ViewModel untuk operasi CRUD catatan
```

---

## 🔑 Konsep Penting

### 1. `Note.kt` — Entity & Parcelable
- **`@Entity`** → Kelas ini direpresentasikan sebagai tabel di SQLite.
- **`@PrimaryKey(autoGenerate = true)`** → ID dibuat otomatis oleh Room (auto-increment), tidak perlu diisi manual.
- **`@Parcelize`** → Memungkinkan objek `Note` dikirim antar Activity lewat `Intent` tanpa menulis serialisasi manual.

### 2. `NoteDao.kt` — Data Access Object
- **`@Dao`** → Interface ini dikompilasi oleh Room menjadi implementasi SQL nyata secara otomatis.
- **`OnConflictStrategy.IGNORE`** → Mencegah crash jika ada data duplikat; data lama dipertahankan.
- **Return `LiveData`** → Query `getAllNotes()` mengembalikan `LiveData` sehingga UI otomatis diperbarui setiap ada perubahan data.

### 3. `NoteRoomDatabase.kt` — Singleton Database
- **`@Volatile`** → Nilai `INSTANCE` selalu dibaca dari memori utama (bukan cache CPU) — aman untuk multi-thread.
- **Double-Checked Locking** → Pola `if (null) { synchronized { if (null) { create } } }` memastikan hanya satu instance database yang pernah dibuat, bahkan dari banyak thread sekaligus.
- **`context.applicationContext`** → Mencegah memory leak; database tidak menyimpan referensi ke Activity/Fragment.

### 4. `NoteRepository.kt` — Repository Pattern
- **`ExecutorService` (single thread)** → Operasi `insert`, `update`, `delete` **wajib** dijalankan di background thread. Single thread juga memastikan operasi berjalan berurutan, menghindari konflik data.
- **`getAllNotes()` tanpa Executor** → Room sudah menangani background thread untuk query yang mengembalikan `LiveData` secara internal.

### 5. `ViewModelFactory.kt` — Custom Factory
- ViewModel yang butuh parameter di constructor (seperti `Application`) tidak bisa dibuat oleh `ViewModelProvider` secara default — Factory ini menjadi solusinya.
- Menggunakan pola **Singleton** dengan `@Volatile` + `synchronized` yang sama seperti `NoteRoomDatabase`.

### 6. `NoteDiffCallback.kt` — Efisiensi RecyclerView
- **`areItemsTheSame()`** → Cek identitas item lewat `id` (tahap 1).
- **`areContentsTheSame()`** → Cek isi item; hanya dijalankan jika tahap 1 lolos (tahap 2).
- Hasil kalkulasi `DiffUtil` dikirim via `dispatchUpdatesTo()` → RecyclerView hanya me-render ulang item yang **benar-benar berubah**, bukan seluruh list. Jauh lebih efisien dari `notifyDataSetChanged()`.

### 7. `NoteAddUpdateActivity.kt` — Dual-Mode Activity
- Flag **`isEdit`** menentukan mode (tambah vs. edit) berdasarkan ada/tidaknya data `Note` di Intent.
- **`Build.VERSION_CODES.TIRAMISU`** → Cara mengambil Parcelable berbeda di Android 13+; pengecekan ini menjaga kompatibilitas ke perangkat lama.
- **Tanggal hanya diset saat INSERT** → Saat mode edit, tanggal tidak diubah agar mencerminkan waktu pembuatan asli catatan.
- **`OnBackPressedCallback`** → Pengganti modern `onBackPressed()` yang deprecated; mencegat tombol Back untuk menampilkan konfirmasi.

---

## 🛠️ Teknologi yang Digunakan

| Teknologi | Fungsi |
|---|---|
| **Room** | ORM database lokal (abstraksi di atas SQLite) |
| **LiveData** | Observable data holder — memperbarui UI otomatis |
| **ViewModel** | Menyimpan data UI yang bertahan saat rotasi layar |
| **ViewBinding** | Akses view yang type-safe tanpa `findViewById` |
| **Kotlin Parcelize** | Serialisasi objek untuk dikirim via `Intent` |
| **DiffUtil** | Kalkulasi perbedaan list yang efisien untuk RecyclerView |
| **ExecutorService** | Menjalankan operasi database di background thread |

---

## 🔄 Alur Data (Data Flow)

```
User Action (klik Simpan)
        ↓
NoteAddUpdateActivity  →  memanggil  →  NoteAddUpdateViewModel
        ↓
NoteAddUpdateViewModel  →  memanggil  →  NoteRepository
        ↓
NoteRepository  →  executorService.execute { NoteDao.insert() }
        ↓
Room Database (SQLite) — data tersimpan
        ↓
LiveData (NoteDao.getAllNotes()) — otomatis terpancar
        ↓
MainActivity.observe()  →  adapter.setListNotes()  →  RecyclerView diperbarui
```

---

## 🚀 Cara Menjalankan

1. Clone repositori ini.
2. Buka dengan **Android Studio**.
3. Sync Gradle: `File → Sync Project with Gradle Files`.
4. Jalankan di emulator atau perangkat fisik (min. API 21).

---

## 📌 Catatan Developer

- Jika mengubah struktur tabel `Note` (menambah/menghapus kolom), **wajib naikkan `version`** di `@Database` dan sediakan `Migration` — jika tidak, aplikasi akan crash saat upgrade.
- Semua operasi write ke database **TIDAK BOLEH** dijalankan di Main Thread. Selalu gunakan `ExecutorService`, `Coroutine`, atau `AsyncTask` (deprecated).
- `ViewModelFactory` perlu diperbarui setiap kali ada ViewModel baru yang membutuhkan parameter di constructor.

