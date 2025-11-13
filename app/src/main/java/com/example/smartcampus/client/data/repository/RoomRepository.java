package com.example.smartcampus.client.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.room.Room;

import com.example.smartcampus.client.data.local.AppDatabase;
import com.example.smartcampus.client.data.local.dao.RoomDao;
import com.example.smartcampus.client.data.local.entities.RoomEntity;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.RoomApi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository Pattern - єдине джерело правди для даних про аудиторії
 *
 * ЩО ВІН РОБИТЬ:
 * 1. Отримує дані з API (онлайн)
 * 2. Зберігає у локальну БД (кеш)
 * 3. Повертає дані з кешу (офлайн)
 * 4. Синхронізує при появі інтернету
 *
 * ПРИСУТНє:
 *  NetworkBoundResource pattern
 *  Перевірка свіжості даних
 *  Правильна робота в фоновому потоці
 *  Обробка помилок
 */
public class RoomRepository {

    private static final String TAG = "RoomRepository";
    private final RoomDao dao;
    private final RoomApi api;
    private final ExecutorService executor;

    /**
     * Конструктор
     * ВИКОРИСТАННЯ:
     * RoomRepository repo = new RoomRepository(getApplication());
     */
    public RoomRepository(Application app) {
        AppDatabase db = Room.databaseBuilder(app, AppDatabase.class, "smartcampus-db")
                .fallbackToDestructiveMigration() // Видаляє БД при зміні версії
                .build();
        this.dao = db.roomDao();
        this.api = RetrofitClient.get().create(RoomApi.class);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ========== ОРИГІНАЛЬНИЙ МЕТОД (з виправленнями) ==========

    /**
     * Отримати список аудиторій (ОРИГІНАЛЬНА ЛОГІКА)
     *
     * ЛОГІКА:
     * 1. Спробувати завантажити з API
     * 2. Якщо успішно → зберегти в БД і показати
     * 3. Якщо помилка → показати з БД
     *
     * ПРОБЛЕМА: Завжди робить мережевий запит, навіть якщо дані свіжі
     */
    public LiveData<List<RoomEntity>> getRooms() {
        MediatorLiveData<List<RoomEntity>> result = new MediatorLiveData<>();

        // Спочатку показуємо кешовані дані (швидко)
        LiveData<List<RoomEntity>> cachedData = dao.getAllLive();
        result.addSource(cachedData, result::setValue);

        // Потім намагаємось оновити з мережі
        api.getRooms().enqueue(new Callback<List<RoomEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<RoomEntity>> call,
                                   @NonNull Response<List<RoomEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Loaded " + response.body().size() + " rooms from API");

                    // Зберігаємо в БД (у фоновому потоці)
                    executor.execute(() -> {
                        dao.deleteAll(); // Очищаємо старі дані
                        dao.insertAll(response.body());
                        Log.d(TAG, "Saved rooms to database");
                    });
                } else {
                    Log.w(TAG, "API response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RoomEntity>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load rooms from API: " + t.getMessage());
                // Дані з БД вже показані через cachedData
            }
        });

