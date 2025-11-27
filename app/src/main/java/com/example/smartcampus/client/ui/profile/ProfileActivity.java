package com.example.smartcampus.client.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.data.local.entities.UserEntity;
import com.example.smartcampus.client.ui.auth.AuthViewModel;
import com.example.smartcampus.client.ui.auth.LoginActivity;
import com.example.smartcampus.client.ui.tags.MyTagsActivity;
import com.example.smartcampus.client.utils.SessionManager;

/**
 * 🆕 Екран профілю користувача
 */
public class ProfileActivity extends AppCompatActivity {

    private ProfileViewModel viewModel;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    // Views
    private ImageView photoImageView;
    private TextView nameText;
    private TextView emailText;
    private TextView roleText;
    private Button editNameButton;
    private Button changePasswordButton;
    private Button myTagsButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupViewModels();
        setupObservers();
        setupListeners();
    }

    private void initViews() {
        photoImageView = findViewById(R.id.profile_photo);
        nameText = findViewById(R.id.profile_name);
        emailText = findViewById(R.id.profile_email);
        roleText = findViewById(R.id.profile_role);
        editNameButton = findViewById(R.id.edit_name_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        myTagsButton = findViewById(R.id.my_tags_button);
        logoutButton = findViewById(R.id.logout_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupViewModels() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(this);
    }

    private void setupObservers() {
        // Завантажити профіль
        viewModel.getCurrentUser().observe(this, this::displayUser);

        // Loading state
        viewModel.getLoadingState().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Success messages
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Error messages
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        editNameButton.setOnClickListener(v -> showEditNameDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        myTagsButton.setOnClickListener(v -> openMyTags());
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    /**
     * Відобразити дані користувача
     */
    private void displayUser(UserEntity user) {
        if (user == null) return;

        nameText.setText(user.name);
        emailText.setText(user.email);

        // Роль
        String roleDisplay;
        switch (user.role) {
            case "STUDENT":
                roleDisplay = "👨‍🎓 Студент";
                break;
            case "PROFESSOR":
                roleDisplay = "👨‍🏫 Викладач";
                break;
            case "ADMIN":
                roleDisplay = "👨‍💼 Адміністратор";
                break;
            default:
                roleDisplay = user.role;
        }
        roleText.setText(roleDisplay);

        // Фото (TODO: завантажити з URL)
        if (user.photoUrl != null && !user.photoUrl.isEmpty()) {
            // Glide.with(this).load(user.photoUrl).into(photoImageView);
        }
    }

    /**
     * Діалог редагування імені
     */
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редагувати ім'я");

        final EditText input = new EditText(this);
        input.setText(nameText.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Зберегти", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.updateName(newName);
            }
        });

        builder.setNegativeButton("Скасувати", null);
        builder.show();
    }

    /**
     * Діалог зміни пароля
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Змінити пароль");

        // Layout з двома полями
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText oldPasswordInput = new EditText(this);
        oldPasswordInput.setHint("Старий пароль");
        oldPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("Новий пароль");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Змінити", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Заповніть обидва поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Новий пароль має бути не менше 6 символів",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.changePassword(oldPassword, newPassword);
        });

        builder.setNegativeButton("Скасувати", null);
        builder.show();
    }

    /**
     * Відкрити екран управління тегами
     */
    private void openMyTags() {
        Intent intent = new Intent(this, MyTagsActivity.class);
        startActivity(intent);
    }

    /**
     * Діалог виходу
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Вийти з акаунту?")
                .setMessage("Ви впевнені що хочете вийти?")
                .setPositiveButton("Вийти", (dialog, which) -> {
                    authViewModel.logout();

                    // Перейти до LoginActivity
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }
}
