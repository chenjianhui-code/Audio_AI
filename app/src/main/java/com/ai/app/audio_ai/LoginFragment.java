package com.ai.app.audio_ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.ai.app.audio_ai.data.UserManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private CheckBox checkBoxRememberPassword;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        checkBoxRememberPassword = view.findViewById(R.id.checkBoxRememberPassword);
        progressBar = view.findViewById(R.id.progressBar);

        // 初始化SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 加载保存的登录信息
        loadSavedLoginInfo();

        // 设置登录按钮点击事件
        view.findViewById(R.id.buttonLogin).setOnClickListener(v -> login());

        // 设置去注册按钮点击事件
        view.findViewById(R.id.buttonGoToRegister).setOnClickListener(v -> {
            // 使用Navigation组件导航到注册页面
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }

    /**
     * 加载保存的登录信息
     */
    private void loadSavedLoginInfo() {
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (isRemembered) {
            String username = sharedPreferences.getString(KEY_USERNAME, "");
            String password = sharedPreferences.getString(KEY_PASSWORD, "");
            editTextUsername.setText(username);
            editTextPassword.setText(password);
            checkBoxRememberPassword.setChecked(true);
        }
    }

    /**
     * 保存登录信息
     */
    private void saveLoginInfo(String username, String password, boolean isRemembered) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isRemembered) {
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    /**
     * 登录
     */
    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            return;
        }

        // 显示进度条
        progressBar.setVisibility(View.VISIBLE);

        // 保存登录信息
        boolean isRemembered = checkBoxRememberPassword.isChecked();
        saveLoginInfo(username, password, isRemembered);

        // 执行登录
        UserManager.getInstance().login(requireContext(), username, password, new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess(com.ai.app.audio_ai.data.User user) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    // 导航到首页
                    NavController navController = NavHostFragment.findNavController(LoginFragment.this);
                    // 使用Navigation组件导航到首页
                    navController.navigate(R.id.action_loginFragment_to_navigation_home);
                });
            }

            @Override
            public void onLoginFailed(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
