package com.example.notesfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private FirebaseFirestore db;
    private List<Note> notesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inisialisasi RecyclerView
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();
        notesList = new ArrayList<>();

        // Inisialisasi adapter
        notesAdapter = new NotesAdapter(notesList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        // Ambil catatan dari Firestore
        fetchNotes();

        // Tombol untuk menambah catatan
        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, AddNoteActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            fetchNotes(); // Refresh data dari Firestore setelah kembali dari AddNoteActivity
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotes();
    }

    private void fetchNotes() {
        db.collection("notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot documents = task.getResult();
                        notesList.clear(); // Kosongkan list sebelum menambahkan data baru
                        for (DocumentSnapshot document : documents.getDocuments()) {
                            Note note = document.toObject(Note.class);
                            if (note != null) {
                                note.setId(document.getId()); // Simpan ID dokumen Firestore
                                notesList.add(note);
                            }
                        }
                        notesAdapter.notifyDataSetChanged(); // Perbarui RecyclerView
                    } else {
                        Toast.makeText(Dashboard.this, "Gagal mengambil data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
