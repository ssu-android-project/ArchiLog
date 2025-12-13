package com.example.frontrow.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.frontrow.MainActivity;
import com.example.frontrow.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPw, tilPwCheck;
    private TextInputEditText etName, etId, etPw, etPwCheck;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase / SharedPreferences 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        firestore    = FirebaseFirestore.getInstance();
        prefs        = getSharedPreferences("frontrow_prefs", Context.MODE_PRIVATE);

        // ---- View binding ----
        tilName     = findViewById(R.id.tilName);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPw       = findViewById(R.id.tilPw);
        tilPwCheck  = findViewById(R.id.tilPwCheck);

        etName      = findViewById(R.id.etName);
        etId        = findViewById(R.id.etId);       // 이메일
        etPw        = findViewById(R.id.etPw);
        etPwCheck   = findViewById(R.id.etPwCheck);

        MaterialButton btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptSignUp() {
        clearErrors();

        String name     = etName.getText()     != null ? etName.getText().toString().trim()     : "";
        String email    = etId.getText()       != null ? etId.getText().toString().trim()       : "";
        String pw       = etPw.getText()       != null ? etPw.getText().toString()              : "";
        String pwCheck  = etPwCheck.getText()  != null ? etPwCheck.getText().toString()         : "";

        boolean isValid = true;

        // 이름
        if (TextUtils.isEmpty(name)) {
            tilName.setError("이름을 입력해 주세요.");
            isValid = false;
        }

        // 이메일
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("이메일을 입력해 주세요.");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("이메일 형식이 올바르지 않습니다.");
            isValid = false;
        }

        // 비밀번호
        if (TextUtils.isEmpty(pw)) {
            tilPw.setError("비밀번호를 입력해 주세요.");
            isValid = false;
        } else if (pw.length() < 6) {   // Firebase Auth 최소 6자
            tilPw.setError("비밀번호는 6자 이상이어야 합니다.");
            isValid = false;
        }

        // 비밀번호 확인
        if (TextUtils.isEmpty(pwCheck)) {
            tilPwCheck.setError("비밀번호 확인을 입력해 주세요.");
            isValid = false;
        } else if (!pw.equals(pwCheck)) {
            tilPwCheck.setError("비밀번호가 서로 일치하지 않습니다.");
            isValid = false;
        }

        if (!isValid) return;

        // Firebase 회원가입 처리
        firebaseAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Firebase Auth 프로필에 displayName 저장
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                            user.updateProfile(profileUpdates);
                            // FireStore에도 사용자 문서 저장
                            saveUserProfileToFirestore(user.getUid(), name, email);
                        }
                        // 자동 로그인 플래그 저장
                        saveAutoLoginPrefs(email);
                        // 메인 화면으로 이동
                        successSignUp();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            tilEmail.setError("이미 가입된 이메일입니다.");
                        } else {
                            Toast.makeText(
                                    SignUpActivity.this,
                                    "회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private void saveUserProfileToFirestore(String uid, String name, String email) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("name", name);
        userDoc.put("email", email);

        // 실패하더라도 회원가입 자체는 진행되게 fire-and-forget
        firestore.collection("users")
                .document(uid)
                .set(userDoc);
    }

    private void saveAutoLoginPrefs(String email) {
        prefs.edit()
                .putBoolean("auto_login_enabled", true)
                .putString("saved_email", email)
                .apply();
    }

    private void successSignUp() {
        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPw.setError(null);
        tilPwCheck.setError(null);
    }
}