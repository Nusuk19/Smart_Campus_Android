package com.example.smartcampus.client.ui.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * 🔄 ВИПРАВЛЕНО: NFCActivity з ViewModel
 */
public class NFCActivity extends AppCompatActivity {

    private static final String TAG = "NFCActivity";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView statusText;
    private SessionManager sessionManager;
    private NFCViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        Log.d(TAG, "✅ onCreate");

        statusText = findViewById(R.id.nfc_status_text);
        sessionManager = new SessionManager(this);

        // ✅ Створити ViewModel
        viewModel = new ViewModelProvider(this).get(NFCViewModel.class);

        // ✅ Спостерігати за станом
        setupObservers();

        // Перевірка авторизації
        if (!sessionManager.isLoggedIn()) {
            showStatus("❌ Помилка\n\nСпочатку увійдіть в додаток");
            finish();
            return;
        }

        // Перевірка NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Log.e(TAG, "❌ NFC not available");
            showStatus("❌ NFC недоступний\n\nЦей пристрій не підтримує NFC");

            // Симуляція для емулятора
            simulateVirtualTag();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Log.w(TAG, "⚠️ NFC disabled");
            showStatus("⚠️ NFC вимкнено\n\nУвімкніть NFC в налаштуваннях");
            Toast.makeText(this, "Увімкніть NFC", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "✅ NFC ready");
            showStatus("✅ NFC готовий\n\n📱 Піднесіть телефон до рідера на дверях");
        }

        // PendingIntent
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * ✅ НОВИЙ: Спостереження за станом відкриття дверей
     */
    private void setupObservers() {
        viewModel.getUnlockState().observe(this, state -> {
            switch (state.status) {
                case LOADING:
                    showStatus("🔄 Завантаження...\n\nПеревіряємо права доступу");
                    break;

                case SUCCESS:
                    Log.d(TAG, "✅ Door unlocked: " + state.response.roomName);
                    showStatus("✅ Двері відкрито!\n\n" +
                            "🚪 " + state.response.roomName + "\n\n" +
                            "Можете заходити");
                    vibrate();
                    Toast.makeText(this, "Двері відкрито!", Toast.LENGTH_LONG).show();

                    // Закрити екран через 2 сек
                    statusText.postDelayed(() -> finish(), 2000);
                    break;

                case DENIED:
                    Log.w(TAG, "⚠️ Access denied: " + state.message);
                    showStatus("❌ Доступ заборонено\n\n" + state.message);
                    vibrate();
                    Toast.makeText(this, "Доступ заборонено", Toast.LENGTH_LONG).show();
                    break;

                case OCCUPIED:
                    Log.w(TAG, "⚠️ Room occupied");
                    showStatus("⚠️ Аудиторія зайнята\n\nСпробуйте іншу");
                    vibrate();
                    Toast.makeText(this, "Аудиторія зайнята", Toast.LENGTH_LONG).show();
                    break;

                case ERROR:
                    Log.e(TAG, "❌ Error: " + state.message);
                    showStatus("❌ Помилка\n\n" + state.message);
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
                Log.d(TAG, "✅ NFC dispatch enabled");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to enable dispatch", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(this);
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to disable dispatch", e);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null) return;

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            handleNfcTag(intent);
        }
    }

    /**
     * ✅ ВИПРАВЛЕНО: Обробка фізичного NFC тегу
     */
    private void handleNfcTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag == null) {
            showStatus("⚠️ Помилка читання мітки");
            return;
        }

        byte[] tagId = tag.getId();
        String tagUid = bytesToHex(tagId);

        Log.d(TAG, "🏷️ Фізичний тег зчитано: " + tagUid);

        showStatus("✅ Тег зчитано!\n\n🏷️ UID: " + tagUid + "\n\n🔓 Відкриваємо двері...");
        vibrate();

        // ✅ Використовуємо ViewModel
        viewModel.unlockDoor(tagUid, null);
    }

    /**
     * ✅ ВИПРАВЛЕНО: Симуляція віртуального тегу
     */
    private void simulateVirtualTag() {
        Log.w(TAG, "⚠️ Simulation mode: Virtual Tag");

        // Отримати перший тег користувача з SessionManager
        String virtualTagUid = getFirstUserTag();

        showStatus("⚠️ Режим емуляції\n\n" +
                "📱 Віртуальний тег:\n" + virtualTagUid +
                "\n\nНатисніть для тесту");

        if (statusText != null) {
            statusText.setOnClickListener(v -> {
                Log.d(TAG, "🧪 Simulating virtual tag: " + virtualTagUid);
                showStatus("🧪 Використовую віртуальний тег\n\nUID: " + virtualTagUid + "\n\n🔓 Відкриваємо...");
                vibrate();

                // ✅ Використовуємо ViewModel
                viewModel.unlockDoor(virtualTagUid, "R305");  // Тестова аудиторія 305
            });
        }
    }

    /**
     * ✅ НОВИЙ: Отримати перший тег користувача
     */
    private String getFirstUserTag() {
        // Для тестування повертаємо хардкоджений тег
        // В реальності - завантажити з TagRepository
        String email = sessionManager.getUserEmail();

        if (email != null && email.contains("student")) {
            return "07:3F:1B:8A:2D:91";  // Студентська картка
        } else if (email != null && email.contains("professor")) {
            return "04:5E:2A:B2:4C:80";  // Картка викладача
        }

        return "VT-TEST:00:11:22:33";  // Fallback
    }

    // ========== HELPER МЕТОДИ ==========

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
            if (sb.length() < bytes.length * 3 - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private void showStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
        Log.d(TAG, "📝 " + message);
    }

    private void vibrate() {
        try {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(
                            200,
                            android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    ));
                } else {
                    vibrator.vibrate(200);
                }
                Log.d(TAG, "📳 Vibration");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Vibration failed", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 onDestroy");
    }
}