package com.example.notesfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, etSharedEmail;
    private CheckBox cbShareNote;
    private ImageView saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String noteId = null; // To check if it's an update
    private boolean isEditMode = false; // Flag to track mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        etSharedEmail = findViewById(R.id.etSharedEmail);
        cbShareNote = findViewById(R.id.cbShareNote);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check if we are in Edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("noteId")) {
            isEditMode = true;
            noteId = intent.getStringExtra("noteId");

            // Fetch the note from Firestore to pre-fill the fields
            db.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .collection("notes")
                    .document(noteId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String title = documentSnapshot.getString("title");
                            String content = documentSnapshot.getString("content");

                            titleEditText.setText(title);
                            contentEditText.setText(content);

                            Log.d("UpdateDebug", "Note ID received: " + noteId);
                        } else {
                            Toast.makeText(this, "Catatan tidak ditemukan!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal mengambil catatan!", Toast.LENGTH_SHORT).show();
                    });
        }

        // Show/hide email input based on checkbox state
        cbShareNote.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etSharedEmail.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Save button logic
        saveButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String sharedEmail = etSharedEmail.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUserId(userId);

        if (isEditMode && noteId != null) {
            // Perbarui catatan yang ada
            db.collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(noteId)
                    .update("title", title, "content", content)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Catatan berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                        handleSharing(note, sharedEmail);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal memperbarui catatan!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Simpan catatan berdasarkan status checkbox
            if (cbShareNote.isChecked()) {
                // Jika checkbox dicentang, simpan ke koleksi "notes"
                db.collection("notes")
                        .add(note)
                        .addOnSuccessListener(documentReference -> {
                            note.setId(documentReference.getId());
                            Toast.makeText(this, "Catatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal menambahkan catatan!", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Jika checkbox tidak dicentang, simpan ke koleksi "user/notes"
                db.collection("users")
                        .document(userId)
                        .collection("notes")
                        .add(note)
                        .addOnSuccessListener(documentReference -> {
                            note.setId(documentReference.getId());
                            Toast.makeText(this, "Catatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal menambahkan catatan!", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void handleSharing(Note note, String sharedEmail) {
        if (cbShareNote.isChecked()) {
            // Jika checkbox dicentang, simpan catatan ke koleksi "notes"
            String userId = auth.getCurrentUser().getUid();
            db.collection("notes")
                    .document(userId)
                    .collection("notes")
                    .document(note.getId())
                    .set(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Catatan berhasil disimpan!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menyimpan catatan!", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}