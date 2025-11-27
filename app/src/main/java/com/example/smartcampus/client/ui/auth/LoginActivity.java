package com.example.smartcampus.client.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.ui.main.MainActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * 🆕 НОВИЙ: Екран входу з підтримкою:
 * - Email + Password
 * - Google SSO
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    // Views
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private SignInButton googleSignInButton;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupViewModel();
        setupGoogleSignIn();
        setupListeners();

        // Якщо вже залогінений → MainActivity
        if (viewModel.isLoggedIn()) {
            goToMainActivity();
        }
    }

    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        progressBar = findViewById(R.id.progress_bar);
        errorText = findViewById(R.id.error_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getLoginState().observe(this, state -> {
            switch (state.status) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    hideLoading();
                    Toast.makeText(this, "Вхід успішний!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                    break;
                case ERROR:
                    hideLoading();
                    showError(state.message);
                    break;
            }
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id)) // Треба додати в strings.xml
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> goToRegisterActivity());
        googleSignInButton.setOnClickListener(v -> handleGoogleSignIn());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email обов'язковий");
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Пароль обов'язковий");
            return;
        }

        viewModel.login(email, password);
    }

    private void handleGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();

                Log.d(TAG, "Google Sign-In успішний: " + account.getEmail());
                viewModel.loginWithGoogle(idToken);

            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In failed", e);
                showError("Помилка Google авторизації: " + e.getMessage());
            }
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        googleSignInButton.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        googleSignInButton.setEnabled(true);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
