package com.example.smartcampus.client.utils;

import android.os.Build;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 🆕 НОВИЙ: Генератор віртуальних NFC тегів
 *
 * КОНЦЕПЦІЯ:
 * - Генерує унікальний UID, який емулює фізичний NFC тег
 * - Формат такий самий як у фізичних тегів (hex)
 * - Може бути прив'язаний до конкретного пристрою
 *
 * ПРИКЛАД ВИКОРИСТАННЯ:
 * String virtualUid = VirtualTagGenerator.generate();
 * // → "VT-04:5E:2A:B2:4C:80:91" (VT = Virtual Tag)
 */
public class VirtualTagGenerator {

    private static final String PREFIX = "VT"; // Virtual Tag prefix
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Згенерувати випадковий віртуальний тег
     *
     * Формат: VT-XX:XX:XX:XX:XX:XX:XX (7 байт hex)
     */
    public static String generate() {
        byte[] uid = new byte[7];
        RANDOM.nextBytes(uid);

        StringBuilder sb = new StringBuilder(PREFIX + "-");
        for (int i = 0; i < uid.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", uid[i] & 0xFF));
        }

        return sb.toString();
    }

    /**
     * Згенерувати віртуальний тег прив'язаний до пристрою
     *
     * Використовує:
     * - Android ID
     * - Модель пристрою
     * - Timestamp
     */
    public static String generateForDevice() {
        String androidId = android.provider.Settings.Secure.ANDROID_ID;
        String deviceModel = Build.MODEL.replaceAll("[^a-zA-Z0-9]", "");
        long timestamp = System.currentTimeMillis();

        String seed = androidId + deviceModel + timestamp;
        int hash = seed.hashCode();

        // Перетворити hash в hex формат
        byte[] uid = new byte[7];
        for (int i = 0; i < 7; i++) {
            uid[i] = (byte) ((hash >> (i * 8)) & 0xFF);
        }

        StringBuilder sb = new StringBuilder(PREFIX + "-");
        for (int i = 0; i < uid.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", uid[i] & 0xFF));
        }

        return sb.toString();
    }

    /**
     * Згенерувати віртуальний тег для конкретного користувача
     */
    public static String generateForCurrentUser(SessionManager sessionManager) {
        long userId = sessionManager.getUserId();
        String email = sessionManager.getUserEmail();

        if (userId == -1 || email == null) {
            // Fallback: випадковий тег
            return generate();
        }

        // Детермінований тег на основі userId + device
        String seed = userId + email + android.provider.Settings.Secure.ANDROID_ID;
        int hash = seed.hashCode();

        byte[] uid = new byte[7];
        for (int i = 0; i < 7; i++) {
            uid[i] = (byte) ((hash >> (i * 8)) & 0xFF);
        }

        StringBuilder sb = new StringBuilder(PREFIX + "-");
        for (int i = 0; i < uid.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", uid[i] & 0xFF));
        }

        return sb.toString();
    }

    /**
     * Перевірити чи тег віртуальний
     */
    public static boolean isVirtual(String tagUid) {
        return tagUid != null && tagUid.startsWith(PREFIX + "-");
    }

    /**
     * Перевірити чи тег фізичний
     */
    public static boolean isPhysical(String tagUid) {
        return tagUid != null && !tagUid.startsWith(PREFIX + "-");
    }

    /**
     * Отримати короткий дисплей UID (для UI)
     *
     * VT-04:5E:2A:B2:4C:80:91 → VT-...80:91
     */
    public static String shortUid(String tagUid) {
        if (tagUid == null || tagUid.length() < 10) {
            return tagUid;
        }

        int lastColon = tagUid.lastIndexOf(':');
        if (lastColon > 0) {
            String prefix = tagUid.substring(0, 5); // "VT-04"
            String suffix = tagUid.substring(lastColon - 3); // "80:91"
            return prefix + "..." + suffix;
        }

        return tagUid;
    }

    /**
     * Згенерувати читабельну назву для тегу
     *
     * @return "Samsung Galaxy S21 (VT)"
     */
    public static String generateDefaultName() {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        // Capitalize перша літера
        deviceName = deviceName.substring(0, 1).toUpperCase() +
                deviceName.substring(1).toLowerCase();
        return deviceName + " (VT)";
    }
}