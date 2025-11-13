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

import com.example.smartcampus.client.R;
import com.example.smartcampus.client.data.remote.RetrofitClient;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * NFCActivity з РЕАЛЬНОЮ інтеграцією API
 *
 * ВИПРАВЛЕННЯ:
 * ✅ Відправляє POST /api/rooms/open/{nfcTagId} на сервер
 * ✅ Оновлює статус аудиторії в БД
 * ✅ MainActivity автоматично бачить зміни через LiveData
 */
public class NFCActivity extends AppCompatActivity {

    private static final String TAG = "NFCActivity";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView statusText;

    // API інтерфейс для NFC
    interface NfcApi {
        @POST("rooms/open/{nfcTagId}")
        Call<ResponseBody> openRoomByNfc(@Path("nfcTagId") String nfcTagId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        Log.d(TAG, "✅ onCreate");

        statusText = findViewById(R.id.nfc_status_text);

        if (statusText == null) {
            Log.e(TAG, "❌ nfc_status_text not found!");
        }

        // Перевірка NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Log.e(TAG, "❌ NFC not available");
            showStatus("❌ NFC недоступний\n\nЦей пристрій не підтримує NFC");
            Toast.makeText(this, "NFC не підтримується", Toast.LENGTH_LONG).show();

            // Симуляція для емулятора
            simulateNfcForTesting();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Log.w(TAG, "⚠️ NFC disabled");
            showStatus("⚠️ NFC вимкнено\n\nУвімкніть NFC в налаштуваннях");
            Toast.makeText(this, "Увімкніть NFC", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "✅ NFC ready");
            showStatus("✅ NFC готовий\n\n📱 Піднесіть телефон до NFC-мітки");
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
     * Обробка NFC мітки
     */
    private void handleNfcTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag == null) {
            showStatus("⚠️ Помилка читання мітки");
            return;
        }

        byte[] tagId = tag.getId();
        String tagIdHex = bytesToHex(tagId);

        Log.d(TAG, "🏷️ NFC Tag read: " + tagIdHex);

        showStatus("✅ Мітка зчитана!\n\n🏷️ ID: " + tagIdHex + "\n\n🔓 Відкриваємо двері...");
        vibrate();

        // ✅ ВІДПРАВКА НА СЕРВЕР
        openDoorViaApi(tagIdHex);
    }

    /**
     * ✅ НОВА ЛОГІКА: Відкрити двері через API
     */
    private void openDoorViaApi(String nfcTagId) {
        Log.d(TAG, "🚪 Opening door via API: " + nfcTagId);

        NfcApi api = RetrofitClient.get().create(NfcApi.class);

        api.openRoomByNfc(nfcTagId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject json = new JSONObject(jsonString);

                        String status = json.optString("status");
                        String roomName = json.optString("roomName", "???");

                        if ("OK".equals(status)) {
                            Log.d(TAG, "✅ Door opened: " + roomName);

                            showStatus("✅ Двері відкрито!\n\n🚪 Аудиторія " + roomName + "\n\nМожете заходити");
                            Toast.makeText(NFCActivity.this,
                                    "Двері відкрито!", Toast.LENGTH_LONG).show();

                            // Закрити екран через 2 сек
                            statusText.postDelayed(() -> {
                                // ✅ MainActivity автоматично оновиться через LiveData
                                finish();
                            }, 2000);

                        } else if ("CONFLICT".equals(status)) {
                            showStatus("⚠️ Аудиторія вже зайнята\n\nСпробуйте іншу");
                            Toast.makeText(NFCActivity.this,
                                    "Аудиторія зайнята", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ JSON parse error", e);
                        showErrorStatus("Помилка обробки відповіді");
                    }

                } else {
                    Log.w(TAG, "⚠️ API response failed: " + response.code());

                    if (response.code() == 404) {
                        showStatus("❌ Мітка не зареєстрована\n\nID: " + nfcTagId);
                        Toast.makeText(NFCActivity.this,
                                "Невідома NFC мітка", Toast.LENGTH_LONG).show();
                    } else {
                        showErrorStatus("Помилка сервера: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ API request failed", t);
                showErrorStatus("Помилка підключення до сервера");

                // ✅ FALLBACK: Якщо сервер недоступний, працюємо офлайн
                if (isKnownNfcTag(nfcTagId)) {
                    showStatus("⚠️ Офлайн режим\n\n🚪 Аудиторія " + getRoomNameByNfc(nfcTagId) + "\n\n(Статус не оновлено)");
                }
            }
        });
    }

    /**
     * ✅ СИМУЛЯЦІЯ для емулятора
     */
    private void simulateNfcForTesting() {
        Log.w(TAG, "⚠️ Simulation mode");

        showStatus("⚠️ Режим симуляції\n\n(NFC недоступний)\n\nНатисніть для тесту");

        if (statusText != null) {
            statusText.setOnClickListener(v -> {
                Log.d(TAG, "🧪 Simulating NFC001");
                showStatus("🧪 Симуляція NFC\n\nID: NFC001\n\n🔓 Відкриваємо...");
                vibrate();
                openDoorViaApi("NFC001");  // ✅ Викликаємо РЕАЛЬНИЙ API
            });
        }
    }

    // ========== HELPER МЕТОДИ ==========

    private boolean isKnownNfcTag(String nfcTagId) {
        return nfcTagId.equals("NFC001") ||
                nfcTagId.equals("NFC002") ||
                nfcTagId.equals("NFC003") ||
                nfcTagId.equals("NFC004");
    }

    private String getRoomNameByNfc(String nfcTagId) {
        switch (nfcTagId) {
            case "NFC001": return "305";
            case "NFC002": return "306";
            case "NFC003": return "401";
            case "NFC004": return "210";
            default: return "???";
        }
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void showStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
        Log.d(TAG, "📝 " + message);
    }

    private void showErrorStatus(String error) {
        showStatus("❌ Помилка\n\n" + error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
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