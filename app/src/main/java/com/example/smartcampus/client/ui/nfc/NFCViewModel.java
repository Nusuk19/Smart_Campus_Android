package com.example.smartcampus.client.ui.nfc;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.DoorApi;
import com.example.smartcampus.client.data.remote.api.UnlockRequest;
import com.example.smartcampus.client.data.remote.api.UnlockResponse;
import com.example.smartcampus.client.data.repository.TagRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🆕 ViewModel для NFC операцій
 */
public class NFCViewModel extends AndroidViewModel {

    private final DoorApi doorApi;
    private final TagRepository tagRepository;
    private final MutableLiveData<UnlockState> unlockState = new MutableLiveData<>();

    public NFCViewModel(@NonNull Application application) {
        super(application);
        doorApi = RetrofitClient.get().create(DoorApi.class);
        tagRepository = new TagRepository(application);
    }

    /**
     * Відкрити двері через NFC тег
     */
    public void unlockDoor(String tagUid, String readerId) {
        unlockState.setValue(UnlockState.loading());

        UnlockRequest request = new UnlockRequest(tagUid, readerId);

        doorApi.unlockDoor(request).enqueue(new Callback<UnlockResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnlockResponse> call,
                                   @NonNull Response<UnlockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UnlockResponse result = response.body();

                    if ("OK".equals(result.status)) {
                        unlockState.postValue(UnlockState.success(result));

                        // Оновити lastUsedAt для тегу
                        // (знайти tag_id за tagUid і оновити)

                    } else if ("DENIED".equals(result.status)) {
                        unlockState.postValue(UnlockState.denied(result.message));
                    } else if ("OCCUPIED".equals(result.status)) {
                        unlockState.postValue(UnlockState.occupied());
                    }
                } else {
                    String errorMsg;
                    if (response.code() == 404) {
                        errorMsg = "Невідомий NFC тег";
                    } else if (response.code() == 403) {
                        errorMsg = "Доступ заборонено";
                    } else {
                        errorMsg = "Помилка сервера: " + response.code();
                    }
                    unlockState.postValue(UnlockState.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnlockResponse> call, @NonNull Throwable t) {
                unlockState.postValue(UnlockState.error("Немає з'єднання з сервером"));
            }
        });
    }

    public MutableLiveData<UnlockState> getUnlockState() {
        return unlockState;
    }

    /**
     * Wrapper для станів відкриття дверей
     */
    public static class UnlockState {
        public enum Status { SUCCESS, ERROR, LOADING, DENIED, OCCUPIED }

        public final Status status;
        public final UnlockResponse response;
        public final String message;

        private UnlockState(Status status, UnlockResponse response, String message) {
            this.status = status;
            this.response = response;
            this.message = message;
        }

        public static UnlockState success(UnlockResponse response) {
            return new UnlockState(Status.SUCCESS, response, null);
        }

        public static UnlockState error(String msg) {
            return new UnlockState(Status.ERROR, null, msg);
        }

        public static UnlockState loading() {
            return new UnlockState(Status.LOADING, null, null);
        }

        public static UnlockState denied(String msg) {
            return new UnlockState(Status.DENIED, null, msg);
        }

        public static UnlockState occupied() {
            return new UnlockState(Status.OCCUPIED, null, "Аудиторія зайнята");
        }
    }
}