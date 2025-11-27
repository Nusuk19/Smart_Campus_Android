package com.example.smartcampus.client.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import java.util.List;

@Dao
public interface TagDao {

    /**
     * Отримати всі теги поточного користувача
     */
    @Query("SELECT * FROM tags WHERE user_id = :userId AND is_active = 1 ORDER BY created_at DESC")
    LiveData<List<TagEntity>> getTagsByUser(long userId);

    /**
     * Знайти тег за UID
     */
    @Query("SELECT * FROM tags WHERE tag_uid = :tagUid LIMIT 1")
    TagEntity getTagByUid(String tagUid);

    /**
     * Оновити час останнього використання
     */
    @Query("UPDATE tags SET last_used_at = :timestamp WHERE id = :tagId")
    void updateLastUsed(long tagId, long timestamp);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TagEntity tag);

    @Update
    void update(TagEntity tag);

    @Delete
    void delete(TagEntity tag);

    @Query("DELETE FROM tags WHERE user_id = :userId")
    void deleteByUser(long userId);
}