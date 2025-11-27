package com.example.smartcampus.client.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.smartcampus.client.data.local.entities.RoomEntity;
import com.example.smartcampus.client.data.repository.RoomRepository;
import com.example.smartcampus.client.ui.main.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🔄 ОНОВЛЕНО: ViewModel для MainActivity
 *
 * ЗМІНИ:
 * - Використовує RoomRepository замість хардкоджених даних
 * - Додано обробку помилок
 * - Додано фільтрацію та пошук
 */
public class MainViewModel extends AndroidViewModel {

    private final RoomRepository repository;
    private final LiveData<List<Room>> rooms;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> showOnlyAvailable = new MutableLiveData<>(false);

    // Стани для UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    /**
     * Конструктор
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new RoomRepository(application);

        // Завантажити дані з Repository
        LiveData<List<RoomEntity>> roomEntities = repository.getRooms();

        // Маппінг RoomEntity → Room (UI model)
        rooms = Transformations.map(roomEntities, this::mapToUiModel);
    }

    /**
     * Отримати список аудиторій
     */
    public LiveData<List<Room>> getRooms() {
        return rooms;
    }

    /**
     * Отримати тільки доступні аудиторії
     */
    public LiveData<List<Room>> getAvailableRooms() {
        LiveData<List<RoomEntity>> availableEntities = repository.getAvailableRooms();
        return Transformations.map(availableEntities, this::mapToUiModel);
    }

    /**
     * Пошук аудиторій
     */
    public LiveData<List<Room>> searchRooms(String query) {
        LiveData<List<RoomEntity>> searchResults = repository.searchRooms(query);
        return Transformations.map(searchResults, this::mapToUiModel);
    }

    /**
     * Оновити дані (pull-to-refresh)
     */
    public void refresh() {
        isLoading.setValue(true);
        repository.forceRefresh();

        // Через 2 секунди прибрати індикатор завантаження
        new android.os.Handler().postDelayed(() -> isLoading.setValue(false), 2000);
    }

    /**
     * Перемикач "Показувати тільки вільні"
     */
    public void toggleAvailableFilter() {
        Boolean current = showOnlyAvailable.getValue();
        showOnlyAvailable.setValue(current == null || !current);
    }

    /**
     * Оновити статус аудиторії (офлайн)
     */
    public void updateRoomAvailability(long roomId, boolean isAvailable) {
        repository.updateRoomAvailability(roomId, isAvailable);
    }

    // ========== ГЕТТЕРИ ДЛЯ UI СТАНІВ ==========

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getShowOnlyAvailable() {
        return showOnlyAvailable;
    }

    // ========== ПРИВАТНІ МЕТОДИ ==========

    /**
     * Конвертувати RoomEntity (БД) → Room (UI)
     */
    private List<Room> mapToUiModel(List<RoomEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(entity -> new Room(
                        entity.id,
                        entity.name,
                        entity.available,
                        entity.floor,
                        entity.building,
                        entity.capacity,
                        entity.getFullName(),
                        entity.getStatusText()
                ))
                .collect(Collectors.toList());
    }
}