package com.example.notesfirebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private DatabaseReference mDatabase;

    public DatabaseHelper() {
        // Menginisialisasi referensi database Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // Menambahkan user baru ke Firebase
    public void addUser(String username, String password) {
        String userId = mDatabase.child("users").push().getKey();
        User user = new User(username, password);
        if (userId != null) {
            mDatabase.child("users").child(userId).setValue(user);
        }
    }

    // Mengecek login pengguna
    public void checkLogin(String username, String password, final LoginCallback callback) {
        mDatabase.child("users").orderByChild("username").equalTo(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Cek password
                        boolean loginSuccess = false;
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            String storedPassword = snapshot.child("password").getValue(String.class);
                            if (storedPassword != null && storedPassword.equals(password)) {
                                loginSuccess = true;
                                break;
                            }
                        }
                        callback.onResult(loginSuccess);
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    // Menambahkan catatan baru
    public void insertNote(Note note) {
        String noteId = mDatabase.child("notes").push().getKey();
        if (noteId != null) {
            note.setId(noteId); // Set ID di dalam objek Note
            mDatabase.child("notes").child(noteId).setValue(note);
        }
    }

    // Mengambil semua catatan
    public void getAllNotes(final NotesCallback callback) {
        mDatabase.child("notes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                List<Note> notesList = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Note note = snapshot.getValue(Note.class);
                    if (note != null) {
                        notesList.add(note);
                    }
                }
                callback.onNotesFetched(notesList);
            } else {
                callback.onNotesFetched(new ArrayList<>());
            }
        });
    }

    // Menghapus catatan berdasarkan ID
    public void deleteNote(String noteId) {
        mDatabase.child("notes").child(noteId).removeValue();
    }

    // Mengupdate catatan
    public void updateNote(Note note) {
        String noteId = note.getId();
        if (noteId != null) {
            mDatabase.child("notes").child(noteId).setValue(note);
        }
    }

    // Interface untuk callback login
    public interface LoginCallback {
        void onResult(boolean success);
    }

    // Interface untuk callback catatan
    public interface NotesCallback {
        void onNotesFetched(List<Note> notes);
    }
}
