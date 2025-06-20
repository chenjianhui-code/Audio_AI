package com.ai.app.audio_ai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.ai.app.audio_ai.data.AppDatabase;
import com.ai.app.audio_ai.data.User;
import com.ai.app.audio_ai.data.UserDao;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private Button buttonRegister;

    private ExecutorService executorService;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        executorService = Executors.newSingleThreadExecutor();

        // 初始化视图
        editTextUsername = view.findViewById(R.id.et_username);
        editTextPassword = view.findViewById(R.id.et_password);
        editTextConfirmPassword = view.findViewById(R.id.et_confirm_password);
        buttonRegister = view.findViewById(R.id.btn_register);
        
        // 设置确认密码验证
        setupConfirmPasswordValidator();

        // 设置注册按钮点击事件
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // 验证输入
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "请填写所有必填字段", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证密码匹配
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "两次输入的密码不匹配", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证密码强度
        int passwordStrength = calculatePasswordStrength(password);
        if (passwordStrength < 40) {
            Toast.makeText(getContext(), "密码强度太弱，请使用更强的密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建用户对象 - 使用默认值代替缺失的字段
        final User user = new User(username, password, 0, "未指定", "{}");

        // 在后台线程中保存用户
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            UserDao userDao = db.userDao();

            // 检查用户名是否已存在
            User existingUser = userDao.getUserByUsername(username);
            if (existingUser != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "用户名已存在", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // 保存用户
            userDao.insert(user);

            // 设置为已登录状态
            User newUser = userDao.getUserByUsername(username);
            if (newUser != null) {
                newUser.setLoggedIn(true);
                userDao.update(newUser);
            }

            // 注册成功，导航到FirstFragment
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "注册成功", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_registerFragment_to_navigation_home);
            });
        });
    }


    private void setupConfirmPasswordValidator() {
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword();
            }
        });
    }

    private void validateConfirmPassword() {
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        
        if (!confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                editTextConfirmPassword.setError("密码不匹配");
                buttonRegister.setEnabled(false);
            } else {
                editTextConfirmPassword.setError(null);
                buttonRegister.setEnabled(true);
            }
        }
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;
        if (password.matches(".*[A-Z].*")) score += 20; // 包含大写字母
        if (password.matches(".*[a-z].*")) score += 20; // 包含小写字母
        if (password.matches(".*\\d.*")) score += 20;   // 包含数字
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 10; // 包含特殊字符
        
        return Math.min(score, 100);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
