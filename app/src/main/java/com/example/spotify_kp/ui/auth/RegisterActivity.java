package com.example.spotify_kp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.spotify_kp.ui.main.MainActivity;
import com.example.spotify_kp.R;
import com.example.spotify_kp.utils.Resource;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация View
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Инициализация ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Обработчик кнопки Register
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // Обработчик ссылки Login
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Возврат на LoginActivity
            }
        });
    }

    private void handleRegister() {
        // Получение данных из полей
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Валидация
        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        // Показать прогресс
        showLoading(true);

        // Запрос на сервер
        authViewModel.register(name, email, password).observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading(true);
                        break;

                    case SUCCESS:
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Переход на главный экран
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        break;

                    case ERROR:
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                resource.getMessage(),
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        // Проверка имени
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            nameInput.setError("Name must be at least 3 characters");
            nameInput.requestFocus();
            return false;
        }

        if (name.length() > 50) {
            nameInput.setError("Name is too long");
            nameInput.requestFocus();
            return false;
        }

        // Проверка email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        // Проверка password
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        // Проверка confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            registerButton.setAlpha(0.5f);
        } else {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            registerButton.setAlpha(1.0f);
        }
    }
}