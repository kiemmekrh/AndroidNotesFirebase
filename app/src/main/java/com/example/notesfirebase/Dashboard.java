package com.example.notesfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private List<Note> notesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inisialisasi UI
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Konfigurasi RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        // Ambil catatan dari Firestore
        fetchNotes();

        // Tombol untuk menambah catatan
        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, AddNoteActivity.class);
            startActivityForResult(intent, 1);
        });

        // Tombol Logout
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            // Menangani logout
            FirebaseAuth.getInstance().signOut(); // Logout dari Firebase

            // Pindah ke halaman login
            Intent intent = new Intent(Dashboard.this, Login.class);
            startActivity(intent);
            finish(); // Pastikan Dashboard tidak muncul kembali
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            fetchNotes(); // Refresh data setelah menambah/mengedit catatan
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotes();
    }

    private void fetchNotes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ambil catatan dari `users/{userId}/notes`
        db.collection("users").document(userId).collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot userNotes = task.getResult();

                        // Bersihkan list sebelum menambahkan data baru
                        notesList.clear();

                        for (DocumentSnapshot doc : userNotes.getDocuments()) {
                            Note note = doc.toObject(Note.class);
                            if (note != null) {
                                note.setId(doc.getId());
                                notesList.add(note);
                            }
                        }

                        // Setelah selesai, ambil data dari koleksi root `notes`
                        fetchSharedNotes();
                    } else {
                        Toast.makeText(Dashboard.this, "Gagal mengambil catatan pribadi!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSharedNotes() {
        // Ambil catatan dari koleksi root `notes`
        db.collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot sharedNotes = task.getResult();

                        for (DocumentSnapshot doc : sharedNotes.getDocuments()) {
                            Note note = doc.toObject(Note.class);
                            if (note != null) {
                                note.setId(doc.getId());

                                // Hindari duplikasi berdasarkan ID
                                if (!isNoteAlreadyInList(note)) {
                                    notesList.add(note);
                                }
                            }
                        }

                        // Perbarui RecyclerView setelah semua data digabungkan
                        notesAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(Dashboard.this, "Gagal mengambil catatan yang dibagikan!", Toast.LENGTH_SHORT).show();
                    }

                    // Sembunyikan progress bar
                    progressBar.setVisibility(View.GONE);
                });
    }

    private boolean isNoteAlreadyInList(Note note) {
        for (Note existingNote : notesList) {
            if (existingNote.getId().equals(note.getId())) {
                return true; // Catatan sudah ada di list
            }
        }
        return false;
    }
}