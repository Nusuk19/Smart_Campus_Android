package com.example.smartcampus.client.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartcampus.client.data.local.entities.UserEntity;
import com.example.smartcampus.client.data.repository.UserRepository;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * ✅ ВИПРАВЛЕНО: ProfileViewModel з примусовим оновленням з API
 */
public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        sessionManager = new SessionManager(application);

        // ✅ ВИПРАВЛЕНО: Завантажити профіль відразу
        loadProfile();
    }

    /**
     * ✅ НОВИЙ: Завантажити профіль з API
     */
    public void loadProfile() {
        isLoading.setValue(true);

        repository.refreshProfile(new UserRepository.UserCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                isLoading.postValue(false);
                currentUser.postValue(user);

                // Оновити SessionManager
                sessionManager.saveUser(user.id, user.email, user.name, user.role);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);

                // Fallback: завантажити з кешу
                loadFromCache();
            }
        });
    }

    /**
     * ✅ НОВИЙ: Завантажити з кешу
     */
    private void loadFromCache() {
        long userId = sessionManager.getUserId();
        repository.getCurrentUser().observeForever(cachedUser -> {
            if (cachedUser != null) {
                currentUser.postValue(cachedUser);
            }
        });
    }

    /**
     * Отримати поточного користувача
     */
    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }

    /**
     * Оновити ім'я
     */
    public void updateName(String newName) {
        isLoading.setValue(true);

        repository.updateProfile(newName, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                isLoading.postValue(false);
                successMessage.postValue("Ім'я оновлено!");
                currentUser.postValue(user);

                // Оновити в SessionManager
                sessionManager.saveUser(user.id, user.email, user.name, user.role);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Змінити пароль
     */
    public void changePassword(String oldPassword, String newPassword) {
        isLoading.setValue(true);

        repository.changePassword(oldPassword, newPassword, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                isLoading.postValue(false);
                successMessage.postValue("Пароль змінено!");
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public LiveData<Boolean> getLoadingState() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
}