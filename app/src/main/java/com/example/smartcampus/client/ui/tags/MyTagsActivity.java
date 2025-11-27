package com.example.smartcampus.client.ui.tags;

import android.os.Bundle;
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
 * 🆕 НОВИЙ: Екран управління NFC тегами
 *
 * Функції:
 * - Список усіх тегів користувача
 * - Створення віртуального тегу
 * - Видалення тегу
 * - Перейменування тегу
 */
public class MyTagsActivity extends AppCompatActivity {

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
    }

    private void setupRecyclerView() {
        adapter = new TagAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Клік на тег
        adapter.setOnItemClickListener(tag -> {
            // Показати деталі тегу
            showTagDetails(tag);
        });

        // Довге натискання → видалення
        adapter.setOnItemLongClickListener(tag -> {
            showDeleteDialog(tag);
            return true;
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TagViewModel.class);

        viewModel.getTags().observe(this, tags -> {
            if (tags != null && !tags.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
                adapter.setTags(tags);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getLoadingState().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        addTagButton.setOnClickListener(v -> showCreateVirtualTagDialog());
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
                    viewModel.deleteTag(tag.id);
                    Toast.makeText(this, "Тег видалено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "Ніколи";
        return android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", timestamp).toString();
    }
}