        return result;
    }

    // ========== ПОКРАЩЕНІ МЕТОДИ ==========

    /**
     * ✅ КРАЩЕ: NetworkBoundResource pattern
     *
     * ЛОГІКА:
     * 1. Перевіряємо чи є дані в БД
     * 2. Якщо є і свіжі (<5 хв) → показуємо їх
     * 3. Якщо немає/застарілі → завантажуємо з API
     * 4. Якщо API недоступне → показуємо старі дані
     */
    public LiveData<Resource<List<RoomEntity>>> getRoomsOptimized() {
        return new NetworkBoundResource<List<RoomEntity>, List<RoomEntity>>() {

            @Override
            protected void saveCallResult(@NonNull List<RoomEntity> items) {
                executor.execute(() -> {
                    dao.deleteAll();

                    // Оновлюємо timestamp
                    for (RoomEntity room : items) {
                        room.markAsUpdated();
                    }

                    dao.insertAll(items);
                    Log.d(TAG, "Saved " + items.size() + " rooms");
                });
            }

            @Override
            protected boolean shouldFetch(List<RoomEntity> data) {
                // Завантажувати з мережі якщо:
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "No cached data, fetching from network");
                    return true;
                }

                // Перевіряємо чи дані застарілі
                boolean isStale = data.get(0).isStale();
                Log.d(TAG, "Data is " + (isStale ? "stale" : "fresh"));
                return isStale;
            }

            @NonNull
            @Override
            protected LiveData<List<RoomEntity>> loadFromDb() {
                return dao.getAllLive();
            }

            @NonNull
            @Override
            protected Call<List<RoomEntity>> createCall() {
                return api.getRooms();
            }
        }.asLiveData();
    }

    /**
     * Отримати тільки доступні аудиторії
     */
    public LiveData<List<RoomEntity>> getAvailableRooms() {
        return dao.getAvailableRooms();
    }

    /**
     * Знайти аудиторію за ID
     */
    public LiveData<RoomEntity> getRoomById(long id) {
        return dao.getRoomById(id);
    }

    /**
     * Пошук аудиторій за назвою
     */
    public LiveData<List<RoomEntity>> searchRooms(String query) {
        return dao.searchByName(query);
    }

    /**
     * Оновити статус аудиторії (офлайн)
     * ВИКОРИСТАННЯ: Коли викладач закриває/відкриває аудиторію без інтернету
     */
    public void updateRoomAvailability(long roomId, boolean isAvailable) {
        executor.execute(() -> {
            dao.updateAvailability(roomId, isAvailable);
            Log.d(TAG, "Updated room " + roomId + " availability to " + isAvailable);
        });
    }

    /**
     * Форсувати синхронізацію з сервером
     * ВИКОРИСТАННЯ: Кнопка "Оновити" або pull-to-refresh
     */
    public void forceRefresh() {
        api.getRooms().enqueue(new Callback<List<RoomEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<RoomEntity>> call,
                                   @NonNull Response<List<RoomEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        dao.deleteAll();
                        dao.insertAll(response.body());
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RoomEntity>> call, @NonNull Throwable t) {
                Log.e(TAG, "Force refresh failed: " + t.getMessage());
            }
        });
    }

    /**
     * Очистити кеш
     */
    public void clearCache() {
        executor.execute(dao::deleteAll);
    }
}

/**
 * Wrapper для станів завантаження
 */
class Resource<T> {
    public enum Status { SUCCESS, ERROR, LOADING }

    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }
}

/**
 * NetworkBoundResource - абстрактний клас для offline-first паттерну
 */
abstract class NetworkBoundResource<ResultType, RequestType> {

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NetworkBoundResource() {
        result.setValue(Resource.loading(null));

        LiveData<ResultType> dbSource = loadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);

            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            } else {
                result.addSource(dbSource, newData ->
                        result.setValue(Resource.success(newData)));
            }
        });
    }

    private void fetchFromNetwork(LiveData<ResultType> dbSource) {
        result.addSource(dbSource, newData ->
                result.setValue(Resource.loading(newData)));

        createCall().enqueue(new Callback<RequestType>() {
            @Override
            public void onResponse(@NonNull Call<RequestType> call,
                                   @NonNull Response<RequestType> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        saveCallResult(response.body());
                        result.postValue(Resource.success(null));
                    });
                } else {
                    result.addSource(loadFromDb(), newData ->
                            result.setValue(Resource.error("API error", newData)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequestType> call, @NonNull Throwable t) {
                result.addSource(loadFromDb(), newData ->
                        result.setValue(Resource.error(t.getMessage(), newData)));
            }
        });
    }

    protected abstract void saveCallResult(@NonNull RequestType item);
    protected abstract boolean shouldFetch(ResultType data);
    @NonNull protected abstract LiveData<ResultType> loadFromDb();
    @NonNull protected abstract Call<RequestType> createCall();

    public LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }
}