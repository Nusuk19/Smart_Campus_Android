package com.example.smartcampus.client.ui.tags;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import com.example.smartcampus.client.data.repository.TagRepository;
import java.util.List;

/**
 * 🆕 ViewModel для управління NFC тегами
 */
public class TagViewModel extends AndroidViewModel {

    private final TagRepository repository;
    private final LiveData<List<TagEntity>> tags;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public TagViewModel(@NonNull Application application) {
        super(application);
        repository = new TagRepository(application);
        tags = repository.getMyTags();
    }

    /**
     * Отримати список тегів
     */
    public LiveData<List<TagEntity>> getTags() {
        return tags;
    }

    /**
     * Створити віртуальний тег
     */
    public void createVirtualTag(String name) {
        isLoading.setValue(true);

        repository.createVirtualTag(name, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег '" + tag.name + "' створено!");
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Видалити тег
     */
    public void deleteTag(long tagId) {
        isLoading.setValue(true);

        repository.deleteTag(tagId, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег видалено");
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Перейменувати тег
     */
    public void renameTag(long tagId, String newName) {
        isLoading.setValue(true);

        repository.renameTag(tagId, newName, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег перейменовано");
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    // Геттери
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
