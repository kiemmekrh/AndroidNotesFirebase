package com.example.notesfirebase;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private String id;
    private String title;
    private String content;
    private String userId;  // Menyimpan ID pengguna
    private List<String> sharedWith;  // Menyimpan siapa saja yang bisa mengakses catatan

    // Constructor kosong diperlukan untuk Firestore (Firebase)
    public Note() {
        sharedWith = new ArrayList<>(); // Memulai daftar akses yang kosong
    }

    // Constructor untuk membuat objek Note secara langsung
    public Note(String id, String title, String content, String userId, List<String> sharedWith) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.sharedWith = sharedWith != null ? sharedWith : new ArrayList<>(); // Jika sharedWith null, buat ArrayList kosong
    }

    // Getter dan Setter untuk id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter dan Setter untuk title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter dan Setter untuk content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter dan Setter untuk userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter dan Setter untuk sharedWith
    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith != null ? sharedWith : new ArrayList<>(); // Jika null, set ArrayList kosong
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", sharedWith=" + sharedWith +
                '}';
    }
}
