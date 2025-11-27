package com.example.smartcampus.client.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartcampus.client.R;
import com.example.smartcampus.client.ui.auth.LoginActivity;
import com.example.smartcampus.client.ui.main.adapter.RoomAdapter;
import com.example.smartcampus.client.ui.nfc.NFCActivity;
import com.example.smartcampus.client.ui.profile.ProfileActivity;
import com.example.smartcampus.client.ui.tags.MyTagsActivity;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * 🔄 ОНОВЛЕНО: Головний екран зі списком аудиторій
 *
 * ЗМІНИ:
 * - Додано перевірку авторизації
 * - Додано меню з Profile, My Tags
 * - Покращено обробку помилок
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MainViewModel vm;
    private RoomAdapter adapter;
    private SessionManager sessionManager;

    // Views
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextView errorText;
    private SwipeRefreshLayout swipeRefresh;
    private Button nfcButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перевірка авторизації
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to LoginActivity");
            goToLoginActivity();
            return;
        }

        setContentView(R.layout.activity_main);

        Log.d(TAG, "✅ onCreate started - User: " + sessionManager.getUserEmail());

        try {
            setupToolbar();
            initViews();
            setupRecyclerView();
            setupViewModel();
            setupObservers();
            setupSwipeRefresh();
            setupNfcButton();

            Log.d(TAG, "✅ Initialization complete");
        } catch (Exception e) {
            Log.e(TAG, "❌ Fatal error in onCreate", e);
            showFatalError("Критична помилка: " + e.getMessage());
        }
    }

    /**
     * Налаштування Toolbar з меню
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Smart Campus");
            }
            Log.d(TAG, "✅ Toolbar configured");
        } else {
            Log.w(TAG, "⚠️ Toolbar not found in layout");
        }
    }

    /**
     * Ініціалізація View
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        errorText = findViewById(R.id.error_text);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        nfcButton = findViewById(R.id.nfc_button);

        if (recyclerView == null) {
            throw new IllegalStateException("RecyclerView not found!");
        }
        if (swipeRefresh == null) {
            throw new IllegalStateException("SwipeRefreshLayout not found!");
        }

        Log.d(TAG, "✅ Views initialized");
    }

    /**
     * Налаштування RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new RoomAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(room -> {
            if (room == null) return;

            Log.d(TAG, "🔘 Room clicked: " + room.getName());
            Toast.makeText(this,
                    room.getFullName() + "\n" + room.getStatusText(),
                    Toast.LENGTH_SHORT).show();
        });

        Log.d(TAG, "✅ RecyclerView configured");
    }

    /**
     * Створення ViewModel
     */
    private void setupViewModel() {
        vm = new ViewModelProvider(this).get(MainViewModel.class);
        Log.d(TAG, "✅ ViewModel created");
    }

    /**
     * Спостереження за LiveData
     */
    private void setupObservers() {
        vm.getRooms().observe(this, rooms -> {
            Log.d(TAG, "📊 Rooms updated: " + (rooms != null ? rooms.size() : 0));
            hideLoading();

            if (rooms != null && !rooms.isEmpty()) {
                showRoomsList(rooms);
            } else {
                showEmptyState();
            }
        });

        vm.isLoading().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading();
            }
        });

        vm.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        Log.d(TAG, "✅ Observers configured");
    }

    /**
     * Pull-to-refresh
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 Refresh triggered");
            vm.refresh();
        });

        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light
        );

        Log.d(TAG, "✅ SwipeRefresh configured");
    }

    /**
     * Кнопка NFC знизу екрану
     */
    private void setupNfcButton() {
        if (nfcButton != null) {
            nfcButton.setOnClickListener(v -> openNfcScanner());
            Log.d(TAG, "✅ NFC Button configured");
        } else {
            Log.w(TAG, "⚠️ NFC Button not found in layout");
        }
    }

    /**
     * Відкрити NFC сканер
     */
    private void openNfcScanner() {
        Log.d(TAG, "🔍 Opening NFC Scanner...");

        try {
            Intent intent = new Intent(this, NFCActivity.class);
            startActivity(intent);
            Log.d(TAG, "✅ NFCActivity started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start NFCActivity", e);
            Toast.makeText(this,
                    "Помилка відкриття NFC сканера: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ========== UI СТАНИ ==========

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);
        if (errorText != null) errorText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    private void showRoomsList(java.util.List<com.example.smartcampus.client.ui.main.model.Room> rooms) {
        recyclerView.setVisibility(View.VISIBLE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);
        if (errorText != null) errorText.setVisibility(View.GONE);

        adapter.setRooms(rooms);
        Log.d(TAG, "✅ Showing " + rooms.size() + " rooms");
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Немає доступних аудиторій\n\nПотягніть вниз для оновлення");
        }
        if (errorText != null) errorText.setVisibility(View.GONE);

        Log.d(TAG, "📭 Empty state shown");
    }

    private void showError(String message) {
        if (errorText != null) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("❌ Помилка\n\n" + message);
        }
        recyclerView.setVisibility(View.GONE);
        if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showFatalError(String message) {
        if (errorText != null) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("💥 Критична помилка\n\n" + message);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // ========== МЕНЮ ==========

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Або програмно:
        menu.add(0, 1, 0, "👤 Профіль");
        menu.add(0, 2, 0, "🏷️ Мої теги");
        menu.add(0, 3, 0, "🔍 NFC Сканер");
        menu.add(0, 4, 0, "🔄 Оновити");
        menu.add(0, 5, 0, "🚪 Вийти");

        Log.d(TAG, "✅ Menu created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Профіль
                openProfile();
                return true;

            case 2: // Мої теги
                openMyTags();
                return true;

            case 3: // NFC сканер
                openNfcScanner();
                return true;

            case 4: // Оновити
                Log.d(TAG, "🔄 Menu refresh");
                vm.refresh();
                return true;

            case 5: // Вийти
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Відкрити профіль
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Відкрити мої теги
     */
    private void openMyTags() {
        Intent intent = new Intent(this, MyTagsActivity.class);
        startActivity(intent);
    }

    /**
     * Вийти з акаунту
     */
    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Вийти з акаунту?")
                .setMessage("Ви впевнені?")
                .setPositiveButton("Вийти", (dialog, which) -> {
                    sessionManager.clearSession();
                    goToLoginActivity();
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

    /**
     * Перейти до LoginActivity
     */
    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ========== LIFECYCLE ==========

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ onResume");

        // Перевірити чи користувач все ще залогінений
        if (!sessionManager.isLoggedIn()) {
            goToLoginActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 onDestroy");
    }
}
