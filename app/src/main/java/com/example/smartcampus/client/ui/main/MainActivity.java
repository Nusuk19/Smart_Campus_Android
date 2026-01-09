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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartcampus.client.R;
import com.example.smartcampus.client.data.local.entities.ScheduleEntity;
import com.example.smartcampus.client.data.remote.RetrofitClient;
import com.example.smartcampus.client.data.remote.api.RoomApi;
import com.example.smartcampus.client.ui.auth.LoginActivity;
import com.example.smartcampus.client.ui.main.adapter.RoomAdapter;
import com.example.smartcampus.client.ui.main.model.Room;
import com.example.smartcampus.client.ui.nfc.NFCActivity;
import com.example.smartcampus.client.ui.profile.ProfileActivity;
import com.example.smartcampus.client.ui.tags.MyTagsActivity;
import com.example.smartcampus.client.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ✅ ОНОВЛЕНО: MainActivity з розкладом у діалозі
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MainViewModel vm;
    private RoomAdapter adapter;
    private SessionManager sessionManager;
    private RoomApi roomApi;

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

        // Ініціалізація API
        roomApi = RetrofitClient.get().create(RoomApi.class);

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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Smart Campus");
            }
            Log.d(TAG, "✅ Toolbar configured");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        errorText = findViewById(R.id.error_text);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        nfcButton = findViewById(R.id.nfc_button);

        if (recyclerView == null || swipeRefresh == null) {
            throw new IllegalStateException("Required views not found!");
        }

        Log.d(TAG, "✅ Views initialized");
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ✅ При кліку - показуємо діалог з розкладом
        adapter.setOnItemClickListener(room -> {
            if (room == null) return;
            showRoomInfoDialog(room);
        });

        Log.d(TAG, "✅ RecyclerView configured");
    }

    /**
     * ✅ ОНОВЛЕНО: Діалог з розкладом
     */
    private void showRoomInfoDialog(Room room) {
        Log.d(TAG, "📋 Loading schedule for: " + room.getName());

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Завантаження...")
                .setMessage("Отримання розкладу...")
                .setCancelable(false)
                .create();

        loadingDialog.show();

        // Завантажити поточну пару
        roomApi.getCurrentClass(room.getId()).enqueue(new Callback<ScheduleEntity>() {
            @Override
            public void onResponse(Call<ScheduleEntity> call, Response<ScheduleEntity> response) {
                loadingDialog.dismiss();

                ScheduleEntity currentClass = response.body();
                showRoomDialogWithSchedule(room, currentClass);
            }

            @Override
            public void onFailure(Call<ScheduleEntity> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Failed to load schedule", t);
                showRoomDialogWithSchedule(room, null);
            }
        });
    }

    /**
     * ✅ НОВИЙ: Показати діалог з інформацією та розкладом
     */
    private void showRoomDialogWithSchedule(Room room, ScheduleEntity currentClass) {
        StringBuilder info = new StringBuilder();
        info.append("🏠 Аудиторія: ").append(room.getName()).append("\n\n");

        if (room.getBuilding() != null && !room.getBuilding().isEmpty()) {
            info.append("🏢 Корпус: ").append(room.getBuilding()).append("\n");
        }

        if (room.getFloor() > 0) {
            info.append("🔢 Поверх: ").append(room.getFloor()).append("\n");
        }

        if (room.getCapacity() > 0) {
            info.append("👥 Вмістимість: ").append(room.getCapacity()).append(" місць\n");
        }

        info.append("\n");

        // Статус
        String statusIcon = room.isAvailable() ? "🟢" : "🔴";
        String statusText = room.isAvailable() ? "Вільна" : "Зайнята";
        info.append(statusIcon).append(" Статус: ").append(statusText).append("\n\n");

        // ✅ НОВИЙ: Інформація про поточну пару
        if (currentClass != null) {
            info.append("📚 Поточна пара:\n");
            info.append("   ").append(currentClass.subject).append("\n");
            info.append("   ⏰ ").append(currentClass.getFormattedTime()).append("\n");

            if (currentClass.groupName != null) {
                info.append("   👥 Група: ").append(currentClass.groupName).append("\n");
            }
        } else {
            info.append("📭 Зараз немає занять\n");
        }

        // Створюємо діалог
        new AlertDialog.Builder(this)
                .setTitle("📋 Інформація про аудиторію")
                .setMessage(info.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("📅 Весь розклад", (dialog, which) -> {
                    showFullSchedule(room);
                })
                .show();
    }

    /**
     * ✅ НОВИЙ: Показати повний розклад аудиторії
     */
    private void showFullSchedule(Room room) {
        Log.d(TAG, "📅 Loading full schedule for: " + room.getName());

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Завантаження розкладу...")
                .setCancelable(false)
                .create();

        loadingDialog.show();

        roomApi.getRoomScheduleToday(room.getId()).enqueue(new Callback<List<ScheduleEntity>>() {
            @Override
            public void onResponse(Call<List<ScheduleEntity>> call, Response<List<ScheduleEntity>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    List<ScheduleEntity> schedule = response.body();
                    showScheduleDialog(room, schedule);
                } else {
                    Toast.makeText(MainActivity.this, "Помилка завантаження розкладу", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleEntity>> call, Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Failed to load full schedule", t);
                Toast.makeText(MainActivity.this, "Помилка з'єднання", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ НОВИЙ: Показати діалог з повним розкладом
     */
    private void showScheduleDialog(Room room, List<ScheduleEntity> schedule) {
        StringBuilder scheduleText = new StringBuilder();
        scheduleText.append("📅 Розклад на сьогодні\n\n");
        scheduleText.append("🏠 ").append(room.getFullName()).append("\n\n");

        if (schedule.isEmpty()) {
            scheduleText.append("📭 Сьогодні занять немає");
        } else {
            for (ScheduleEntity item : schedule) {
                scheduleText.append("⏰ ").append(item.getFormattedTime()).append("\n");
                scheduleText.append("   📚 ").append(item.subject).append("\n");

                if (item.groupName != null) {
                    scheduleText.append("   👥 ").append(item.groupName).append("\n");
                }

                scheduleText.append("\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("📅 Розклад аудиторії")
                .setMessage(scheduleText.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupViewModel() {
        vm = new ViewModelProvider(this).get(MainViewModel.class);
        Log.d(TAG, "✅ ViewModel created");
    }

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

    private void setupNfcButton() {
        if (nfcButton != null) {
            nfcButton.setOnClickListener(v -> openNfcScanner());
            Log.d(TAG, "✅ NFC Button configured");
        }
    }

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

    private void showRoomsList(List<Room> rooms) {
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
            case 1:
                openProfile();
                return true;

            case 2:
                openMyTags();
                return true;

            case 3:
                openNfcScanner();
                return true;

            case 4:
                Log.d(TAG, "🔄 Menu refresh");
                vm.refresh();
                return true;

            case 5:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openMyTags() {
        Intent intent = new Intent(this, MyTagsActivity.class);
        startActivity(intent);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Вийти з акаунту?")
                .setMessage("Ви впевнені?")
                .setPositiveButton("Вийти", (dialog, which) -> {
                    sessionManager.clearSession();
                    goToLoginActivity();
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

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
