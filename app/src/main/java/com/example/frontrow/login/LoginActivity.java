package com.example.frontrow.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.frontrow.MainActivity;
import com.example.frontrow.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogleLogin;

    private GoogleSignInClient googleSignInClient;
    private final int RC_GOOGLE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        TextView tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));

        btnLogin.setOnClickListener(v -> attemptLogin());

        setupGoogleLogin();
        btnGoogleLogin.setOnClickListener(v -> googleSignIn());
    }

    // ------------------ 자동 로그인 ------------------
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("frontrow_prefs", MODE_PRIVATE);
        boolean auto = prefs.getBoolean("auto_login_enabled", false);

        if (auto && mAuth.getCurrentUser() != null) {
            goMain();
        }
    }

    // ------------------ 이메일 로그인 ------------------
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pw = etPassword.getText().toString().trim();

        boolean ok = true;

        if (email.isEmpty()) {
            etEmail.setError("이메일을 입력해주세요");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("이메일 형식이 올바르지 않습니다");
            ok = false;
        }

        if (pw.isEmpty()) {
            etPassword.setError("비밀번호를 입력해주세요");
            ok = false;
        } else if (pw.length() < 6) {
            etPassword.setError("비밀번호는 6자 이상이어야 합니다");
            ok = false;
        }

        if (!ok) return;

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        saveAutoLogin();
                        goMain();
                    } else {
                        etPassword.setError("이메일 또는 비밀번호가 올바르지 않습니다");
                    }
                });
    }

    private void saveAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("frontrow_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("auto_login_enabled", true).apply();
    }

    private void goMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ------------------ 구글 로그인 ------------------
    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))   // google-services.json에서 자동 생성됨
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                if (account != null) firebaseAuthWithGoogle(account.getIdToken());

            } catch (Exception e) {
                btnGoogleLogin.setText("로그인 실패");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        btnGoogleLogin.setEnabled(false);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    btnGoogleLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        saveAutoLogin();
                        goMain();
                    } else {
                        btnGoogleLogin.setText("로그인 실패");
                    }
                });
    }
}