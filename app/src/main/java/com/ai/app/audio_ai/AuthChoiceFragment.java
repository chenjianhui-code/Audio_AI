package com.ai.app.audio_ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class AuthChoiceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth_choice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取UI元素引用
        ImageView ivLogo = view.findViewById(R.id.ivLogo);
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        CardView cardButtons = view.findViewById(R.id.cardButtons);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnSkip = view.findViewById(R.id.btnSkip);
        TextView tvVersion = view.findViewById(R.id.tvVersion);

        // 应用淡入动画
        applyFadeInAnimation(ivLogo, 500, 0);
        applyFadeInAnimation(tvWelcome, 500, 300);
        applyFadeInAnimation(tvDescription, 500, 500);
        applyFadeInAnimation(cardButtons, 500, 700);
        applyFadeInAnimation(tvVersion, 500, 900);

        // 设置登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_authChoiceFragment_to_loginFragment);
        });

        // 设置注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_authChoiceFragment_to_registerFragment);
        });

        // 设置跳过按钮点击事件
        btnSkip.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_authChoiceFragment_to_navigation_home);
        });
    }

    /**
     * 应用淡入动画到视图
     * @param view 要应用动画的视图
     * @param duration 动画持续时间（毫秒）
     * @param startOffset 动画开始延迟（毫秒）
     */
    private void applyFadeInAnimation(View view, long duration, long startOffset) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(duration);
        fadeIn.setStartOffset(startOffset);
        fadeIn.setFillAfter(true);
        view.startAnimation(fadeIn);
    }
}
