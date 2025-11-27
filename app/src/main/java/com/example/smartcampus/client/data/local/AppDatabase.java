package com.example.smartcampus.client.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartcampus.client.data.local.dao.*;
import com.example.smartcampus.client.data.local.entities.*;

/**
 * ✅ ЗМІНЕНО: version = 3
 * ✅ ДОДАНО: UserEntity, TagEntity
 */

@Database(
        entities = {RoomEntity.class, UserEntity.class, TagEntity.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RoomDao roomDao();
    public abstract UserDao userDao();
    public abstract TagDao tagDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smartcampus"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
