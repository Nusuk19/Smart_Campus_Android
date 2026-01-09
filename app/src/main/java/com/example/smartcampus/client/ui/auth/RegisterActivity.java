package com.example.smartcampus.client.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.ui.main.MainActivity;

/**
 * 🔄 ОНОВЛЕНО: Екран реєстрації з вибором ролі
 */
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    // Views
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private RadioGroup roleRadioGroup;
    private RadioButton studentRadio;
    private RadioButton professorRadio;
    private Button registerButton;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        roleRadioGroup = findViewById(R.id.role_radio_group);
        studentRadio = findViewById(R.id.student_radio);
        professorRadio = findViewById(R.id.professor_radio);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);
        errorText = findViewById(R.id.error_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getRegisterState().observe(this, state -> {
            switch (state.status) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    hideLoading();
                    Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                    break;
                case ERROR:
                    hideLoading();
                    showError(state.message);
                    break;
            }
        });
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> handleRegister());
        loginButton.setOnClickListener(v -> goToLoginActivity());
    }

    private void handleRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Валідація
        if (name.isEmpty()) {
            nameInput.setError("Ім'я обов'язкове");
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email обов'язковий");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Невірний формат email");
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Пароль обов'язковий");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Пароль має бути не менше 6 символів");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Паролі не співпадають");
            return;
        }

        // ✅ НОВИЙ: Отримати вибрану роль
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Оберіть роль", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = selectedRoleId == R.id.student_radio ? "STUDENT" : "PROFESSOR";

        // Реєстрація з роллю
        viewModel.register(email, password, name, role);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLoginActivity() {
        finish();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        errorText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        registerButton.setEnabled(true);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
