package com.example.notesfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText;
    private ImageView saveButton;
    private FirebaseFirestore db;

    private String noteId = null; // To check if it's an update
    private boolean isEditMode = false; // Flag to track mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Check if we are in Edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("noteId")) {
            isEditMode = true;
            noteId = intent.getStringExtra("noteId");

            // Fetch the note from Firestore to pre-fill the fields
            db.collection("notes").document(noteId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Set the retrieved data into the EditText fields
                            String title = documentSnapshot.getString("title");
                            String content = documentSnapshot.getString("content");

                            titleEditText.setText(title);
                            contentEditText.setText(content);

                            // Log the noteId to confirm it's received correctly
                            Log.d("UpdateDebug", "Note ID received: " + noteId);
                        } else {
                            Toast.makeText(this, "Catatan tidak ditemukan!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal mengambil catatan!", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error fetching note: " + e.getMessage());
                    });
        } else {
            Log.d("UpdateDebug", "No noteId passed, adding new note.");
        }

        // Save button logic
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();

            if (!title.isEmpty() && !content.isEmpty()) {
                if (isEditMode && noteId != null && !noteId.isEmpty()) {
                    // Log the noteId being used to update the note
                    Log.d("UpdateDebug", "Updating note with ID: " + noteId);

                    // Update existing note if noteId is valid
                    db.collection("notes").document(noteId)
                            .update("title", title, "content", content)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Catatan berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal memperbarui catatan!", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Add new note if no noteId or invalid noteId
                    Note note = new Note();
                    note.setTitle(title);
                    note.setContent(content);

                    db.collection("notes")
                            .add(note)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Catatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal menambahkan catatan!", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
