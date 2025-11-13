package com.example.smartcampus.client.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.smartcampus.client.data.local.dao.RoomDao;
import com.example.smartcampus.client.data.local.entities.RoomEntity;

/**
 * Room Database для Smart Campus
 *
 * ✅ ВИПРАВЛЕНО: version = 2 (було 1)
 *
 * ВАЖЛИВО:
 * - Кожна зміна структури Entity потребує збільшення версії!
 * - fallbackToDestructiveMigration() видаляє старі дані при оновленні
 */
@Database(
        entities = {RoomEntity.class},
        version = 2,  // ✅ ЗБІЛЬШЕНО з 1 → 2
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RoomDao roomDao();
}