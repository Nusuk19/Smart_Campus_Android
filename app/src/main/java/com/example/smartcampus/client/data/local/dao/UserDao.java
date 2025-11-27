package com.example.smartcampus.client.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.smartcampus.client.data.local.entities.UserEntity;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserById(long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteById(long userId);

    @Query("DELETE FROM users")
    void deleteAll();
}