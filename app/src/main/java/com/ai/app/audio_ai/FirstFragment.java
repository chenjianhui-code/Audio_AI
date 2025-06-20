package com.ai.app.audio_ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.ai.app.audio_ai.data.User;
import com.ai.app.audio_ai.data.UserManager;
import com.ai.app.audio_ai.databinding.FragmentFirstBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        loadUserInfo();

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时重新加载用户信息
        loadUserInfo();
    }

    private void loadUserInfo() {
        // 检查是否有登录用户
        UserManager.getInstance().getCurrentUser(requireContext(), new UserManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (user == null) {
                    // 导航到注册页面
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_registerFragment);
                } else {
                    // 显示用户信息
                    requireActivity().runOnUiThread(() -> {
                        binding.textviewUsername.setText("用户名: " + user.getUsername());
                        binding.textviewEmail.setText("邮箱: " + user.getUsername() + "@example.com");
                        binding.textviewAge.setText("年龄: " + user.getAge());
                        binding.textviewGender.setText("性别: " + user.getGender());

                        // 解析用户偏好
                        try {
                            JSONObject preferencesJson = new JSONObject(user.getPreferences());
                            JSONArray preferencesArray = preferencesJson.getJSONArray("preferences");
                            StringBuilder preferences = new StringBuilder();
                            for (int i = 0; i < preferencesArray.length(); i++) {
                                preferences.append(preferencesArray.getString(i));
                                if (i < preferencesArray.length() - 1) {
                                    preferences.append(", ");
                                }
                            }
                            binding.textviewPreferences.setText("偏好: " + preferences.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            binding.textviewPreferences.setText("偏好: 未设置");
                        }
                    });
                }
            }
        });
    }

    private void logout() {
        // 清除用户信息
        UserManager.getInstance().logout(requireContext(), new UserManager.LogoutCallback() {
            @Override
            public void onLogoutSuccess() {
                requireActivity().runOnUiThread(() -> {
                    // 导航到注册页面
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_registerFragment);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
