package com.example.smartcampus.client.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartcampus.client.data.local.entities.RoomEntity;

import java.util.List;

/**
 * DAO (Data Access Object) для роботи з таблицею rooms.
 *
 * ЩО ВІН РОБИТЬ:
 * - Надає методи для CRUD операцій з локальною базою даних
 * - Room автоматично генерує реалізацію цього інтерфейсу
 * - Всі запити виконуються асинхронно (окрім LiveData)
 *
 * ВИКОРИСТАННЯ:
 * RoomDao dao = database.roomDao();
 * dao.insert(room); // Додати кімнату
 * List<RoomEntity> rooms = dao.getAll(); // Отримати всі
 */
@Dao
public interface RoomDao {

    /**
     * Отримати всі кімнати з БД
     * ПРОБЛЕМА: Блокує потік! Треба викликати в фоновому потоці
     */
    @Query("SELECT * FROM rooms")
    List<RoomEntity> getAll();

    /**
     * Вставити список кімнат (замінити при конфлікті)
     * OnConflictStrategy.REPLACE - якщо ID вже існує, замінить запис
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomEntity> rooms);

    /**
     * Вставити одну кімнату
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomEntity room);

    @Query("SELECT * FROM rooms ORDER BY name ASC")
    LiveData<List<RoomEntity>> getAllLive();

    /**
     * Отримати тільки доступні кімнати
     * ВИКОРИСТАННЯ: Показати студенту де він може зайти зараз
     */
    @Query("SELECT * FROM rooms WHERE available = 1 ORDER BY name ASC")
    LiveData<List<RoomEntity>> getAvailableRooms();

    /**
     * Знайти кімнату за ID
     * ВИКОРИСТАННЯ: Перевірити деталі конкретної аудиторії
     */
    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    LiveData<RoomEntity> getRoomById(long roomId);

    /**
     * Знайти кімнату за назвою
     * ВИКОРИСТАННЯ: Пошук "305" → знайти аудиторію 305
     */
    @Query("SELECT * FROM rooms WHERE name LIKE '%' || :name || '%'")
    LiveData<List<RoomEntity>> searchByName(String name);

    /**
     * Оновити статус доступності кімнати
     * ВИКОРИСТАННЯ: Коли викладач закриває/відкриває аудиторію
     */
    @Query("UPDATE rooms SET available = :isAvailable WHERE id = :roomId")
    void updateAvailability(long roomId, boolean isAvailable);

    /**
     * Видалити всі кімнати (для синхронізації)
     * ВИКОРИСТАННЯ: Очистити перед повним оновленням з сервера
     */
    @Query("DELETE FROM rooms")
    void deleteAll();

    /**
     * Видалити конкретну кімнату
     */
    @Delete
    void delete(RoomEntity room);

    /**
     * Оновити кімнату
     */
    @Update
    void update(RoomEntity room);

    /**
     * Підрахувати кількість доступних кімнат
     * ВИКОРИСТАННЯ: Показати "Вільно: 12 аудиторій"
     */
    @Query("SELECT COUNT(*) FROM rooms WHERE available = 1")
    LiveData<Integer> getAvailableCount();

    /**
     * Перевірити чи існує кімната з таким ID
     * ВИКОРИСТАННЯ: Валідація перед операціями
     */
    @Query("SELECT EXISTS(SELECT 1 FROM rooms WHERE id = :roomId)")
    boolean exists(long roomId);

    /**
     * Отримати кімнати за поверхом (коли додаси floor в Entity)
     * ВИКОРИСТАННЯ: Показати "Всі аудиторії 3-го поверху"
     */
    @Query("SELECT * FROM rooms WHERE floor = :floor ORDER BY name ASC")
    LiveData<List<RoomEntity>> getRoomsByFloor(int floor);
}