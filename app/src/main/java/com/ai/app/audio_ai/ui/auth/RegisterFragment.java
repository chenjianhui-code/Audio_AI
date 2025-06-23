package com.ai.app.audio_ai.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ai.app.audio_ai.data.User;
import com.ai.app.audio_ai.data.UserManager;
import com.ai.app.audio_ai.databinding.FragmentRegisterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置注册按钮点击事件
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 验证密码强度
            int passwordStrength = calculatePasswordStrength(password);
            if (passwordStrength < 40) {
                Toast.makeText(requireContext(), "密码强度太弱，请使用更强的密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建用户对象
            User user = new User(
                username,
                password,
                0, // 默认年龄
                "未设置", // 默认性别
                "" // 默认偏好
            );
            
            // 设置默认喜好
            try {
                JSONObject preferencesJson = new JSONObject();
                JSONArray preferencesArray = new JSONArray();
                preferencesArray.put("音乐");
                preferencesArray.put("有声书");
                preferencesJson.put("preferences", preferencesArray);
                user.setPreferences(preferencesJson.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                user.setPreferences("{}");
            }

            // 注册用户
            UserManager.getInstance().register(requireContext(), user, new UserManager.RegisterCallback() {
                @Override
                public void onRegisterSuccess(User user) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "注册成功", Toast.LENGTH_SHORT).show();
                        // 自动登录
                        UserManager.getInstance().login(requireContext(), username, password, new UserManager.LoginCallback() {
                            @Override
                            public void onLoginSuccess(User user) {
                                requireActivity().runOnUiThread(() -> {
                                    // 切换到主页面并显示底部导航栏
                                    if (requireActivity() instanceof com.ai.app.audio_ai.ui.MainActivity) {
                                        ((com.ai.app.audio_ai.ui.MainActivity) requireActivity()).switchToMainPage();
                                    }
                                    // 导航到首页
                                    Navigation.findNavController(v).navigate(com.ai.app.audio_ai.R.id.action_registerFragment_to_navigation_home);
                                });
                            }

                            @Override
                            public void onLoginFailed(String errorMessage) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "登录失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(v).navigateUp();
                                });
                            }
                        });
                    });
                }

                @Override
                public void onRegisterFailed(String errorMessage) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "注册失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        // 设置返回按钮点击事件
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    /**
     * 计算密码强度
     * @param password 密码
     * @return 密码强度分数（0-100）
     */
    private int calculatePasswordStrength(String password) {
        int score = 0;
        
        // 基础分数：密码长度
        if (password.length() >= 8) {
            score += 10;
        }
        if (password.length() >= 12) {
            score += 10;
        }
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) {
            score += 10;
        }
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) {
            score += 10;
        }
        
        // 包含数字
        if (password.matches(".*\\d.*")) {
            score += 10;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            score += 20;
        }
        
        // 混合字符类型
        int typesCount = 0;
        if (password.matches(".*[a-z].*")) typesCount++;
        if (password.matches(".*[A-Z].*")) typesCount++;
        if (password.matches(".*\\d.*")) typesCount++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) typesCount++;
        
        if (typesCount >= 3) {
            score += 20;
        }
        
        // 限制最高分为100
        return Math.min(score, 100);
    }
}
