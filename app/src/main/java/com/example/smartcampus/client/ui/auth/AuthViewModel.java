package com.example.smartcampus.client.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartcampus.client.data.remote.api.AuthResponse;
import com.example.smartcampus.client.data.repository.AuthRepository;

/**
 * 🔄 ОНОВЛЕНО: ViewModel з підтримкою ролі
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<AuthState> loginState = new MutableLiveData<>();
    private final MutableLiveData<AuthState> registerState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    /**
     * Вхід через Email + Password
     */
    public void login(String email, String password) {
        loginState.setValue(AuthState.loading());

        repository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse.UserData user) {
                loginState.postValue(AuthState.success(user));
            }

            @Override
            public void onError(String error) {
                loginState.postValue(AuthState.error(error));
            }
        });
    }

    /**
     * Вхід через Google SSO
     */
    public void loginWithGoogle(String googleIdToken) {
        loginState.setValue(AuthState.loading());

        repository.loginWithGoogle(googleIdToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse.UserData user) {
                loginState.postValue(AuthState.success(user));
            }

            @Override
            public void onError(String error) {
                loginState.postValue(AuthState.error(error));
            }
        });
    }

    /**
     * ✅ ОНОВЛЕНО: Реєстрація з роллю
     */
    public void register(String email, String password, String name, String role) {
        registerState.setValue(AuthState.loading());

        repository.register(email, password, name, role, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse.UserData user) {
                registerState.postValue(AuthState.success(user));
            }

            @Override
            public void onError(String error) {
                registerState.postValue(AuthState.error(error));
            }
        });
    }

    /**
     * Вийти
     */
    public void logout() {
        repository.logout();
    }

    /**
     * Чи користувач залогінений?
     */
    public boolean isLoggedIn() {
        return repository.isLoggedIn();
    }

    // Геттери для LiveData
    public LiveData<AuthState> getLoginState() {
        return loginState;
    }

    public LiveData<AuthState> getRegisterState() {
        return registerState;
    }

    /**
     * Wrapper для станів автентифікації
     */
    public static class AuthState {
        public enum Status { SUCCESS, ERROR, LOADING }

        public final Status status;
        public final AuthResponse.UserData user;
        public final String message;

        private AuthState(Status status, AuthResponse.UserData user, String message) {
            this.status = status;
            this.user = user;
            this.message = message;
        }

        public static AuthState success(AuthResponse.UserData user) {
            return new AuthState(Status.SUCCESS, user, null);
        }

        public static AuthState error(String msg) {
            return new AuthState(Status.ERROR, null, msg);
        }

        public static AuthState loading() {
            return new AuthState(Status.LOADING, null, null);
        }
    }
}
