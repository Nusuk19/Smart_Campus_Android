package com.example.smartcampus.client.ui.tags;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * ✅ ВИПРАВЛЕНО: MyTagsActivity з правильними observers
 */
public class MyTagsActivity extends AppCompatActivity {

    private static final String TAG = "MyTagsActivity";

    private TagViewModel viewModel;
    private TagAdapter adapter;

    // Views
    private RecyclerView recyclerView;
    private FloatingActionButton addTagButton;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tags);

        Log.d(TAG, "✅ onCreate started");

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.tags_recycler);
        addTagButton = findViewById(R.id.add_tag_button);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupRecyclerView() {
        adapter = new TagAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Клік на тег
        adapter.setOnItemClickListener(tag -> {
            Log.d(TAG, "🔘 Tag clicked: " + tag.name);
            showTagDetails(tag);
        });

        // Довге натискання → видалення
        adapter.setOnItemLongClickListener(tag -> {
            Log.d(TAG, "🔘 Tag long-clicked: " + tag.name);
            showDeleteDialog(tag);
            return true;
        });

        Log.d(TAG, "✅ RecyclerView configured");
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TagViewModel.class);

        // ✅ ВИПРАВЛЕНО: Правильні observers
        viewModel.getTags().observe(this, tags -> {
            Log.d(TAG, "📊 Tags received: " + (tags != null ? tags.size() : 0));

            if (tags != null && !tags.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
                adapter.setTags(tags);
                Log.d(TAG, "✅ Showing " + tags.size() + " tags");
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
                Log.d(TAG, "📭 No tags found");
            }
        });

        viewModel.getLoadingState().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            Log.d(TAG, isLoading ? "⏳ Loading..." : "✅ Loading complete");
        });

        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "✅ Success: " + message);
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "❌ Error: " + message);
            }
        });

        Log.d(TAG, "✅ ViewModel configured");
    }

    private void setupListeners() {
        addTagButton.setOnClickListener(v -> {
            Log.d(TAG, "➕ Add tag button clicked");
            showCreateVirtualTagDialog();
        });
    }

    /**
     * Діалог створення віртуального тегу
     */
    private void showCreateVirtualTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Створити віртуальний тег");

        // Input для назви
        final EditText input = new EditText(this);
        input.setHint("Назва (наприклад: Мій телефон)");
        builder.setView(input);

        builder.setPositiveButton("Створити", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Введіть назву", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "📝 Creating tag: " + name);
            viewModel.createVirtualTag(name);
        });

        builder.setNegativeButton("Скасувати", null);
        builder.show();
    }

    /**
     * Показати деталі тегу
     */
    private void showTagDetails(TagEntity tag) {
        String type = tag.isVirtual() ? "📱 Віртуальний" : "💳 Фізичний";
        String info = "Назва: " + tag.name + "\n" +
                "Тип: " + type + "\n" +
                "UID: " + tag.tagUid + "\n" +
                "Створено: " + formatDate(tag.createdAt) + "\n" +
                "Останнє використання: " + formatDate(tag.lastUsedAt);

        new AlertDialog.Builder(this)
                .setTitle("Деталі тегу")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .setNeutralButton("Перейменувати", (d, w) -> showRenameDialog(tag))
                .setNegativeButton("Видалити", (d, w) -> showDeleteDialog(tag))
                .show();
    }

    /**
     * Діалог перейменування
     */
    private void showRenameDialog(TagEntity tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Перейменувати тег");

        final EditText input = new EditText(this);
        input.setText(tag.name);
        builder.setView(input);

        builder.setPositiveButton("Зберегти", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                Log.d(TAG, "✏️ Renaming tag " + tag.id + " to: " + newName);
                viewModel.renameTag(tag.id, newName);
            }
        });

        builder.setNegativeButton("Скасувати", null);
        builder.show();
    }

    /**
     * Діалог видалення
     */
    private void showDeleteDialog(TagEntity tag) {
        new AlertDialog.Builder(this)
                .setTitle("Видалити тег?")
                .setMessage("Тег '" + tag.name + "' буде видалено назавжди")
                .setPositiveButton("Видалити", (dialog, which) -> {
                    Log.d(TAG, "🗑️ Deleting tag: " + tag.id);
                    viewModel.deleteTag(tag.id);
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "Ніколи";
        return android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", timestamp).toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ onResume - Refreshing tags");

        // ✅ ВИПРАВЛЕНО: Оновити теги при поверненні на екран
        viewModel.loadTags();
    }
}
