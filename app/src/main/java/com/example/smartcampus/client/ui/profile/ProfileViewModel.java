package com.example.smartcampus.client.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.smartcampus.client.data.local.entities.UserEntity;
import com.example.smartcampus.client.data.repository.UserRepository;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * 🔄 ВИПРАВЛЕНО: ProfileViewModel
 */
public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        sessionManager = new SessionManager(application);
    }

    /**
     * ✅ ВИПРАВЛЕНО: Тепер завантажує дані з API кожного разу
     */
    public LiveData<UserEntity> getCurrentUser() {
        MutableLiveData<UserEntity> result = new MutableLiveData<>();

        // Спочатку показати з кешу (якщо є)
        long userId = sessionManager.getUserId();
        LiveData<UserEntity> cached = repository.getCurrentUser();

        // Але обов'язково завантажити з API
        repository.refreshProfile(new UserRepository.UserCallback() {
            @Override
            public void onSuccess(UserEntity user) {
                result.postValue(user);
            }

            @Override
            public void onError(String error) {
                // Якщо помилка - показати з кешу
                errorMessage.postValue(error);
            }
        });

        return Transformations.switchMap(cached, cachedUser -> {
            if (result.getValue() == null) {
                result.setValue(cachedUser);
            }
            return result;
        });
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