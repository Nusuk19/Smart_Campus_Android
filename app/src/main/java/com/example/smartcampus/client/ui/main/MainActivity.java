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
import com.example.smartcampus.client.ui.main.adapter.RoomAdapter;
import com.example.smartcampus.client.ui.nfc.NFCActivity;

/**
 * Головний екран зі списком аудиторій
 *
 * ВИПРАВЛЕННЯ:
 * ✅ Додано Toolbar для меню
 * ✅ Додано кнопку "NFC Scanner" знизу екрану
 * ✅ Детальне логування
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MainViewModel vm;
    private RoomAdapter adapter;

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
        setContentView(R.layout.activity_main);

        Log.d(TAG, "✅ onCreate started");

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
     * ✅ ДОДАНО: Налаштування Toolbar
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
     * ✅ ДОДАНО: Кнопка NFC знизу екрану
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
     * ✅ Відкрити NFC сканер
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
        menu.add(0, 1, 0, "🔍 NFC Сканер");
        menu.add(0, 2, 0, "🔄 Оновити");
        menu.add(0, 3, 0, "ℹ️ Про додаток");
        Log.d(TAG, "✅ Menu created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // NFC сканер
                openNfcScanner();
                return true;

            case 2: // Оновити
                Log.d(TAG, "🔄 Menu refresh");
                vm.refresh();
                return true;

            case 3: // Про додаток
                showAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Smart Campus")
                .setMessage("Система управління аудиторіями\n\nВерсія: 1.0")
                .setPositiveButton("OK", null)
                .show();
    }

    // ========== LIFECYCLE ==========

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ onResume");
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