package com.example.smartcampus.client.ui.tags;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import com.example.smartcampus.client.data.repository.TagRepository;
import java.util.List;

/**
 * ✅ ВИПРАВЛЕНО: ViewModel з примусовим завантаженням
 */
public class TagViewModel extends AndroidViewModel {

    private static final String TAG = "TagViewModel";

    private final TagRepository repository;
    private final MutableLiveData<List<TagEntity>> tags = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public TagViewModel(@NonNull Application application) {
        super(application);
        repository = new TagRepository(application);

        // ✅ ВИПРАВЛЕНО: Завантажити теги відразу
        loadTags();
    }

    /**
     * ✅ НОВИЙ: Завантажити теги з API
     */
    public void loadTags() {
        Log.d(TAG, "🔄 Loading tags from API...");
        isLoading.setValue(true);

        // Спостерігаємо за змінами в БД
        LiveData<List<TagEntity>> tagsLiveData = repository.getMyTags();
        tagsLiveData.observeForever(tagList -> {
            Log.d(TAG, "📊 Tags updated: " + (tagList != null ? tagList.size() : 0));
            tags.postValue(tagList);
            isLoading.postValue(false);
        });
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
        Log.d(TAG, "🏷️ Creating virtual tag: " + name);
        isLoading.setValue(true);

        repository.createVirtualTag(name, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег '" + tag.name + "' створено!");
                Log.d(TAG, "✅ Tag created: " + tag.id);

                // ✅ ВИПРАВЛЕНО: Оновити список
                loadTags();
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                Log.e(TAG, "❌ Failed to create tag: " + error);
            }
        });
    }

    /**
     * Видалити тег
     */
    public void deleteTag(long tagId) {
        Log.d(TAG, "🗑️ Deleting tag: " + tagId);
        isLoading.setValue(true);

        repository.deleteTag(tagId, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег видалено");
                Log.d(TAG, "✅ Tag deleted");

                // ✅ ВИПРАВЛЕНО: Оновити список
                loadTags();
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                Log.e(TAG, "❌ Failed to delete tag: " + error);
            }
        });
    }

    /**
     * Перейменувати тег
     */
    public void renameTag(long tagId, String newName) {
        Log.d(TAG, "✏️ Renaming tag " + tagId + " to: " + newName);
        isLoading.setValue(true);

        repository.renameTag(tagId, newName, new TagRepository.TagCallback() {
            @Override
            public void onSuccess(TagEntity tag) {
                isLoading.postValue(false);
                successMessage.postValue("Тег перейменовано");
                Log.d(TAG, "✅ Tag renamed");

                // ✅ ВИПРАВЛЕНО: Оновити список
                loadTags();
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                Log.e(TAG, "❌ Failed to rename tag: " + error);
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
